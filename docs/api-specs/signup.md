# 회원가입 (Signup)

## 1. 기능 개요

| 항목 | 내용 |
|---|---|
| 기능명 | 회원가입 |
| 카테고리 | 인증 |
| 사용자 | 유저 |
| Method | POST |
| URL | `/auth/signup` |
| 설명 | 이메일/비밀번호 기반으로 신규 회원을 등록한다 |

---

## 2. 요구사항 정의 (개선본)

### 화면 요소
- 이메일, 비밀번호, 비밀번호 확인, 닉네임
- [이메일 중복검사], [닉네임 중복검사] (또는 자동 검사)
- [회원가입] 버튼

### 동작 흐름

1. 회원가입 페이지 진입 ([회원가입] 버튼 비활성화)
2. 입력 항목 검증
    - 2-1. 이메일 중복확인 (`/auth/email/check` 결과 반영)
        - 사용 가능 → "사용 가능한 이메일입니다."
        - 중복(`DUPLICATE`) → "이미 사용중인 이메일입니다."
        - 소셜 전용(`SOCIAL_ONLY`) → "구글 로그인을 이용해주세요."
    - 2-2. 비밀번호 형식 검증 (문자+숫자+특수문자 조합, 8~20자) 및 비밀번호/비밀번호 확인 일치 검사
    - 2-3. 닉네임 입력 후 포커스 이탈 시 자동 중복확인 (`/auth/nickname/check` 결과 반영, 2~10자·한글/영문/숫자만 허용)
    - 2-4. 모든 항목이 유효할 때만 [회원가입] 버튼 활성화
3. [회원가입] 버튼 클릭 → `POST /auth/signup` 요청
4. 서버 응답 처리
    - 4-1. 성공(201) → "회원가입이 완료되었습니다." 안내 후 **로그인 페이지로 이동** (자동 로그인 없음 — 사용자가 직접 재로그인)
    - 4-2. 실패 → 에러코드별 메시지 표시 (아래 Response 참조)
5. 이메일/닉네임 값이 중복확인 통과 후 **변경되면 프론트 검증 상태는 초기화**되지만, 최종 방어는 서버가 4단계 재검증으로 수행한다.

> 원본 요구사항 대비 추가/명확화된 부분:
> - 클라이언트 중복확인 통과 여부와 무관하게 **서버가 제출 시점에 이메일·닉네임 중복을 반드시 재검증**한다 (클라이언트 검증은 UX 보조 수단일 뿐).
> - 비밀번호 정책(문자+숫자+특수문자, 8~20자)과 닉네임 정책(2~10자, 한글/영문/숫자)을 명시.
> - 회원가입 성공 시 자동 로그인 하지 않음(토큰 미발급) — 로그인 페이지로 이동 후 사용자가 직접 로그인.
> - 이용약관 동의 절차는 현재 요구사항에 없어 이번 스펙에서 제외 (필요 시 추후 반영).

---

## 3. API 명세

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| email | 이메일 (로그인 ID로 사용) | String | 필수 | N | `test123@gmail.com` |
| password | 비밀번호 (영문+숫자+특수문자 조합, 8~20자) | String | 필수 | N | `password123!` |
| passwordConfirm | 비밀번호 확인 | String | 필수 | N | `password123!` |
| nickname | 닉네임 (2~10자, 한글/영문/숫자) | String | 필수 | N | `안뇽가리` |

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| userId | 생성된 사용자 PK | Long | 필수 | N | `1024` |
| email | 가입된 이메일 | String | 필수 | N | `test123@gmail.com` |
| nickname | 가입된 닉네임 | String | 필수 | N | `안뇽가리` |

**Example**

```json
// 성공
{
  "userId": 1024,
  "email": "test123@gmail.com",
  "nickname": "안뇽가리"
}

// 실패 - 이메일 중복 (서버 재검증)
{
  "errorCode": "EMAIL_DUPLICATE",
  "message": "이미 사용 중인 이메일입니다."
}

// 실패 - 닉네임 중복 (서버 재검증)
{
  "errorCode": "NICKNAME_DUPLICATE",
  "message": "이미 사용 중인 닉네임입니다."
}

// 실패 - 비밀번호 불일치
{
  "errorCode": "PASSWORD_MISMATCH",
  "message": "비밀번호가 일치하지 않습니다."
}

// 실패 - 비밀번호 형식 오류
{
  "errorCode": "INVALID_PASSWORD_FORMAT",
  "message": "비밀번호는 문자, 숫자, 특수기호를 모두 포함해 8~20자로 입력해주세요."
}

// 실패 - 닉네임 형식 오류
{
  "errorCode": "INVALID_NICKNAME_FORMAT",
  "message": "닉네임은 2~10자의 한글, 영문, 숫자만 사용 가능합니다."
}
```

### Status

| status | response content |
|---|---|
| 201 | 회원가입 성공 |
| 400 | 필수값 누락, 비밀번호/닉네임 형식 오류, 비밀번호 불일치 |
| 409 | 이메일 또는 닉네임 중복 |

---

## 4. 구현 참고 (팀 컨벤션 반영)

### 계층 구조

```
com.sist.web.restcontroller.AuthRestController   ← POST /auth/signup
com.sist.web.service.AuthService / AuthServiceImpl
com.sist.web.mapper.AuthMapper
resources/mybatis/mapper/auth-mapper.xml
com.sist.web.vo.UsersVO, LocalAccountVO
```

### 처리 로직 (Service 레이어) — 순서 중요

1. **형식 검증** (이메일 형식, 비밀번호 정규식, 비밀번호/확인 일치, 닉네임 정규식) → 실패 시 각각의 `errorCode`로 `AuthException` 발생 (400)
2. **서버 재검증 — 이메일 중복**: `AuthMapper.findUserByEmail(email)` (users 테이블) → 존재하면 `AuthException("EMAIL_DUPLICATE", ...)` (409)
3. **서버 재검증 — 닉네임 중복**: `AuthMapper.findUserByNickname(nickname)` → 존재하면 `AuthException("NICKNAME_DUPLICATE", ...)` (409)
4. 비밀번호 암호화 (`PasswordEncoder.encode(password)`) — SecurityConfig에 Bean 추가 필요 (아직 없음)
5. `users` 레코드 생성 (nickname, status='ACTIVE', role='USER' 기본값) → 생성된 PK 확보
6. `local_accounts` 레코드 생성 (email, 암호화된 password, user_id = 5번에서 생성된 PK)
7. 생성된 userId, email, nickname을 응답으로 반환

> ⚠️ **트랜잭션 처리 필수**: 5번과 6번은 하나의 트랜잭션으로 묶여야 합니다. `users` 생성 후 `local_accounts` 생성이 실패하면 고아 레코드가 남으므로, Service 메서드에 `@Transactional`을 반드시 적용합니다.

> ⚠️ **PK 확보 방식**: Oracle은 AUTO_INCREMENT가 없어 시퀀스를 사용합니다. MyBatis에서 `<selectKey>`(INSERT 전에 시퀀스 값을 미리 조회) 또는 INSERT 후 `SELECT {시퀀스}.CURRVAL`로 생성된 PK를 받아와야 `local_accounts.user_id`에 채울 수 있습니다.

### 예외 처리
- 모든 실패 케이스는 `AuthException(errorCode, message)` 형태로 통일, `AuthExceptionHandler`가 400/409 등 적절한 status로 매핑
- `errorCode`별 status 매핑 예시: `EMAIL_DUPLICATE`/`NICKNAME_DUPLICATE` → 409, 나머지 형식 오류 → 400

### Mapper 예시 (SQL)

```xml
<insert id="insertUser" parameterType="com.sist.web.vo.UsersVO">
    <selectKey keyProperty="id" resultType="int" order="BEFORE">
        SELECT users_seq.NEXTVAL FROM dual
    </selectKey>
    INSERT INTO users (id, email, nickname, status, role, created_at, updated_at)
    VALUES (#{id}, #{email}, #{nickname}, 'ACTIVE', 'USER', SYSTIMESTAMP, SYSTIMESTAMP)
</insert>

<insert id="insertLocalAccount" parameterType="com.sist.web.vo.LocalAccountVO">
    <selectKey keyProperty="id" resultType="int" order="BEFORE">
        SELECT local_accounts_seq.NEXTVAL FROM dual
    </selectKey>
    INSERT INTO local_accounts (id, password, user_id)
    VALUES (#{id}, #{password}, #{user_id})
</insert>
```

> `email`은 이제 `users`에 저장하므로 `local_accounts`는 `password`, `user_id`만 갖습니다.

---

## 5. 열린 이슈

- [ ] 이용약관 동의 절차 포함 여부 (현재 제외)
- [x] `SOCIAL_ONLY` 판별 로직 — `users.email` 통합으로 해결 (email-check.md 참조)
- [ ] 스키마 마이그레이션 필요: `users.email` 컬럼 추가, `local_accounts.email`의 기존(더미) 데이터 이관 후 해당 컬럼은 당분간 유지(다른 팀원 참조 여부 확인 후 제거)
- [ ] `PasswordEncoder` Bean이 SecurityConfig에 아직 없음 — 이 기능 구현 시 함께 추가 필요

---

## 6. 테스트 기록

### 요약
| # | 케이스 | 상태 | errorCode |
|---|---|---|---|
| 1 | 성공 | 201 | - |
| 2 | 이메일 중복 | 409 | EMAIL_DUPLICATE |
| 3 | 닉네임 중복 | 409 | NICKNAME_DUPLICATE |
| 4 | 비밀번호 형식 오류 | 400 | INVALID_PASSWORD_FORMAT |
| 5 | 비밀번호 불일치 | 400 | PASSWORD_MISMATCH |
| 6 | 닉네임 형식 오류 | 400 | INVALID_NICKNAME_FORMAT |
| 7 | 이메일 형식 오류 | 400 | INVALID_EMAIL_FORMAT |

- 테스트 도구: Swagger UI
- 테스트 일자: 2026-09-08
- 결과: 명세서와 100% 일치, 별도 수정 없음
### 상세

<details>
<summary>1. 성공</summary>

**Request**
```json
{ "email": "test123@naver.com", "password": "password123!", "passwordConfirm": "password123!", "nickname": "안뇽가리" }
```
**Response** `201`
```json
{ "userId": 1001, "email": "test123@naver.com", "nickname": "안뇽가리" }
```
</details>
<details>
<summary>2. 이메일 중복</summary>

**Request**
```json
{ "email": "user1@sist.co.kr", "password": "password123!", "passwordConfirm": "password123!", "nickname": "안뇽가리" }
```
**Response** `409`
```json
{ "errorCode": "EMAIL_DUPLICATE", "message": "이미 사용 중인 이메일입니다." }
```
</details>
<details>
<summary>3. 닉네임 중복</summary>

**Request**
```json
{ "email": "test1234@naver.com", "password": "password123!", "passwordConfirm": "password123!", "nickname": "김철수" }
```
**Response** `409`
```json
{ "errorCode": "NICKNAME_DUPLICATE", "message": "이미 사용 중인 닉네임입니다." }
```
</details>
<details>
<summary>4. 비밀번호 형식 오류</summary>

**Request**
```json
{ "email": "test1234@naver.com", "password": "password123", "passwordConfirm": "password123", "nickname": "김철수" }
```
**Response** `400`
```json
{ "errorCode": "INVALID_PASSWORD_FORMAT", "message": "비밀번호는 문자, 숫자, 특수기호를 모두 포함해 8~20자로 입력해주세요." }
```
</details>
<details>
<summary>5. 비밀번호 불일치</summary>

**Request**
```json
{ "email": "test1234@naver.com", "password": "password123!", "passwordConfirm": "password123", "nickname": "김철수" }
```
**Response** `400`
```json
{ "errorCode": "PASSWORD_MISMATCH", "message": "비밀번호가 일치하지 않습니다." }
```
</details>
<details>
<summary>6. 닉네임 형식 오류</summary>

**Request**
```json
{ "email": "test1234@naver.com", "password": "password123!", "passwordConfirm": "password123!", "nickname": "김" }
```
**Response** `400`
```json
{ "errorCode": "INVALID_NICKNAME_FORMAT", "message": "닉네임은 2~10자의 한글, 영문, 숫자만 사용 가능합니다." }
```
</details>
<details>
<summary>7. 이메일 형식 오류</summary>

**Request**
```json
{ "email": "test1234", "password": "password123!", "passwordConfirm": "password123!", "nickname": "김철수" }
```
**Response** `400`
```json
{ "errorCode": "INVALID_EMAIL_FORMAT", "message": "올바른 이메일 형식을 입력해주세요." }
```
</details>
 