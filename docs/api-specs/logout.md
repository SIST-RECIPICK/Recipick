# 로그아웃 (Logout)

## 1. 기능 개요

| 항목 | 내용 |
|---|---|
| 기능명 | 로그아웃 |
| 카테고리 | 인증 |
| 사용자 | 유저 |
| Method | POST |
| URL | `/auth/logout` |
| 설명 | Refresh Token 삭제 및 Access Token 블랙리스트 등록 후 로그아웃 처리한다 |

---

## 2. 요구사항 정의

### 동작 흐름

1. 사용자가 로그아웃 요청 (`POST /auth/logout`). Access Token은 Authorization 헤더로, Refresh Token은 httpOnly 쿠키로 자동 전달됨.
2. 서버: `@AuthenticationPrincipal JwtUser jwtUser`로 인증 여부 확인 (`JwtAuthenticationFilter`가 미리 검증·주입) — `jwtUser`가 null이면 인증되지 않은 요청으로 간주.
3. 서버: 쿠키로 전달된 Refresh Token 값을 키(`refresh:{refreshToken}`)로 사용해 Redis에서 삭제. 키가 없어도(중복 로그아웃 등) 에러 없이 진행 (멱등 처리).
4. 서버: Access Token을 블랙리스트에 등록 (블랙리스트 등록에는 원본 토큰 문자열이 필요하므로 Authorization 헤더에서 별도로 추출).
    - 4-1. Access Token 남은 유효기간만큼 TTL 설정해 `blacklist:{accessToken}` Redis 키 생성.
    - 4-2. TTL 동안 해당 Access Token으로의 접근 차단.
    - 4-3. TTL 만료 시 Redis 키 자동 삭제 (Access Token 자연 만료 시점과 일치). 이미 만료된 토큰으로 요청이 와도 정상 처리(성공)로 간주.
5. 서버: 응답 헤더에 `Set-Cookie: refreshToken=; Max-Age=0`으로 쿠키 만료 처리 (httpOnly라 클라이언트 JS가 직접 못 지우므로 서버가 처리).
6. 클라이언트: 응답 성공 시 Zustand의 accessToken 상태 초기화 (XSS 방어를 위해 accessToken은 localStorage가 아닌 Zustand 인메모리로 저장).
7. 클라이언트: 로그인 페이지로 이동.
> 참고: SecurityConfig의 `authorizeHttpRequests`는 그대로 `permitAll` 유지 (팀 정책상 개발 기간 중 전체 개방). 인증 여부는 컨트롤러에서 `jwtUser` null 체크로 판단.
---

## 3. API 명세

### Request

> 별도의 request body 없음. 인증 정보는 아래 두 곳에서 전달됨:
- **Header**: `Authorization: Bearer {accessToken}`
- **Cookie**: `refreshToken={refreshToken}` (httpOnly, 자동 전송)

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| Authorization | Access Token (Bearer) | String (Header) | 필수 | N | `Bearer eyJhbGciOiJIUzI1NiIs...` |

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| message | 처리 결과 메시지 | String | 필수 | N | `로그아웃되었습니다.` |

> 응답 헤더에 `Set-Cookie: refreshToken=; Max-Age=0; HttpOnly; Secure` 포함하여 클라이언트 쿠키 만료 처리

**Example**

```json
// 성공
{
  "message": "로그아웃되었습니다."
}

// 실패 - 인증되지 않은 요청 (Access Token 누락/무효)
{
  "errorCode": "UNAUTHORIZED",
  "message": "로그인이 필요합니다."
}
```

### Status

| status | response content |
|---|---|
| 200 | 로그아웃 성공 (Refresh Token 삭제 + Access Token 블랙리스트 등록 + 쿠키 만료) |
| 401 | 인증되지 않은 요청 (`jwtUser`가 null — Access Token 누락/무효) |

---

## 4. 구현 참고 (팀 컨벤션 반영)

### 계층 구조

```
com.sist.web.restcontroller.AuthRestController   ← POST /auth/logout
com.sist.web.service.AuthService / AuthServiceImpl
(Redis 직접 사용 - Mapper/VO 불필요)
```

> 이 API는 DB(Oracle)를 조회하지 않고 Redis만 사용하므로 Mapper/XML 작성이 필요 없습니다.

### 처리 로직 (Controller/Service 레이어)

1. `@AuthenticationPrincipal JwtUser jwtUser`로 인증 여부 확인 → null이면 `AuthException("UNAUTHORIZED", "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)` (401)
2. `@CookieValue(value = "refreshToken", required = false) String refreshToken`으로 쿠키 값 확보
3. Redis에서 `refresh:{refreshToken}` 키 삭제 (`DEL`) — 쿠키가 없거나 키가 없어도 정상 진행 (멱등 처리)
4. 블랙리스트 등록을 위해 `@RequestHeader("Authorization") String authHeader`로 원본 Access Token 문자열 확보 (`Bearer ` 접두사 제거)
5. Access Token을 블랙리스트에 등록: `SET blacklist:{accessToken} true EX {남은 유효기간(초)}` — 남은 유효기간은 토큰의 `exp` claim에서 계산
6. 응답 헤더에 만료된 `Set-Cookie` 포함해서 200 반환

### 예외 처리
- `AuthException("UNAUTHORIZED", ...)` → `AuthExceptionHandler`가 401로 응답 (인증 안 된 요청)

### 클라이언트(프론트) 처리 순서
1. `POST /auth/logout` 호출
2. 성공 시 Zustand `clearAccessToken()` 호출
3. Refresh Token 쿠키는 서버가 자동으로 만료시킴 (프론트가 별도 처리 불필요)
4. 로그인 페이지로 이동

---

## 5. 열린 이슈

없음

---

## 6. 테스트 기록

### 요약
| # | 케이스                                                                    | 상태  | errorCode    |
|---|------------------------------------------------------------------------|-----|--------------|
| 1 | 성공                                                                     | 200 | -            |
| 2 | 인증 없이 시도                                                               | 401 | UNAUTHORIZED |
| 3 | 같은 토큰 재사용                                                              | 401 | UNAUTHORIZED |
| 4 | Refresh Token 쿠키 없이 시도 (멱등성 보장)</br>사용자가 임의로 지워서 Access Token만 존재하는 경우 | 200 | -            |


- 테스트 도구: Swagger UI
- 테스트 일자: 2026-09-09
- 결과: 명세서와 100% 일치, 별도 수정 없음
### 상세

<details>
<summary>1. 성공</summary>

**Request**
```json
{
   "refreshToken": "쿠키의 RefreshToken 값",
   "Authorization": "Bearer eyJhbGciOiJIUzI..."
}
```
**Response** `200`
```json
{
  "message": "로그아웃되었습니다."
}
```
</details>
<details>
<summary>2. 인증 없이 시도</summary>

**Request**
```json
{
   "refreshToken": "",
   "Authorization": ""
}
```
**Response** `401`
```json
{
  "errorCode": "UNAUTHORIZED",
  "message": "로그인이 필요합니다."
}
```
</details>
<details>
<summary>3. 같은 토큰 재사용</summary>

**Request**
```json
{
  "refreshToken": "이미 로그아웃 한 쿠키의 RefreshToken 값",
  "Authorization": "이미 로그아웃 한 Bearer eyJhbGciOiJIUzI..."
}
```
**Response** `401`
```json
{
  "errorCode": "UNAUTHORIZED",
  "message": "로그인이 필요합니다."
}
```
</details>
<details>
<summary>4. Refresh Token 쿠키 없이 시도</summary>

**Request**
```json
{
  "refreshToken": "",
  "Authorization": "Bearer eyJhbGciOiJIUzI..."
}
```
**Response** `200`
```json
{
  "message": "로그아웃되었습니다."
}
```
</details>
