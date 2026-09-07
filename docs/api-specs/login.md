# 로그인 (Login)

## 1. 기능 개요

| 항목 | 내용 |
|---|---|
| 기능명 | 로그인 |
| 카테고리 | 인증 |
| 사용자 | 유저 |
| Method | POST |
| URL | `/auth/login` |
| 설명 | 이메일/비밀번호로 로그인하고 토큰을 발급받는다 |

---

## 2. 요구사항 정의 (개선본)

### 화면 요소
- 이메일, 비밀번호
- [로그인], [회원가입], [비밀번호 찾기]

### 동작 흐름

1. 로그인 페이지 진입
2. 이메일/비밀번호 입력
3. [로그인] 버튼 클릭 → `POST /auth/login` 요청
4. 서버 인증 처리
    - 4-1. 이메일 또는 비밀번호 불일치(계정 미존재 포함) → "이메일 또는 비밀번호가 올바르지 않습니다." (계정 존재 여부를 노출하지 않기 위해 통일된 메시지 사용)
    - 4-2. **소셜 전용 계정으로 로그인 시도** → "구글 로그인을 이용해주세요." (이메일 중복확인/회원가입 때 이미 계정 존재 여부가 노출되므로, 로그인에서도 동일하게 안내)
    - 4-3. 인증 성공 → 계정 상태 확인
        - 4-3-1. **활성화(ACTIVE)** → Access Token(응답 body) + Refresh Token(httpOnly Secure Cookie) 발급 → 클라이언트 상태(Zustand)에 accessToken 저장 → 메인 페이지로 이동
        - 4-3-2. **소프트탈퇴(WITHDRAWN)** → 토큰 미발급, 대신 단기 `recoveryToken` 발급 → "탈퇴한 계정입니다. 계정을 복구하시겠습니까?" 팝업 노출 → [계정 복구] API로 연결

> 원본 요구사항 대비 추가/명확화된 부분:
> - 소셜 전용 계정 로그인 시도 케이스 추가 (`SOCIAL_ACCOUNT_ONLY`)
> - 소프트탈퇴 시 응답에 `userId`를 직접 노출하지 않고, 단기 `recoveryToken`(Redis 저장, TTL 5분)을 발급하여 계정 복구 API 호출 시 사용 — 비밀번호 검증 없이 임의의 userId로 복구를 시도하는 것을 방지하기 위함
> - 관리자 정지 등 별도 계정 상태는 이번 스콥에 없음 (`ACTIVE`/`WITHDRAWN` 두 가지만 존재)
> - 로그인 실패 횟수 제한(브루트포스 방어)은 이번 스콥에서 제외

---

## 3. API 명세

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| email | 로그인 이메일 | String | 필수 | N | `test123@gmail.com` |
| password | 비밀번호 | String | 필수 | N | `password123!` |

### Response

> Refresh Token은 응답 body에 포함하지 않고 `Set-Cookie` 헤더로 `httpOnly`, `Secure` 쿠키에 담아 전달합니다.

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| accessToken | 발급된 Access Token | String | 필수 (활성 계정 성공 시) | Y | `eyJhbGciOiJIUzI1NiIs...` |
| accountStatus | 계정 상태 | String | 필수 | N | `ACTIVE` / `WITHDRAWN` |
| userId | 사용자 PK | Long | 필수 (활성 계정 성공 시) | Y | `1024` |
| nickname | 닉네임 | String | 필수 (활성 계정 성공 시) | Y | `안뇽가리` |
| role | 권한 | String | 필수 (활성 계정 성공 시) | Y | `USER` / `ADMIN` |
| recoveryToken | 계정 복구용 단기 토큰 (소프트탈퇴 시에만) | String | 필수 (소프트탈퇴 시) | Y | `f3a9c1e2-...` |

**Example**

```json
// 성공 - 활성화 계정
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "accountStatus": "ACTIVE",
  "userId": 1024,
  "nickname": "안뇽가리",
  "role": "USER"
}

// 성공(인증O) - 소프트탈퇴 계정 → 토큰 미발급, 복구 절차 안내
{
  "accountStatus": "WITHDRAWN",
  "recoveryToken": "f3a9c1e2-...",
  "message": "탈퇴한 계정입니다. 계정을 복구하시겠습니까?"
}

// 실패 - 이메일/비밀번호 불일치 (계정 미존재 포함)
{
  "errorCode": "INVALID_CREDENTIALS",
  "message": "이메일 또는 비밀번호가 올바르지 않습니다."
}

// 실패 - 소셜 전용 계정으로 로그인 시도
{
  "errorCode": "SOCIAL_ACCOUNT_ONLY",
  "message": "구글 로그인을 이용해주세요."
}
```

### Status

| status | response content |
|---|---|
| 200 | 로그인 성공 (활성화 계정 → 토큰 발급) |
| 200 | 인증 성공했으나 소프트탈퇴 계정 → 토큰 미발급, `accountStatus: WITHDRAWN` + `recoveryToken` 반환 |
| 400 | 필수값 누락 |
| 401 | 이메일/비밀번호 불일치 |
| 403 | 소셜 전용 계정으로 로그인 시도 |

---

## 4. 구현 참고 (팀 컨벤션 반영)

### 계층 구조

```
com.sist.web.restcontroller.AuthRestController   ← POST /auth/login
com.sist.web.service.AuthService / AuthServiceImpl
com.sist.web.mapper.AuthMapper
resources/mybatis/mapper/auth-mapper.xml
com.sist.web.vo.UsersVO, LocalAccountVO
```

### 처리 로직 (Service 레이어)

1. `AuthMapper.findUserByEmail(email)` 조회 (users 테이블 — email-check.md와 동일 쿼리 재사용 가능)
    - 결과 없음 → `AuthException("INVALID_CREDENTIALS", ...)` (401)
2. 조회된 user의 `id`로 `AuthMapper.findLocalAccountByUserId(userId)` 조회
    - 결과 없음(로컬 계정 없음 = 소셜 전용) → `AuthException("SOCIAL_ACCOUNT_ONLY", ...)` (403)
3. `PasswordEncoder.matches(입력 password, 저장된 암호화 password)` 검증
    - 불일치 → `AuthException("INVALID_CREDENTIALS", ...)` (401) — 이메일 존재 여부와 무관하게 1번 실패와 동일한 에러코드/메시지로 통일 (계정 존재 여부 미노출)
4. `user.status` 확인
    - `ACTIVE` → Access Token 발급(JWT, body) + Refresh Token 발급(Redis 저장 + Set-Cookie) → 200 응답
    - `WITHDRAWN` → `recoveryToken`(랜덤 UUID) 생성 → Redis에 `recoveryToken → userId` 매핑 저장 (TTL 5분) → 200 응답 (토큰 미발급, recoveryToken만 반환)

### 예외 처리
- `AuthException` → `AuthExceptionHandler`가 `{ errorCode, message }`로 응답, errorCode별 status 매핑(401/403 등)

### Mapper 예시 (SQL)

```xml
<!-- findUserByEmail, findLocalAccountByUserId는 email-check.md에서 이미 정의됨 - 재사용 -->

<select id="findLocalAccountWithPasswordByUserId" resultType="com.sist.web.vo.LocalAccountVO" parameterType="int">
    SELECT id, password, user_id
    FROM local_accounts
    WHERE user_id = #{user_id}
</select>
```

> 참고: email-check.md의 `findLocalAccountByUserId`는 존재 여부 확인용(비밀번호 미포함)이고, 로그인에서는 비밀번호 검증이 필요하므로 `password` 컬럼을 포함한 별도 쿼리(`findLocalAccountWithPasswordByUserId`)를 사용합니다. 필요 시 하나로 통합해도 무방합니다 (성능 차이 미미).

---

## 5. 열린 이슈

없음 (이전 대화에서 소셜 판별, recoveryToken 방식, 관리자 정지 여부, 브루트포스 제한 여부 모두 확정됨)