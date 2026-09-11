# 계정 복구

## 1. 기능 개요

| 항목 | 내용 |
|---|---|
| 기능명 | 계정 복구 |
| 카테고리 | 인증 |
| 사용자 | 소프트탈퇴 계정 소유자 (일반/소셜 공통) |
| Method | POST |
| URL | `/auth/recover` |
| 설명 | 소프트탈퇴 계정을 복구하고 토큰을 발급한다 |

---

## 2. 요구사항 정의

### 화면 요소
- [복구하기], [취소]

### 동작 흐름

1. 일반/소셜 로그인 시도 시 소프트탈퇴 계정임을 감지 (login API에서 `recoveryToken` 발급, TTL 5분)
2. 복구 여부 팝업 안내
    - 2-1. [복구하기] 클릭 → `POST /auth/recover` 요청
        - 2-1-1. `recoveryToken` 유효성 재검증 (Redis 조회) 및 계정 상태 재확인
        - 2-1-2. 사용자 `status`를 `ACTIVE`로 변경, `withdrawn_at`을 `NULL`로 초기화 → 이 상태 변경만으로 하드탈퇴 배치의 대상 조건(`status = WITHDRAWN`)에서 자동으로 제외됨 (별도의 "취소" 작업이 존재하는 것이 아님)
        - 2-1-3. ~~사용자가 작성한 데이터들의 `is_visible`을 `true`로 복원~~ → **이번 스콥 제외.** 회원탈퇴(소프트) 기능에서 `is_visible` 처리 자체를 스콥 제외했으므로(현재 `recipes` 테이블에 해당 컬럼 없음, 레시피 도메인 팀원과 협의 필요), 복원할 대상도 없다. 해당 협의가 끝나면 소프트탈퇴/복구 양쪽에 함께 반영한다.
        - 2-1-4. Access Token(JWT, body) + Refresh Token(UUID, httpOnly Secure Cookie) 신규 발급 및 저장
        - 2-1-5. 메인 페이지 이동
        - 2-1-6. "계정이 복구되었습니다." 안내
    - 2-2. [취소] 클릭 → 로그인 처리하지 않고 로그인 페이지 유지 (별도 API 호출 없음, 프론트 처리로 충분)

> 원본 요구사항 대비 추가/확정된 부분:
> - **응답 필드 보강**: 복구 성공은 사실상 로그인 성공과 동일한 상태이므로, 클라이언트 상태(Zustand) 구성에 필요한 `accountStatus`, `role`을 login.md 성공 응답과 동일하게 포함한다.
> - **Refresh Token 발급 명시**: 요구사항엔 있었으나 API 명세에 누락되어 있던 Refresh Token 발급을, login.md와 동일한 방식(Set-Cookie, httpOnly, Secure)으로 명시한다.
> - **소셜/일반 공통 처리 그대로 유효**: `recoveryToken → userId` 매핑은 계정 유형과 무관하게 동작하므로, 소셜 로그인이 아직 구현되지 않았어도 이 API 자체에는 별도 분기가 필요 없다.
> - **`recoveryToken` 파라미터 누락 케이스 추가**: 기존 Status 표에 없던 400 케이스를 추가한다.

---

## 3. API 명세

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| recoveryToken | 로그인 시 발급된 복구용 토큰 | String | 필수 | N | `f3a9c1e2-...` |

### Response

> Refresh Token은 응답 body에 포함하지 않고 `Set-Cookie` 헤더로 `httpOnly`, `Secure` 쿠키에 담아 전달합니다. (login.md와 동일)

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| accessToken | 발급된 Access Token | String | 필수 (성공 시) | Y | `eyJhbGciOiJIUzI1NiIs...` |
| accountStatus | 계정 상태 (복구 성공 시 항상 ACTIVE) | String | 필수 (성공 시) | Y | `ACTIVE` |
| userId | 복구된 사용자 PK | Long | 필수 (성공 시) | Y | `1024` |
| nickname | 닉네임 | String | 필수 (성공 시) | Y | `안뇽가리` |
| role | 권한 | String | 필수 (성공 시) | Y | `USER` |
| message | 처리 결과 메시지 | String | 필수 | N | `계정이 복구되었습니다.` |

**Example**

```json
// 성공
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "accountStatus": "ACTIVE",
  "userId": 1024,
  "nickname": "솜귤",
  "role": "USER",
  "message": "계정이 복구되었습니다."
}

// 실패 - recoveryToken 파라미터 누락
{
  "errorCode": "MISSING_RECOVERY_TOKEN",
  "message": "잘못된 접근입니다."
}

// 실패 - 토큰 만료/무효 (계정 상태가 이미 다른 경우 포함)
{
  "errorCode": "INVALID_RECOVERY_TOKEN",
  "message": "복구 요청이 만료되었습니다. 다시 로그인해주세요."
}
```

### Status

| status | response content |
|---|---|
| 200 | 계정 복구 성공 (Access/Refresh Token 발급 포함) |
| 400 | `recoveryToken` 파라미터 누락 |
| 410 | `recoveryToken` 만료 또는 유효하지 않음 (계정 상태가 이미 WITHDRAWN이 아닌 경우 포함) |

---

## 4. 구현 참고 (팀 컨벤션 반영)

### 계층 구조

```
com.sist.web.restcontroller.AuthRestController   ← POST /auth/recover
com.sist.web.service.AuthService / AuthServiceImpl
com.sist.web.mapper.AuthMapper
resources/mybatis/mapper/auth-mapper.xml
com.sist.web.vo.UsersVO
com.sist.web.security.JwtTokenProvider
```

### Redis 키

| 키 패턴 | 값 | TTL | 용도 |
|---|---|---|---|
| `recovery:{recoveryToken}` | `userId` | 5분 | login.md에서 이미 발급하는 복구용 토큰 (기존 구현의 실제 키 패턴과 일치하는지 확인 필요) |

### 처리 로직 (Service 레이어)

1. `recoveryToken` 파라미터 존재 확인 → 없으면 `AuthException("MISSING_RECOVERY_TOKEN", ...)` (400)
2. Redis `recovery:{recoveryToken}` 조회 → 값(`userId`) 확인
    - 키 없음(만료/무효) → `AuthException("INVALID_RECOVERY_TOKEN", ...)` (410)
3. 조회된 `userId`로 `AuthMapper.findUserStatusById(userId)` 재조회 (reissue.md에서 정의한 쿼리 재사용)
    - `status != WITHDRAWN` (이미 복구됐거나 다른 상태로 바뀐 경우) → `AuthException("INVALID_RECOVERY_TOKEN", ...)` (410) — 상태를 구체적으로 노출하지 않고 동일 에러로 통일
    - `status == WITHDRAWN` → 다음 단계 진행
4. `AuthMapper.recoverUser(userId)` 실행 → `status = 'ACTIVE'`, `withdrawn_at = NULL`
5. Redis `recovery:{recoveryToken}` 삭제 (1회용 소비)
6. 신규 Access Token 발급(JWT, `sub`=userId, `role` claim 포함) + 신규 Refresh Token 발급(UUID) → Redis `refresh:{token}` → `userId` 저장 (TTL 14일)
7. 응답: Access Token은 body, Refresh Token은 `Set-Cookie`(httpOnly, Secure)로 전달 + `{ accessToken, accountStatus: "ACTIVE", userId, nickname, role, message }` 200 응답

### 예외 처리
- `AuthException` → `AuthExceptionHandler`가 `{ errorCode, message }`로 응답
- `MISSING_RECOVERY_TOKEN` → 400, `INVALID_RECOVERY_TOKEN` → 410

### Mapper 예시 (SQL)

```xml
<update id="recoverUser" parameterType="long">
    UPDATE users
    SET status = 'ACTIVE',
        withdrawn_at = NULL
    WHERE id = #{id}
</update>

<!-- findUserStatusById는 reissue.md에서 이미 정의됨 - 재사용 -->
```

---

## 5. 열린 이슈

1. **`is_visible` 복원 — 이번 스콥 제외 (withdraw-soft.md와 동일 사유).** 레시피 담당 팀원과 컬럼/처리 방식 협의가 끝나면 소프트탈퇴의 `is_visible=false` 처리와 함께 이 API의 복원 로직도 같이 추가한다.
2. **소셜 계정 복구 실제 테스트는 소셜 로그인 구현 이후 가능.** API 로직 자체는 계정 유형과 무관하게 동작하도록 설계했으나, 소셜 로그인 미구현으로 현재는 일반 계정 기준으로만 테스트 가능하다.

---

## 6. 테스트 기록

### 요약
| # | 케이스                            | 상태  | errorCode          |
|---|--------------------------------|-----|--------------------|
| 1 | 정상 복구                          | 200 | -                  |
| 2 | recoveryToken 파라미터 누락          | 400 | MISSING_RECOVERY_TOKEN  |
| 3 | 존재하지 않는/조작된 토큰                 | 410 | INVALID_RECOVERY_TOKEN |
| 4 | 만료된 토큰                         | 410 | INVALID_RECOVERY_TOKEN |
| 5 | 이미 소비된 토큰 재사용(status = ACTIVE) | 410 | INVALID_RECOVERY_TOKEN    |

- 테스트 도구: Swagger UI
- 테스트 일자: 2026-09-11
-
### 상세

<details>
<summary>1. 정상 복구</summary>

**Request**
```json
{
   "recoveryToken": "f6a62b41-c9fe-41e8-a1c2-1c933325b09f"
}
```
**Response** `200`
```json
{
   "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMDAzIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3ODkxMTAyNzEsImV4cCI6MTc4OTExMjA3MX0.qmO4FKkhsRX-yzMvmjYt9XuSywCJI5P1vHqWJG0KJR8",
   "accountStatus": "ACTIVE",
   "message": "계정이 복구되었습니다.",
   "nickname": "안녕하세여",
   "recoveryToken": null,
   "role": "USER",
   "userId": 1003
}
```
</details>
<details>
<summary>2. recoveryToken 파라미터 누락</summary>

**Request**
```json
{
   "recoveryToken": ""
}
```
**Response** `400`
```json
{
   "errorCode": "MISSING_RECOVERY_TOKEN",
   "message": "잘못된 접근입니다."
}
```
</details>
<details>
<summary>3. 존재하지 않는/조작된 토큰</summary>

**Request**
```json
{
   "recoveryToken": "존재하지 않는 토큰"
}
```
**Response** `410`
```json
{
   "errorCode": "INVALID_RECOVERY_TOKEN",
   "message": "복구 요청이 만료되었습니다. 다시 로그인해주세요."
}
```
</details>
<details>
<summary>4. 만료된 토큰</summary>

**Request**
```json
{
   "recoveryToken": "만료된 토큰"
}
```
**Response** `410`
```json
{
   "errorCode": "INVALID_RECOVERY_TOKEN",
   "message": "복구 요청이 만료되었습니다. 다시 로그인해주세요."
}
```
</details>
<details>
<summary>5. 이미 소비된 토큰 재사용</summary>

**Request**
```json
{
   "recoveryToken": "처음에 정상 복구에서 사용한 토큰"
}
```
**Response** `410`
```json
{
   "errorCode": "INVALID_RECOVERY_TOKEN",
   "message": "복구 요청이 만료되었습니다. 다시 로그인해주세요."
}
```
</details>