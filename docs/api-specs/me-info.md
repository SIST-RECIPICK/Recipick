# 현재 로그인 사용자 정보 조회 (Me)

## 1. 기능 개요

| 항목 | 내용 |
|---|---|
| 기능명 | 현재 로그인 사용자 정보 조회 |
| 카테고리 | 인증 |
| 사용자 | 로그인 유저 |
| Method | GET |
| URL | `/auth/me` |
| 설명 | Access Token으로 현재 로그인한 사용자의 기본 정보(userId, nickname, role)를 조회한다 |

---

## 2. 요구사항 정의

### 배경 (왜 필요한가)
- 프론트(Pinia)는 새로고침 시 클라이언트 상태가 전부 초기화됨
- 새로고침 후 `/auth/reissue`로 Access Token은 복구되지만, 그 응답엔 `accessToken`만 있고 사용자 정보(닉네임 등)가 없어 헤더 등 UI가 로그인 상태를 제대로 표시하지 못함
- 이 API로 재발급 직후 사용자 정보를 별도로 복구

### 동작 흐름
1. 새로고침 → `/auth/reissue`로 Access Token 재발급 성공
2. 프론트가 이어서 `GET /auth/me` 호출 (재발급받은 `accessToken`을 Authorization 헤더에 실어서)
3. 응답으로 받은 `userId`/`nickname`/`role`로 Pinia `user` 상태 채움 → 헤더 등 UI 정상 표시

### 활용처
- 새로고침 후 사용자 정보 복구 (1차 목적)
- 향후 마이페이지 등 "현재 로그인한 사용자 정보"가 필요한 다른 화면에서도 범용 재사용 가능

---

## 3. API 명세

### Request

> Body 없음.

| 위치 | key | 설명 | 필수 | 예시 |
|---|---|---|---|---|
| Header | Authorization | Access Token | Y | `Bearer eyJhbGciOiJIUzI1NiIs...` |

### Response

| key | 설명 | 타입 | 필수 | 예시 |
|---|---|---|---|---|
| userId | 사용자 PK | int | Y | `1024` |
| nickname | 닉네임 | String | Y | `안뇽가리` |
| role | 권한 | String | Y | `USER` / `ADMIN` |

**Example**

```json
// 성공
{
  "userId": 1024,
  "nickname": "안뇽가리",
  "role": "USER"
}

// 실패 - 인증되지 않은 요청 (Access Token 누락/무효/만료)
{
  "errorCode": "UNAUTHORIZED",
  "message": "로그인이 필요합니다."
}
```

### Status

| status | response content |
|---|---|
| 200 | 조회 성공 |
| 401 | Access Token 누락/무효/만료 (블랙리스트 등록된 토큰 포함) |

---

## 4. 구현 참고 (팀 컨벤션 반영)

### 계층 구조

```
com.sist.web.restcontroller.AuthRestController   ← GET /auth/me
com.sist.web.service.AuthService / AuthServiceImpl
com.sist.web.mapper.AuthMapper
com.sist.web.security.JwtUser
```

### 처리 로직 (Service 레이어)

1. `@AuthenticationPrincipal JwtUser jwtUser` null 체크 → null이면 `AuthException("UNAUTHORIZED", "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)` (401) — 기존 컨트롤러 패턴 재사용
2. `AuthMapper.findUserStatusById(jwtUser.getUserId())` 조회 (reissue.md/계정 복구에서 이미 `id`, `status`, `role`, `nickname`까지 반환하도록 확장된 쿼리 — **신규 SQL 불필요, 그대로 재사용**)
3. 조회된 `nickname`, `role`과 `jwtUser.getUserId()`를 응답으로 반환

> 참고: `role`은 JWT 클레임에도 이미 들어있어 `jwtUser.getRole()`로 바로 꺼낼 수도 있지만, DB 조회 결과(`user.getRole()`)를 최종 소스로 사용하는 쪽으로 통일합니다 — 토큰 발급 이후 값이 바뀔 가능성을 열어두고 DB를 최신 진실 공급원(source of truth)으로 삼는 편이 안전합니다. 이번 스콥에서 권한 변경 기능 자체가 없어 실질적 차이는 없지만, 일관성 차원에서 이렇게 갑니다.

### 예외 처리
- `AuthException` → `AuthExceptionHandler`가 `{ errorCode, message }`로 응답 (401)

### Mapper 예시 (SQL)

```xml
<!-- findUserStatusById는 reissue.md/계정 복구 작업에서 이미 nickname까지 포함해 정의됨 - 그대로 재사용, 신규 쿼리 없음 -->
```

---

## 5. 열린 이슈

- [ ] (참고, 블로킹 아님) 계정이 WITHDRAWN으로 전환된 후에도, 아직 블랙리스트에 등록되지 않은 다른 기기의 Access Token(최대 30분 잔여)으로는 이 API가 여전히 정상 응답할 수 있음. 이건 reissue가 이미 갖고 있는 "재발급 시점 계정 상태 재확인" 로직으로 다음 재발급 때 자연스럽게 걸러지므로, `/auth/me` 자체에 별도 상태 체크를 추가할 필요는 없다고 판단 — 필요 시 추후 논의.