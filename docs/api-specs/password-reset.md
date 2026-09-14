# 비밀번호 재설정

## 1. 기능 개요

| 항목 | 내용 |
|---|---|
| 기능명 | 비밀번호 재설정 |
| 카테고리 | 인증 |
| 사용자 | 비로그인 유저 (이메일 링크로 진입) |
| Method | POST |
| URL | `/auth/password/reset` |
| 설명 | 유효한 Token으로 새 비밀번호를 설정한다 |

---

## 2. 요구사항 정의

### 화면 요소
- 새 비밀번호, 새 비밀번호 확인, URL의 유효 관리 Token, [비밀번호 변경]

### 동작 흐름

1. 사용자가 메일에 도착한 재설정 링크 클릭
2. 비밀번호 재설정 페이지로 이동
3. URL에서 Token 파라미터 추출하여 유효성 검증 ("비밀번호 재설정 링크 유효성 검증" API 호출)
4. 유효한 경우 (= Redis에 Token 존재 + 계정 ACTIVE)
    - 4-1. 새 비밀번호 / 새 비밀번호 확인 입력
    - 4-2. [비밀번호 변경] 클릭 → `POST /auth/password/reset` 요청
    - 4-3. 서버에서 Token으로 Redis 재조회하여 userId 확인 (제출 시점에 다시 한번 검증)
    - 4-4. 비밀번호 형식 및 확인 일치 검증
    - 4-5. userId 사용자 비밀번호 업데이트 후 Redis의 Token(및 역인덱스) 삭제
    - 4-6. "비밀번호가 변경되었습니다" 안내 → 로그인 페이지로 이동
5. 무효 (= Redis에 Token 없음, 만료, 또는 계정이 WITHDRAWN으로 전환됨)
    - 5-1. "유효하지 않거나 만료된 링크입니다" 안내

> 원본 요구사항 대비 추가/확정된 부분:
> - **제출 시점 재검증**: 페이지 진입 시 검증(validate API)과 별개로, 실제 [비밀번호 변경] 제출 시에도 Token을 다시 조회한다. 페이지에 머무르는 동안 시간이 지나 만료되었거나, 다른 탭/중복 제출로 이미 소비된 경우를 방지하기 위함.
> - **계정 상태(WITHDRAWN) 재확인 추가**: "비밀번호 재설정 링크 유효성 검증" API와 동일하게, Token은 유효하더라도 그 사이 계정이 탈퇴되었다면 비밀번호 변경을 거부한다.
> - **다른 기기 세션(Refresh Token) 전체 무효화는 이번 스콥에서 제외.** (아래 열린 이슈 참고)
> - Token 사용 완료 시 `pwReset:{token}` 뿐 아니라, 비밀번호 찾기(재설정 링크 요청) API에서 만든 역인덱스 `pwResetUser:{userId}`도 함께 정리한다.

---

## 3. API 명세

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| token | 재설정 URL에 포함된 토큰 | String | 필수 | N | `a1b2c3d4-...` |
| newPassword | 새 비밀번호 (영문+숫자+특수문자, 8~20자) | String | 필수 | N | `NewPass123!` |
| newPasswordConfirm | 새 비밀번호 확인 | String | 필수 | N | `NewPass123!` |

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| message | 처리 결과 메시지 | String | 필수 | N | `비밀번호가 변경되었습니다.` |

**Example**

```json
// 성공
{
  "message": "비밀번호가 변경되었습니다."
}

// 실패 - 토큰 무효/만료 (재검증 결과, 계정 WITHDRAWN 포함)
{
  "errorCode": "INVALID_OR_EXPIRED_TOKEN",
  "message": "유효하지 않거나 만료된 링크입니다."
}

// 실패 - 새 비밀번호 확인 불일치
{
  "errorCode": "PASSWORD_MISMATCH",
  "message": "비밀번호가 일치하지 않습니다."
}

// 실패 - 비밀번호 형식 오류
{
  "errorCode": "INVALID_PASSWORD_FORMAT",
  "message": "비밀번호는 문자, 숫자, 특수기호를 모두 포함해 8~20자로 입력해주세요."
}
```

### Status

| status | response content |
|---|---|
| 200 | 비밀번호 변경 성공 (Redis Token 및 역인덱스 삭제 포함) |
| 400 | 비밀번호 형식 오류, 비밀번호 불일치, 필수값 누락 |
| 410 | 토큰이 유효하지 않거나 만료됨 (이미 사용됨/시간 초과/계정이 WITHDRAWN으로 전환됨 포함) |

---

## 4. 구현 참고 (팀 컨벤션 반영)

### 계층 구조

```
com.sist.web.restcontroller.AuthRestController   ← POST /auth/password/reset
com.sist.web.service.AuthService / AuthServiceImpl
com.sist.web.mapper.AuthMapper
resources/mybatis/mapper/auth-mapper.xml
com.sist.web.vo.UsersVO
```

### 처리 로직 (Service 레이어)

1. 필수값 존재 확인 (`token`, `newPassword`, `newPasswordConfirm`) → 누락 시 `AuthException("INVALID_REQUEST", ...)` (400)
2. 비밀번호 형식 검증 (정규식: 영문+숫자+특수문자 포함 8~20자) → 실패 시 `AuthException("INVALID_PASSWORD_FORMAT", ...)` (400)
3. `newPassword`와 `newPasswordConfirm` 일치 확인 → 불일치 시 `AuthException("PASSWORD_MISMATCH", ...)` (400)
4. Redis `pwReset:{token}` 조회 → 값(`userId`) 확인
    - 키 없음(만료/무효/이미 사용됨) → `AuthException("INVALID_OR_EXPIRED_TOKEN", ...)` (410)
5. 조회된 `userId`로 `AuthMapper.findUserStatusById(userId)` 조회 (reissue.md에서 정의한 쿼리 재사용)
    - `status == WITHDRAWN` → Redis `pwReset:{token}`, `pwResetUser:{userId}` 삭제(정리) → `AuthException("INVALID_OR_EXPIRED_TOKEN", ...)` (410) (탈퇴 여부를 별도로 노출하지 않고 동일한 에러로 통일)
    - `status == ACTIVE` → 다음 단계 진행
6. `PasswordEncoder.encode(newPassword)` → `AuthMapper.updatePassword(userId, encodedPassword)` 실행
7. Redis `pwReset:{token}`, `pwResetUser:{userId}` 삭제
8. `{ "message": "비밀번호가 변경되었습니다." }` 200 응답

### 예외 처리
- `AuthException` → `AuthExceptionHandler`가 `{ errorCode, message }`로 응답
- `INVALID_PASSWORD_FORMAT` / `PASSWORD_MISMATCH` / 필수값 누락 → 400, `INVALID_OR_EXPIRED_TOKEN` → 410

### Mapper 예시 (SQL)

```xml
<update id="updatePassword" parameterType="map">
    UPDATE local_accounts
    SET password = #{password}
    WHERE user_id = #{user_id}
</update>

<!-- findUserStatusById는 reissue.md에서 이미 정의됨 - 재사용 -->
```

---

## 5. 열린 이슈

1. **다른 기기 세션(Refresh Token) 전체 무효화 — 이번 스콥 제외로 확정.** 비밀번호 재설정 시 다른 기기에 이미 로그인되어 있던 세션(Refresh Token)은 자연 만료(최대 14일, Rolling)까지 유지된다. 이를 구현하려면 Refresh Token을 `userId` 기준으로 역조회할 수 있는 별도 인덱스(예: `refreshSet:{userId}` → 활성 토큰 Set)를 새로 설계해야 하며, 이는 이미 완료·테스트된 로그인/로그아웃/재발급 API를 소급 수정해야 하는 작업이라 이번 스콥에서는 제외한다. 향후 보안 강화 시점에 CLAUDE.md의 기술 부채 목록에 추가하여 재논의한다.

---

## 6. 테스트 기록

### 요약
| # | 케이스                         | 상태  | errorCode    |
|---|-----------------------------|-----|--------------|
| 1 | 성공                          | 200 | -            |
| 2 | 필수값 누락                      | 400 | INVALID_REQUEST |
| 3 | 비밀번호 형식 오류                  | 400 | INVALID_PASSWORD_FORMAT |
| 4 | 비밀번호 확인 불일치                 | 400 | PASSWORD_MISMATCH   |
| 5 | 존재하지 않는 토큰                  | 410 | INVALID_OR_EXPIRED_TOKEN |
| 6 | 이미 소비된 토큰 재사용               | 410 | INVALID_OR_EXPIRED_TOKEN  |
| 7 | WITHDRAWN (탈퇴) 계정           | 410 |  INVALID_OR_EXPIRED_TOKEN  |
| 8 | 실제 비밀번호 변경 확인                      | 200 | -            |

- 테스트 도구: Swagger UI
- 테스트 일자: 2026-09-10

### 상세
<details>
<summary>1. 성공</summary>

**Request**
```json
{
   "token": "171b620e-6965-40af-bda7-4484f11d545f",
   "newPassword": "password1234!",
   "newPasswordConfirm": "password1234!"
}
```
**Response** `200`
```json
{
   "message": "비밀번호가 변경되었습니다."
}
```
</details>
<details>
<summary>2. 필수값 누락</summary>

**Request**
```json
{
   "token": "",
   "newPassword": "password1234!",
   "newPasswordConfirm": "password1234!"
}
```
**Response** `400`
```json
{
   "errorCode": "INVALID_REQUEST",
   "message": "잘못된 요청입니다."
}
```
</details>
<details>
<summary>3. 비밀번호 형식 오류</summary>

**Request**
```json
{
   "token": "171b620e-6965-40af-bda7-4484f11d545f",
   "newPassword": "password123",
   "newPasswordConfirm": "password123"
}
```
**Response** `400`
```json
{
   "errorCode": "INVALID_PASSWORD_FORMAT",
   "message": "비밀번호는 문자, 숫자, 특수기호를 모두 포함해 8~20자로 입력해주세요."
}
```
</details>
<details>
<summary>4. 비밀번호 확인 불일치</summary>

**Request**
```json
{
   "token": "171b620e-6965-40af-bda7-4484f11d545f",
   "newPassword": "password1234!",
   "newPasswordConfirm": "password123!"
}
```
**Response** `400`
```json
{
   "errorCode": "PASSWORD_MISMATCH",
   "message": "비밀번호가 일치하지 않습니다."
}
```
</details>
<details>
<summary>5. 존재하지 않는 토큰</summary>

**Request**
```json
{
   "token": "aslkdjfla아무거나f",
   "newPassword": "password1234!",
   "newPasswordConfirm": "password1234!"
}
```
**Response** `410`
```json
{
   "errorCode": "INVALID_OR_EXPIRED_TOKEN",
   "message": "유효하지 않거나 만료된 링크입니다."
}
```
</details>
<details>
<summary>6. 이미 소비된 토큰 재사용</summary>

**Request**
```json
{
   "token": "171b620e-6965-40af-bda7-4484f11d545f(1번에서 이미 사용된 토큰)",
   "newPassword": "password1234!",
   "newPasswordConfirm": "password1234!"
}
```
**Response** `410`
```json
{
   "errorCode": "INVALID_OR_EXPIRED_TOKEN",
   "message": "유효하지 않거나 만료된 링크입니다."
}
```
</details>
<details>
<summary>7. WITHDRAWN (탈퇴) 계정</summary>

**Request**
```json
{
   "token": "e6128a0f-8bc9-42d4-b8dd-94a0237cfb82(메일 요청 후 탈퇴함)",
   "newPassword": "password1234!",
   "newPasswordConfirm": "password1234!"
}
```
**Response** `410`
```json
{
   "errorCode": "INVALID_OR_EXPIRED_TOKEN",
   "message": "유효하지 않거나 만료된 링크입니다."
}
```
</details>
<details>
<summary>8. 실제 비밀번호 변경 확인</summary>

**Request**
```json
{
   "email": "rmawl8600@naver.com",
   "password": "password1234!"
}
```
**Response** `200`
```json
{
   "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMDAzIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3ODkwMjg4NzcsImV4cCI6MTc4OTAzMDY3N30.5cCXfRYO6-a25LZUZmjc1qf7gOxMGidHIybLKxL1Ug0",
   "accountStatus": "ACTIVE",
   "message": null,
   "nickname": "안녕하세여",
   "recoveryToken": null,
   "role": "USER",
   "userId": 1003
}
```
</details>
