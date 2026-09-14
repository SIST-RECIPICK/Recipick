# 토큰 재발급 (Reissue)

## 1. 기능 개요

| 항목 | 내용 |
|---|---|
| 기능명 | 토큰 재발급 |
| 카테고리 | 인증 |
| 사용자 | 유저 (로그인 상태) |
| Method | POST |
| URL | `/auth/reissue` |
| 설명 | Refresh Token을 검증하여 Access Token과 Refresh Token을 모두 재발급한다 (Rotation) |

---

## 2. 요구사항 정의 (개선본)

### 클라이언트(프론트) 동작 흐름

1. 새로고침 또는 API 요청 시 토큰 상태 확인
2. 재발급 분기 처리
    - 2-1. Access Token 있음 (만료 임박 아님) → 재발급 X
    - 2-2. Access Token 있지만 만료 임박 → 재발급 O
    - 2-3. Access Token 없음 → 재발급 O
3. 재발급 시도
    - 3-1. 동시에 여러 재발급 요청이 발생하지 않도록 단일 요청으로 처리 (밀린 요청은 큐에 저장 후 순차 처리)
    - 3-2. Refresh Token(httpOnly 쿠키)으로 `POST /auth/reissue` 요청

### 서버 처리 흐름

4. Refresh Token 유효성 검증 (Redis 조회: `refresh:{refreshToken}` → `userId`)
    - 4-1. 존재하지 않음(만료/무효/이미 사용되어 삭제됨) → 재발급 실패, 클라이언트 자동 로그아웃
    - 4-2. 존재함 → 매핑된 `userId`로 계정 상태 재조회
        - 4-2-1. **계정이 WITHDRAWN 상태로 전환된 경우** → 재발급 거부, 해당 Refresh Token 즉시 폐기(Redis 삭제), 클라이언트 강제 로그아웃 (재로그인 시 비밀번호 재검증을 거쳐야만 recoveryToken이 발급되므로, reissue 단계에서는 recoveryToken을 발급하지 않음)
        - 4-2-2. **계정 ACTIVE** → 기존 Refresh Token(Redis) 삭제 후 Access/Refresh Token 모두 신규 발급 (Rotation, TTL 14일로 재설정) → 클라이언트는 큐에 쌓인 밀린 요청 처리

> 원본 요구사항 대비 추가/명확화 및 확정된 부분:
> - **Refresh Token 조회 결과가 곧 유효성 검증이다.** 별도의 "유효/무효" 판단 로직이 있는 게 아니라, Redis에 해당 토큰 키가 존재하는지 여부로 판단한다(Rotation 방식이므로 한 번 사용된 토큰은 즉시 삭제되어 재사용 자체가 불가능).
> - **TTL 정책: Rolling 방식으로 확정.** 재발급마다 TTL을 14일로 재설정한다 (활동이 계속되는 한 세션이 유지됨).
> - **계정 상태 재검증 추가**: Refresh Token 자체는 유효하더라도, 그 사이 사용자가 소프트탈퇴(WITHDRAWN)했을 수 있으므로 재발급 시점에 계정 상태를 다시 확인한다.
> - **WITHDRAWN 발견 시 처리: 단순 강제 로그아웃으로 확정.** recoveryToken은 로그인 API에서 비밀번호 재검증(본인 확인)을 통과한 경우에만 발급하는 것이 원래 설계 의도이므로, 비밀번호 검증이 없는 reissue 단계에서는 recoveryToken을 발급하지 않는다. 복구 흐름은 재로그인을 통해서만 진입한다.
> - **동시 요청(3-1) 관련**: 프론트엔드 큐잉/단일 요청 처리는 UX 및 불필요한 요청 감소 목적이며, 백엔드 관점에서는 Rotation 방식 자체가 동시성 문제를 방지한다(아래 구현 참고 4번 항목 참고). 따라서 백엔드에 별도의 동시성 제어(락 등)는 필요하지 않다.
> - **Refresh Token 재사용(탈취) 탐지: 이번 스콥에서 제외로 확정.** 이미 사용되어 삭제된 토큰으로 재요청이 오는 경우도 단순히 `INVALID_REFRESH_TOKEN`으로 처리하며, 전체 세션 강제 폐기 등 별도 대응은 하지 않는다.
> - 관리자 정지 등 별도 계정 상태는 이번 스콥에 없음 (`ACTIVE`/`WITHDRAWN` 두 가지만 고려)

---

## 3. API 명세

### Request

> 별도의 Request Body가 없습니다. Refresh Token은 `httpOnly`, `Secure` 쿠키에 담겨 요청 시 자동으로 전송되며, 서버에서 쿠키 값을 추출하여 사용합니다.

| 위치 | key | 설명 | value 타입 | 옵션 | 예시 |
|---|---|---|---|---|---|
| Cookie | refreshToken | 재발급용 Refresh Token | String | 필수 | (자동 전송, 클라이언트에서 값 직접 확인 불가) |

### Response

> 새로 발급된 Refresh Token은 응답 body에 포함하지 않고 `Set-Cookie` 헤더로 기존 쿠키를 교체합니다.

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| accessToken | 새로 발급된 Access Token | String | 필수 | N | `eyJhbGciOiJIUzI1NiIs...` |

**Example**

```json
// 성공
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs..."
}

// 실패 - Refresh Token 없음/만료/무효(이미 사용됨)
{
  "errorCode": "INVALID_REFRESH_TOKEN",
  "message": "세션이 만료되었습니다. 다시 로그인해주세요."
}

// 실패 - Refresh Token은 유효하나 계정이 탈퇴 상태로 전환됨
{
  "errorCode": "ACCOUNT_WITHDRAWN",
  "message": "탈퇴한 계정입니다. 다시 로그인해주세요."
}
```

### Status

| status | response content |
|---|---|
| 200 | 재발급 성공 (Access Token은 body, 새 Refresh Token은 Set-Cookie로 교체, TTL 14일로 재설정) |
| 401 | Refresh Token 없음, 만료, 또는 무효 (Redis에 존재하지 않음) → 프론트: 자동 로그아웃 |
| 401 | Refresh Token은 유효하나 계정이 WITHDRAWN 상태 → 프론트: 자동 로그아웃 (재로그인 시 비밀번호 검증 통과하면 복구 절차 안내) |

---

## 4. 구현 참고 (팀 컨벤션 반영)

### 계층 구조

```
com.sist.web.restcontroller.AuthRestController   ← POST /auth/reissue
com.sist.web.service.AuthService / AuthServiceImpl
com.sist.web.mapper.AuthMapper
resources/mybatis/mapper/auth-mapper.xml
com.sist.web.vo.UsersVO
com.sist.web.security.JwtTokenProvider
```

### 처리 로직 (Service 레이어)

1. 쿠키에서 `refreshToken` 추출
    - 쿠키 자체가 없음 → `AuthException("INVALID_REFRESH_TOKEN", ...)` (401)
2. Redis에서 `refresh:{refreshToken}` 키 조회 → 값(`userId`) 확인
    - 키 없음(만료/무효/이미 사용되어 삭제됨) → `AuthException("INVALID_REFRESH_TOKEN", ...)` (401)
3. 조회된 `userId`로 `AuthMapper.findUserStatusById(userId)` 재조회
    - `status == WITHDRAWN` → Redis에서 `refresh:{refreshToken}` 즉시 삭제(세션 무효화) → `AuthException("ACCOUNT_WITHDRAWN", ...)` (401)
    - `status == ACTIVE` → 다음 단계 진행
4. 기존 Refresh Token 삭제: Redis `DEL refresh:{refreshToken}` (Rotation)
    - **동시성 참고**: 동일한 Refresh Token으로 요청이 동시에 여러 개 들어와도, Redis 삭제는 원자적으로 처리되므로 먼저 도착한 요청만 정상 진행되고 이후 요청들은 2번 단계에서 자연스럽게 `INVALID_REFRESH_TOKEN`으로 실패한다. 별도의 분산 락이 필요하지 않다.
5. 신규 Access Token 발급 (`JwtTokenProvider`, `sub`=userId, `role` claim 포함, 만료 30분)
6. 신규 Refresh Token 발급 (UUID) → Redis에 `refresh:{new refreshToken}` → `userId` 저장 (TTL 14일, Rolling 방식이므로 매 재발급마다 새로 설정)
7. 응답: Access Token은 body로, 신규 Refresh Token은 `Set-Cookie`(httpOnly, Secure)로 전달

### 예외 처리
- `AuthException` → `AuthExceptionHandler`가 `{ errorCode, message }`로 응답
- `INVALID_REFRESH_TOKEN`, `ACCOUNT_WITHDRAWN` 모두 401로 매핑

### Mapper 예시 (SQL)

```xml
<select id="findUserStatusById" parameterType="int" resultType="com.sist.web.vo.UsersVO">
    SELECT id, status, role
    FROM users
    WHERE id = #{id}
</select>
```

> 참고: 신규 Access Token 발급 시 `role` claim이 필요하므로, `status` 단일 컬럼이 아닌 `status`와 `role`을 함께 조회합니다. `UsersVO` 전체 컬럼을 조회하지 않고 필요한 컬럼(`id`, `status`, `role`)만 선택하는 별도 쿼리를 사용합니다.

---

## 5. 열린 이슈

없음 (TTL 정책은 Rolling 방식으로, Refresh Token 재사용 탐지는 이번 스콥 제외로, WITHDRAWN 발견 시 처리는 단순 강제 로그아웃(recoveryToken 미발급)으로 모두 확정됨)

---

## 6. 테스트 기록

### 요약
| # | 케이스                      | 상태  | errorCode               |
|---|--------------------------|-----|-------------------------|
| 1 | 성공(정상 재발급)               | 200 | -                       |
| 2 | Refresh Token 쿠키 없음      | 401 | INVALID_REFRESH_TOKEN   |
| 3 | Refresh Token 삭제 후 재사용   | 401 | INVALID_REFRESH_TOKEN   |
| 4 | 존재하지 않는/조작된 토큰           | 401 | INVALID_REFRESH_TOKEN   |
| 5 | 계정이 WITHDRAWN(소프트 탈퇴) 상태 | 401 | INVALID_REFRESH_TOKEN   |

- 테스트 도구: Swagger UI
- 테스트 일자: 2026-09-10
- 결과: 명세서와 100% 일치, 별도 수정 없음
### 상세

<details>
<summary>1. 성공(정상 재발급)</summary>

**Request**
```
refreshToken: "91c6034e-d8.."
```
**Response** `201`
- refreshToken 재발급
```json
{
   "accessToken": "eyJhb..."
}
```
</details>
<details>
<summary>2. Refresh Token 쿠키 없음</summary>

**Request**
```
없음
```
**Response** `401`
```json
{
   "errorCode": "INVALID_REFRESH_TOKEN",
   "message": "세션이 만료되었습니다. 다시 로그인해주세요."
}
```
</details>
<details>
<summary>3. Refresh Token 삭제 후 재사용</summary>

**Request**
```
refreshToken: "91c6034e-d8.."
(쿠키에서 삭제한 RefreshToken 재사용)
```
**Response** `401`
```json
{
   "errorCode": "INVALID_REFRESH_TOKEN",
   "message": "세션이 만료되었습니다. 다시 로그인해주세요."
}
```
</details>
<details>
<summary>4. 존재하지 않는/조작된 토큰</summary>

**Request**
```
refreshToken: "아무런 문자"
(존재하지 않는 RefreshToken)
```
**Response** `401`
```json
{
   "errorCode": "INVALID_REFRESH_TOKEN",
   "message": "세션이 만료되었습니다. 다시 로그인해주세요."
}
```
</details>
<summary>5. 계정이 WITHDRAWN(소프트 탈퇴) 상태</summary>

**Request**
```
refreshToken: "소프트 탈퇴한 사용자의 refreshToken"
```
**Response** `401`
```json
{
   "errorCode": "INVALID_REFRESH_TOKEN",
   "message": "세션이 만료되었습니다. 다시 로그인해주세요."
}
```
</details>
