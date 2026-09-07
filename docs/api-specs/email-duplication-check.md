# 이메일 중복확인 (Email Duplicate Check)

## 1. 기능 개요

| 항목 | 내용 |
|---|---|
| 기능명 | 이메일 중복확인 |
| 카테고리 | 인증 |
| 사용자 | 유저 |
| Method | GET |
| URL | `/auth/email/check` |
| 설명 | 회원가입 시 입력한 이메일의 사용 가능 여부를 확인한다 |

---

## 2. 요구사항 정의 (개선본)

### 화면 요소
- 이메일 입력창
- [이메일 중복검사] 버튼 (또는 입력 완료 시 자동 트리거 — 프론트 구현 시 확정)

### 동작 흐름

1. 사용자가 이메일 입력창에 값을 입력한다.
2. [이메일 중복검사] 버튼 클릭 (또는 입력창 이탈 시 자동 실행)
3. 서버에 `GET /auth/email/check?email={입력값}` 요청
4. 이메일 형식 검증
    - 4-1. 형식 오류 → "올바른 이메일 형식을 입력해주세요." 안내, 서버 요청 없이 프론트에서 우선 차단 (서버도 400으로 재검증)
5. 서버 응답에 따라 분기
    - 5-1. `available: true` → "사용 가능한 이메일입니다." 안내
    - 5-2. `available: false, reason: "DUPLICATE"` → "이미 사용중인 이메일입니다." 안내
    - 5-3. `available: false, reason: "SOCIAL_ONLY"` → "구글 로그인을 이용해주세요." 안내
6. 이후 이메일 입력값이 **변경되면 검증 상태를 초기화**한다 (재검사 없이 이전 결과로 회원가입 제출되는 것 방지 — 서버는 회원가입 시점에 어차피 재검증하므로 최종 방어선은 서버에 있음).

> 원본 요구사항 대비 추가/명확화된 부분: (1) 이메일 형식 오류 케이스, (2) 검증 후 값 변경 시 상태 초기화, (3) 트리거 방식(버튼 vs 자동)은 프론트 구현 시 확정 필요.

---

## 3. API 명세

### Request

**Query Parameter**

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| email | 확인할 이메일 주소 | String | 필수 | N | `test123@gmail.com` |

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| available | 사용 가능 여부 | Boolean | 필수 | N | `true` |
| reason | 사용 불가 시 사유 코드 | String | 선택 | Y | `DUPLICATE` / `SOCIAL_ONLY` |

**Example**

```json
// 사용 가능
{
  "available": true,
  "reason": null
}

// 이미 로컬 계정으로 가입된 이메일
{
  "available": false,
  "reason": "DUPLICATE"
}

// 구글 소셜 로그인으로 가입된 이메일
{
  "available": false,
  "reason": "SOCIAL_ONLY"
}
```

### Status

| status | response content |
|---|---|
| 200 | 정상 처리 (available 값으로 중복 여부 판단) |
| 400 | 이메일 형식이 올바르지 않음 |

---

## 4. 구현 참고 (팀 컨벤션 반영)

> 이 섹션은 CLAUDE.md의 아키텍처 규칙을 이 기능에 적용한 실제 설계입니다.

### 계층 구조

```
com.sist.web.restcontroller.AuthRestController   ← GET /auth/email/check
com.sist.web.service.AuthService                  ← 인터페이스
com.sist.web.service.AuthServiceImpl               ← 구현체
com.sist.web.mapper.AuthMapper                     ← 인터페이스
resources/mybatis/mapper/auth-mapper.xml           ← SQL
com.sist.web.vo.UsersVO / LocalAccountVO           ← 필요 시 참조
```

### 처리 로직 (Service 레이어)

> ⚠️ 스키마 변경 반영: `email`은 `local_accounts`가 아니라 **`users` 테이블에 저장**합니다 (로컬/소셜 공통 식별자). 아래 로직은 이 구조를 기준으로 합니다.

1. 이메일 형식 정규식 검증 → 실패 시 `AuthException("INVALID_EMAIL_FORMAT", "올바른 이메일 형식을 입력해주세요.")` 발생
2. `AuthMapper.findUserByEmail(email)` 조회 (users 테이블)
    - 결과 없음 → `available: true, reason: null`
    - 결과 존재 → 3번으로 진행
3. 조회된 user의 `id`로 `AuthMapper.findLocalAccountByUserId(userId)` 조회
    - 존재함(로컬 계정 있음) → `available: false, reason: "DUPLICATE"`
    - 존재하지 않음(소셜 계정만 있음) → `available: false, reason: "SOCIAL_ONLY"`

### 예외 처리
- `AuthException` 발생 시 `AuthExceptionHandler`(`@RestControllerAdvice`)가 잡아서 `{ errorCode, message }` 형태로 응답 (400)
- 컨트롤러에는 try-catch 불필요

### Mapper 예시 (SQL)

```xml
<select id="findUserByEmail" resultType="com.sist.web.vo.UsersVO" parameterType="String">
    SELECT id, email, nickname
    FROM users
    WHERE email = #{email}
</select>

<select id="findLocalAccountByUserId" resultType="com.sist.web.vo.LocalAccountVO" parameterType="int">
    SELECT id, user_id
    FROM local_accounts
    WHERE user_id = #{user_id}
</select>
```

---

## 5. 열린 이슈

- [x] 소셜 계정의 이메일 저장/조회 방식 확정 — `users.email`로 통합, 로컬/소셜 공통 사용
- [ ] 스키마 마이그레이션 필요: `users.email` 컬럼 추가 + `local_accounts.email`의 기존(더미) 데이터 이관. 다른 팀원이 `local_accounts.email`을 참조 중인지 먼저 확인 후 진행
- [ ] 프론트: 검사 트리거 방식(버튼 클릭 vs 자동) 확정

---

## 6. api 테스트 결과
| 케이스 | 요청 | 결과                                         |
|---|---|--------------------------------------------|
| 신규 이메일 | `email=new@gmail.com` | 200, `available: true, reason: null`       |
| 중복 이메일 | `email=user1@sist.co.kr` | 200, `available: false, reason: DUPLICATE` |
| 형식 오류 | `email=abc` | 400, `"errorCode: INVALID_EMAIL_FORMAT, message: 올바른 이메일 형식을 입력해주세요.`  |          

- 테스트 도구: Swagger UI
- 테스트 일자: 2026-09-07
- 결과: 명세서와 100% 일치, 별도 수정 없음