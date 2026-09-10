# 비밀번호 찾기 (재설정 링크 요청)

## 1. 기능 개요

| 항목 | 내용 |
|---|---|
| 기능명 | 비밀번호 찾기 (재설정 링크 요청) |
| 카테고리 | 인증 |
| 사용자 | 비로그인 유저 |
| Method | POST |
| URL | `/auth/password/reset-request` |
| 설명 | 입력한 이메일로 비밀번호 재설정 링크(또는 소셜 가입 안내)를 전송한다 |

---

## 2. 요구사항 정의

### 화면 요소
- 로그인 페이지 → [비밀번호 찾기] → 이메일 입력 → [재설정 링크 전송]

### 동작 흐름

1. 로그인 페이지에서 [비밀번호 찾기] 클릭
2. 이메일 입력 → [재설정 링크 전송] 클릭 → `POST /auth/password/reset-request` 요청
3. 이메일 형식 검증 (실패 시 즉시 400 반환)
4. **요청 주기 제한 확인** (동일 이메일 기준 60초 이내 재요청인지)
    - 4-1. 제한에 걸림 → 429 반환, 안내 메일 발송하지 않음 (형식 검증만 통과하면 가입 여부와 무관하게 동일하게 적용됨 — 존재 여부를 노출하지 않기 위함)
5. 입력된 이메일의 가입 여부 확인
    - 5-1. **미가입** → 토큰 생성/저장 없음, 메일 발송 없음
    - 5-2. **가입 + 일반 가입** → 기존에 발급된 활성 토큰이 있다면 무효화(삭제) → 새 랜덤 토큰 생성 및 Redis 저장(TTL 30분) → 토큰을 포함한 비밀번호 재설정 URL을 메일로 전송
    - 5-3. **가입 + 소셜 가입** → 토큰 생성/저장 없음, "Google 로그인을 이용해주세요" 안내 메일 전송
6. 가입 여부 및 방식과 무관하게 "입력하신 이메일로 안내 메일을 전송했습니다" 문구를 동일하게 표시 (계정 존재 여부 비노출)
7. 실제 가입된 이메일인 경우에만 메일 도착 (재설정 링크 또는 소셜 가입 안내)

> 원본 요구사항 대비 추가/확정된 부분:
> - **재설정 토큰 TTL**: 30분 (기존 확정 아키텍처와 동일한 정책 적용)
> - **재요청 시 이전 토큰 처리**: 같은 이메일로 재설정 링크를 다시 요청하면, 이전에 발급된 토큰은 즉시 무효화하고 최신 링크만 유효하게 한다 (오래된 링크로 재설정 시도 방지).
> - **요청 주기 제한(메일 폭탄 방지) 추가**: 동일 이메일로 60초 이내 재요청 시 429로 거부한다. 가입 여부와 무관하게 이메일 형식만 유효하면 동일하게 제한을 적용하여, 제한 발동 여부로 계정 존재 유무가 노출되지 않도록 한다.
> - **메일 발송 시스템 오류 처리 추가**: SMTP 장애 등으로 실제 메일 발송 자체가 실패한 경우, 계정 존재 여부와 무관한 순수 인프라 오류이므로 통일된 성공 메시지 대신 별도의 시스템 오류를 안내한다.
> - 로그인 실패 횟수 제한과 달리, 이 기능의 남용 방지는 "특정 이메일 주소로의 스팸성 메일 발송"을 막기 위한 것이므로 이번 스콥에 포함한다.

---

## 3. API 명세

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| email | 비밀번호를 재설정할 이메일 | String | 필수 | N | `test123@gmail.com` |

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| message | 처리 결과 안내 메시지 | String | 필수 | N | `입력하신 이메일로 안내 메일을 전송했습니다.` |

**Example**

```json
// 성공 (실제로는 미가입/소셜가입/일반가입 모두 이 응답으로 통일됨)
{
  "message": "입력하신 이메일로 안내 메일을 전송했습니다."
}

// 실패 - 이메일 형식 오류
{
  "errorCode": "INVALID_EMAIL_FORMAT",
  "message": "올바른 이메일 형식을 입력해주세요."
}

// 실패 - 요청 주기 제한 초과
{
  "errorCode": "TOO_MANY_REQUESTS",
  "message": "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."
}

// 실패 - 메일 발송 시스템 오류
{
  "errorCode": "MAIL_SEND_FAILED",
  "message": "일시적인 오류로 메일 전송에 실패했습니다. 잠시 후 다시 시도해주세요."
}
```

### Status

| status | response content |
|---|---|
| 200 | 요청 처리 완료 (내부적으로 미가입/일반가입/소셜가입 분기되어 처리되지만 응답은 동일) |
| 400 | 이메일 형식 오류 (필수값 누락 포함) |
| 429 | 동일 이메일로 60초 이내 재요청 (가입 여부와 무관하게 동일 적용) |
| 500 | 메일 발송 시스템 오류 (SMTP 장애 등, 계정 존재 여부와 무관한 인프라 오류) |

---

## 4. 구현 참고 (팀 컨벤션 반영)

### 계층 구조

```
com.sist.web.restcontroller.AuthRestController   ← POST /auth/password/reset-request
com.sist.web.service.AuthService / AuthServiceImpl
com.sist.web.mapper.AuthMapper
resources/mybatis/mapper/auth-mapper.xml
com.sist.web.vo.UsersVO, LocalAccountVO
com.sist.web.util.MailSender (or MailService)
```

### Redis 키 설계

| 키 패턴 | 값 | TTL | 용도 |
|---|---|---|---|
| `pwReset:{token}` | `userId` | 30분 | 재설정 토큰 유효성 검증용 (다음 단계인 "재설정 링크 유효성 검증" API에서 조회) |
| `pwResetUser:{userId}` | `token` | 30분 | 특정 유저의 현재 활성 토큰을 역으로 찾기 위한 인덱스 — 재요청 시 이 키로 이전 토큰을 찾아 `pwReset:{oldToken}` 삭제 |
| `pwResetLimit:{email}` | `"1"` | 60초 | 요청 주기 제한 — 키 존재 시 재요청 거부 |

### 처리 로직 (Service 레이어)

1. 이메일 형식 검증 (정규식) → 실패 시 `AuthException("INVALID_EMAIL_FORMAT", ...)` (400)
2. Redis `pwResetLimit:{email}` 존재 확인
    - 존재함 → `AuthException("TOO_MANY_REQUESTS", ...)` (429)
    - 존재하지 않음 → `SETEX pwResetLimit:{email} 60 "1"` 설정 후 다음 단계 진행 (가입 여부 확인 **이전에** 설정하여, 제한 발동 여부로 계정 존재 유무가 노출되지 않도록 함)
3. `AuthMapper.findUserByEmail(email)` 조회 (email-check.md와 동일 쿼리 재사용)
    - 결과 없음(미가입) → 토큰 생성/메일 발송 없이 바로 200 응답 준비
4. 조회된 user의 `id`로 `AuthMapper.findLocalAccountByUserId(userId)` 조회
    - 결과 없음(소셜 전용) → 안내 메일("Google 로그인을 이용해주세요") 발송 시도
    - 결과 있음(일반 가입) →
        1. Redis `pwResetUser:{userId}` 조회 → 기존 활성 토큰이 있으면 `pwReset:{oldToken}` 삭제 (무효화)
        2. 신규 토큰(UUID) 생성 → `pwReset:{token}` → `userId` 저장(TTL 30분), `pwResetUser:{userId}` → `token` 저장(TTL 30분)
        3. 재설정 URL(`.../reset-password?token={token}`)을 포함한 메일 발송 시도
5. 메일 발송 시도 결과 확인
    - 발송 실패(SMTP 예외 등) → `AuthException("MAIL_SEND_FAILED", ..., HttpStatus.INTERNAL_SERVER_ERROR)` (500)
    - 발송 성공 또는 애초에 발송 대상이 아님(미가입) → `{ "message": "입력하신 이메일로 안내 메일을 전송했습니다." }` 200 응답

### 예외 처리
- `AuthException` → `AuthExceptionHandler`가 `{ errorCode, message }`로 응답
- `INVALID_EMAIL_FORMAT` → 400, `TOO_MANY_REQUESTS` → 429, `MAIL_SEND_FAILED` → 500

### Mapper 예시 (SQL)

```xml
<!-- findUserByEmail, findLocalAccountByUserId는 email-check.md / login.md에서 이미 정의됨 - 재사용 -->
```

---

## 5. 열린 이슈

없음 (재요청 시 이전 토큰 무효화 방식, 요청 주기 제한(60초/이메일) 포함 여부, 메일 발송 시스템 오류 시 별도 안내 방식 모두 확정됨)

---

## 6. 테스트 기록

### 요약
| # | 케이스                 | 상태  | errorCode           |
|---|---------------------|-----|---------------------|
| 1 | 성공 (메일 전송)          | 200 | -                   |
| 2 | 미가입 이메일 (DB에 없는 메일) | 200 | -                   |
| 3 | 형식 오류               | 400 | INVALID_EMAIL_FORMAT |
| 4 | 잦은 요청 (60초)         | 429 | TOO_MANY_REQUESTS   |
| 5 | 제한 해제 후 재요청 (성공)    | 200 | -                   |
| 6 | 이전 토큰 무효화 확인           | -   |                     |
| 7 | 메일 발송 시스템 오류           | 500 | MAIL_SEND_FAILED |

- 테스트 도구: Swagger UI
- 테스트 일자: 2026-09-10

### 상세

<details>
<summary>1. 성공 (메일 전송)</summary>

**Request**
```json
{
  "email": "DB에 저장되어 있는 이메일"
}
```
**Response** `200`
```json
{
   "message": "입력하신 이메일로 안내 메일을 전송했습니다."
}
```
</details>
<details>
<summary>2. 미가입 이메일 (DB에 없는 메일)</summary>

**Request**
```json
{
  "email": "DB에 없는 메일"
}
```
**Response** `200`
- 완료 메시지는 뜨지만, 실제 메일 전송은 안함.
```json
{
   "message": "입력하신 이메일로 안내 메일을 전송했습니다."
}
```
</details>
<details>
<summary>3. 형식 오류</summary>

**Request**
```json
{
  "email": "잘못된 이메일 형식"
}
```
**Response** `400`
```json
{
   "errorCode": "INVALID_EMAIL_FORMAT",
   "message": "올바른 이메일 형식을 입력해주세요."
}
```
</details>
<details>
<summary>4. 잦은 요청 (60초)</summary>

**Request**
```json
{
  "email": "DB에 저장되어 있는 이메일"
}
```
**Response** `429`
```json
{
   "errorCode": "TOO_MANY_REQUESTS",
   "message": "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."
}
```
</details>
<details>
<summary>5. 제한 해제 후 재요청 (성공)</summary>

**Request**
```json
{
  "email": "DB에 저장되어 있는 이메일"
}
```
**Response** `200`
```json
{
   "message": "입력하신 이메일로 안내 메일을 전송했습니다."
}
```
</details>
<details>
<summary>6. 이전 토큰 무효화 확인</summary>

```
받은 메일 확인 후, token= 뒤에 문자가 매번 달라지는것 확인 
```
</details>
<details>
<summary>7. 메일 발송 시스템 오류</summary>

**Request**
```
잘못된 요청
```
**Response** `500`
```json
{
   "errorCode": "MAIL_SEND_FAILED",
   "message": "일시적인 오류로 메일 전송에 실패했습니다. 잠시 후 다시 시도해주세요."
}
```
</details>