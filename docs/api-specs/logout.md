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

## 2. 요구사항 정의 (개선본)

### 동작 흐름

1. 사용자 로그아웃 요청 (`POST /auth/logout`, Access Token은 Authorization 헤더로 전달)
2. 서버: Access Token에서 userId 추출
3. 서버: Redis에 저장된 해당 userId의 Refresh Token 삭제 (없어도 에러 없이 진행 — 멱등 처리)
4. 서버: Access Token을 블랙리스트에 등록
    - 4-1. Access Token 남은 유효기간만큼 TTL 설정하여 Redis 키 생성
    - 4-2. TTL 동안 해당 Access Token으로의 접근 차단
    - 4-3. TTL 만료 시 Redis 키 자동 삭제 (Access Token도 자연 만료되는 시점과 일치)
5. 서버: 응답 헤더에 `Set-Cookie`로 Refresh Token 쿠키 만료 처리 (`Max-Age=0`)
6. 클라이언트: 응답 성공 시 Zustand의 accessToken 상태 초기화
7. 클라이언트: 로그인 페이지로 이동

> 원본 요구사항 대비 명확화된 부분:
> - Access Token 저장 방식은 (초기 논의된) localStorage가 아니라 **Zustand 인메모리 스토어**로 확정 — XSS 방어 목적
> - Refresh Token은 httpOnly 쿠키라 클라이언트 JS가 직접 지울 수 없으므로, 서버가 `Set-Cookie` 응답 헤더로 만료시켜야 함
> - Access Token이 이미 만료된 상태로 로그아웃 요청이 와도 정상 처리(성공)로 간주
> - Redis에 Refresh Token이 이미 없는 상태(중복 로그아웃)여도 에러 없이 성공 처리 (멱등성)

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

// 실패 - Access Token 누락/형식 오류
{
  "errorCode": "INVALID_TOKEN",
  "message": "인증 정보가 유효하지 않습니다."
}
```

### Status

| status | response content |
|---|---|
| 200 | 로그아웃 성공 (Refresh Token 삭제 + Access Token 블랙리스트 등록 + 쿠키 만료) |
| 401 | Authorization 헤더 누락 또는 Access Token 형식 오류 |

---

## 4. 구현 참고 (팀 컨벤션 반영)

### 계층 구조

```
com.sist.web.restcontroller.AuthRestController   ← POST /auth/logout
com.sist.web.service.AuthService / AuthServiceImpl
(Redis 직접 사용 - Mapper/VO 불필요)
```

> 이 API는 DB(Oracle)를 조회하지 않고 Redis만 사용하므로 Mapper/XML 작성이 필요 없습니다.

### 처리 로직 (Service 레이어)

1. Authorization 헤더에서 Access Token 추출 (`Bearer ` 접두사 제거) → 형식 오류/누락 시 `AuthException("INVALID_TOKEN", ...)` (401)
2. JWT 파싱하여 userId(claim) 확보 — 이미 만료된 토큰이어도 서명 검증만 통과하면 claim 추출 가능하도록 파싱 로직 구성 (만료 예외와 서명 위조 예외를 구분 처리)
3. Redis에서 `refresh:{userId}` 키 삭제 (`DEL`) — 키가 없어도 정상 진행
4. Access Token을 블랙리스트에 등록: `SET blacklist:{accessToken} true EX {남은 유효기간(초)}`
5. 응답 헤더에 만료된 `Set-Cookie` 포함해서 200 반환

### 예외 처리
- `AuthException("INVALID_TOKEN", ...)` → `AuthExceptionHandler`가 401로 응답

### 클라이언트(프론트) 처리 순서
1. `POST /auth/logout` 호출
2. 성공 시 Zustand `clearAccessToken()` 호출
3. Refresh Token 쿠키는 서버가 자동으로 만료시킴 (프론트가 별도 처리 불필요)
4. 로그인 페이지로 이동

---

## 5. 열린 이슈

없음