# 회원탈퇴 (소프트)

## 1. 기능 개요

| 항목 | 내용 |
|---|---|
| 기능명 | 회원탈퇴 (소프트) |
| 카테고리 | 인증 |
| 사용자 | 로그인 유저 |
| Method | POST |
| URL | `/auth/withdraw` |
| 설명 | 계정을 소프트탈퇴 처리하고 관련 토큰/데이터를 정리한다 |

---

## 2. 요구사항 정의

### 화면 요소
- 확인 문구 입력값, [회원탈퇴], [탈퇴하기]

### 동작 흐름

1. 마이페이지 → [회원탈퇴] 클릭
2. 탈퇴 안내 화면
    - 2-1. 탈퇴 시 안내사항 표시 (복구 유예 기간, 데이터 처리 방침 등)
    - 2-2. 본인 확인 (일반: 비밀번호 입력 / 소셜: 생략)
3. 확인 문구 입력란 표시 (예: "탈퇴합니다"를 입력해주세요.) — **클라이언트 측 UX 장치**로, [탈퇴하기] 버튼 활성화 조건으로만 사용하며 서버에는 전달하지 않는다. 본 요청의 실질적 본인확인 수단은 ①일반 가입자의 비밀번호 검증, ②Access Token(Bearer) 자체(이미 로그인된 사용자만 호출 가능)이므로 확인 문구는 서버 검증 대상이 아니다.
4. 입력값 일치 시 [탈퇴하기] 버튼 활성화 → 클릭 → `POST /auth/withdraw` 요청
5. 서버 처리
    - 5-1. 본인 확인: 일반 가입자는 비밀번호 검증(필수), 소셜 전용 계정은 생략
    - 5-2. 사용자 `status`를 `WITHDRAWN`으로 변경, `withdrawn_at` 기록 (DB 트랜잭션)
    - 5-3. 토큰 무효화: 현재 Access Token 블랙리스트 등록, Refresh Token(쿠키에서 추출) Redis에서 삭제
    - 5-4. ~~소셜 로그인 시 구글 연동 해제~~ → **이번 스콥 제외.** 소셜 로그인 기능 자체가 담당 범위 밖이라 실제 Google Revoke 연동은 하지 않으며, 추후 담당자가 소셜 로그인을 구현할 때 이어붙일 수 있도록 서비스 레이어에 호출 지점(TODO)만 남겨둔다.
    - 5-5. ~~사용자가 작성한 데이터에 `is_visible = false` 일괄 변경~~ → **이번 스콥 제외.** 현재 `recipes` 테이블에 `is_visible` 컬럼 자체가 없고, 레시피 도메인은 다른 팀원 담당이라 스키마 변경 및 로직을 임의로 추가하면 충돌 위험이 있음. 팀원과 컬럼/처리 방식 협의 후 별도로 반영한다 (열린 이슈 참고).
    - 5-6. `withdrawn_at`이 기록됨으로써 해당 계정은 이후 독립적으로 실행되는 하드탈퇴 배치 스케줄러의 대상 조건(예: `withdrawn_at`이 유예기간 이전)에 자동으로 포함된다. **이 API가 스케줄러를 직접 실행하는 것은 아니다.**
6. 클라이언트 처리: Access Token은 Zustand 메모리 상태 초기화로 제거, Refresh Token은 서버가 쿠키를 만료(`Set-Cookie` Max-Age=0)시켜 정리
7. "탈퇴가 완료되었습니다." 안내 및 로그인 페이지로 이동

> 원본 요구사항 대비 수정/확정된 부분:
> - **Access Token 저장 위치 정정**: 원본에 "localStorage"로 되어 있었으나, 이미 확정된 아키텍처(XSS 방지를 위한 Zustand 인메모리 저장)와 맞지 않아 정정함.
> - **"하드 탈퇴 스케줄러 실행"(원 5-5) 표현 정정**: 이 API가 스케줄러를 실행시키는 것이 아니라, `withdrawn_at`을 기록해두는 것뿐이며 하드탈퇴는 별도로 주기 실행되는 독립적인 배치가 담당한다.
> - **확인 문구는 서버 비검증으로 확정**: 클라이언트 UX 장치로만 사용.
> - **구글 연동 해제(5-4), 레시피 is_visible 일괄 변경(5-5)은 이번 스콥에서 제외**하고 자리(TODO/열린 이슈)만 남긴다.

---

## 3. API 명세

### Request

- Header: `Authorization: Bearer {accessToken}` (필수)
- Cookie: `refreshToken` (httpOnly, 존재 시 자동 전송 — 토큰 삭제 대상, 없어도 처리에는 영향 없음)

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| password | 본인확인용 비밀번호 (일반 가입자만 필수) | String | 선택 | Y | `password123!` |

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| message | 처리 결과 메시지 | String | 필수 | N | `탈퇴가 완료되었습니다.` |

**Example**

```json
// 성공
{
  "message": "탈퇴가 완료되었습니다."
}

// 실패 - 일반 가입자인데 비밀번호 누락
{
  "errorCode": "PASSWORD_REQUIRED",
  "message": "본인 확인을 위해 비밀번호를 입력해주세요."
}

// 실패 - 비밀번호 불일치
{
  "errorCode": "INVALID_PASSWORD",
  "message": "비밀번호가 일치하지 않습니다."
}
```

### Status

| status | response content |
|---|---|
| 200 | 소프트탈퇴 성공 |
| 400 | 비밀번호 누락 (일반 가입자) |
| 401 | Access Token 누락/무효 |
| 403 | 비밀번호 불일치 |

---

## 4. 구현 참고 (팀 컨벤션 반영)

### 계층 구조

```
com.sist.web.restcontroller.AuthRestController   ← POST /auth/withdraw
com.sist.web.service.AuthService / AuthServiceImpl
com.sist.web.mapper.AuthMapper
resources/mybatis/mapper/auth-mapper.xml
com.sist.web.vo.UsersVO, LocalAccountVO
com.sist.web.security.JwtUser, JwtTokenProvider
```

### 처리 로직 (Service 레이어)

1. `@AuthenticationPrincipal JwtUser jwtUser` null 체크 → null이면 `AuthException("UNAUTHORIZED", ...)` (401) (기존 컨트롤러 패턴 재사용)
2. `AuthMapper.findLocalAccountByUserId(userId)` 조회 (email-check.md에서 정의된 쿼리 재사용)
    - 결과 있음(일반 가입자) → `password` 파라미터 누락 시 `AuthException("PASSWORD_REQUIRED", ...)` (400) → 있으면 `PasswordEncoder.matches()` 검증, 불일치 시 `AuthException("INVALID_PASSWORD", ...)` (403)
    - 결과 없음(소셜 전용) → 본인확인 생략, 다음 단계로
3. **(DB 트랜잭션, `@Transactional`)** `AuthMapper.withdrawUser(userId)` 실행 → `status = 'WITHDRAWN'`, `withdrawn_at = SYSDATE`
    - // TODO: 소셜 계정(social_accounts)인 경우 여기서 Google 연동 해제(revoke) 호출 지점. 현재 소셜 로그인 미구현으로 실제 로직 없음.
    - // TODO: 레시피 등 사용자 작성 데이터 is_visible 처리 지점. 레시피 도메인 담당자와 협의 후 추가 예정 (현재 컬럼 없음).
4. DB 트랜잭션 커밋 성공 후, Redis/토큰 정리 수행 (DB 반영이 실패하면 토큰은 그대로 살아있도록 순서 보장)
    - Access Token 블랙리스트 등록: `blacklist:{accessToken}` → `true` (TTL = 토큰 잔여 유효시간, `JwtTokenProvider.getRemainingExpiration()` 재사용)
    - Refresh Token 삭제: 쿠키에서 `refreshToken` 추출 → 존재하면 Redis `DEL refresh:{refreshToken}`
5. 응답: `Set-Cookie`로 `refreshToken` 쿠키 만료(Max-Age=0) 처리 + `{ "message": "탈퇴가 완료되었습니다." }` 200 응답

> 참고: 이 요청에 사용된 Access Token은 처리 직후 블랙리스트에 등록되므로, 동일 토큰으로 재요청이 들어와도 `JwtAuthenticationFilter` 단계에서 이미 무효 처리되어 자연스럽게 401로 막힌다. 별도의 중복 요청 방지(idempotency) 로직은 필요하지 않다.

### 예외 처리
- `AuthException` → `AuthExceptionHandler`가 `{ errorCode, message }`로 응답
- `PASSWORD_REQUIRED` → 400, `INVALID_PASSWORD` → 403, `UNAUTHORIZED` → 401

### Mapper 예시 (SQL)

```xml
<update id="withdrawUser" parameterType="long">
    UPDATE users
    SET status = 'WITHDRAWN',
        withdrawn_at = SYSDATE
    WHERE id = #{id}
</update>

<!-- findLocalAccountByUserId는 email-check.md에서 이미 정의됨 - 재사용 -->
```

---

## 5. 열린 이슈

1. **구글 연동 해제(Google Revoke) — 이번 스콥 제외.** 소셜 로그인 자체가 아직 구현되지 않았고 담당 범위 밖이므로, 실제 Revoke API 호출 및 Google 토큰 저장 설계는 하지 않는다. 서비스 레이어에 TODO 주석으로 호출 지점만 남겨두며, 추후 소셜 로그인 담당자가 구현할 때 이어붙인다.
2. **레시피 `is_visible` 일괄 변경 — 이번 스콥 제외.** 현재 `recipes` 테이블에 `is_visible` 컬럼이 없고, 레시피 도메인은 다른 팀원 담당이라 임의로 스키마/로직을 추가하면 충돌 위험이 있음. 레시피 담당 팀원과 컬럼 설계 및 처리 방식(직접 UPDATE vs. 다른 방식)을 협의한 뒤 별도로 반영한다.

---

## 6. 테스트 기록

### 요약
| # | 케이스                | 상태  | errorCode          |
|---|--------------------|-----|--------------------|
| 1 | 정상 탈퇴 (일반 가입자)     | 200 | -                  |
| 2 | 비밀번호 누락 (일반 가입자)   | 400 | PASSWORD_REQUIRED  |
| 3 | 비밀번호 불일치           | 403 | INVALID_PASSWORD   |
| 4 | Access Token 없음/무효 | 401 | UNAUTHORIZED       |
| 5 | 탈퇴한 사용자 재요청        | 200 | -                  |
| 6 | 정상 탈퇴 (소셜 가입자)     | -   | ⚠️ 소셜 로그인 구현 후 테스트 |

- 테스트 도구: Swagger UI
- 테스트 일자: 2026-09-11
- 
### 상세

<details>
<summary>1. 정상 탈퇴 (일반 가입자)</summary>

**Request**
```
refreshToken: "1f01b480-29b3.."
Authorization: "eyJhbGciOi.."
```
```json
{
   "password": "password1234!"
}
```
**Response** `200`
```json
{
   "message": "탈퇴가 완료되었습니다."
}
```
</details>
<details>
<summary>2. 비밀번호 누락 (일반 가입자)</summary>

**Request**
```
refreshToken: "1f01b480-29b3.."
Authorization: "eyJhbGciOi.."
```
```json
{
   "password": ""
}
```
**Response** `400`
```json
{
   "errorCode": "PASSWORD_REQUIRED",
   "message": "본인 확인을 위해 비밀번호를 입력해주세요."
}
```
</details>
<details>
<summary>3. 비밀번호 불일치</summary>

**Request**
```
refreshToken: "1f01b480-29b3.."
Authorization: "eyJhbGciOi.."
```
```json
{
   "password": "틀린비밀번호"
}
```
**Response** `403`
```json
{
   "errorCode": "INVALID_PASSWORD",
   "message": "비밀번호가 일치하지 않습니다."
}
```
</details>
<details>
<summary>4. Access Token 없음/무효</summary>

**Request**
```
refreshToken: "75bb4198-3e70-428e-8893-6dac403a53a3"
Authorization: "공백 또는 틀린값"
```
```json
{
   "password": "password1234!"
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
<summary>5. 탈퇴한 사용자 재요청</summary>

**Request**
```
refreshToken: "75bb4198-3e70-428e-8893-6dac403a53a3"
Authorization: "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMDAxIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3ODkxMDQ4MzcsImV4cCI6MTc4OTEwNjYzN30.6yVwzofdqNNj2Vu6rLCCWwCnoPEL5j1JCNvfctqSri0"
```
```json
{
   "password": "password123!"
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
