# 비밀번호 재설정 링크 유효성 검증

## 1. 기능 개요

| 항목 | 내용 |
|---|---|
| 기능명 | 비밀번호 재설정 링크 유효성 검증 |
| 카테고리 | 인증 |
| 사용자 | 비로그인 유저 (이메일 링크로 진입) |
| Method | GET |
| URL | `/auth/password/reset/validate` |
| 설명 | 비밀번호 재설정 페이지 진입 시 URL의 token 유효 여부를 확인한다 |

---

## 2. 요구사항 정의

### 동작 흐름

1. 사용자가 메일에 포함된 재설정 URL 클릭 → 비밀번호 재설정 페이지 진입
2. 프론트에서 URL의 `token` 쿼리 파라미터로 `GET /auth/password/reset/validate` 요청
3. 서버 검증
    - 3-1. `token` 파라미터 자체가 없음 → 400 (파라미터 누락)
    - 3-2. Redis에 해당 토큰이 존재하지 않음(만료 또는 애초에 무효한 토큰) → `valid: false`
    - 3-3. 토큰은 존재하지만, 매핑된 계정이 **WITHDRAWN 상태로 전환됨** → `valid: false`
    - 3-4. 토큰 존재 + 계정 ACTIVE → `valid: true`
4. 프론트는 `valid` 값에 따라 새 비밀번호 입력 폼을 노출하거나, "유효하지 않은 링크입니다" 안내를 표시

> 원본 요구사항 대비 추가/확정된 부분:
> - **읽기 전용(peek) 동작으로 확정**: 이 API는 토큰을 소비(삭제)하지 않는다. 실제 비밀번호 변경은 다음 기능("비밀번호 재설정")에서 동일 토큰을 다시 검증 + 소비하므로, 여기서 미리 삭제하면 폼 진입은 성공했는데 정작 제출 시 토큰이 사라져 실패하는 모순이 생긴다.
> - **만료/위조 토큰 구분 없음**: Redis 특성상 TTL이 지난 키와 애초에 존재하지 않는 키를 서버가 구분할 방법이 없으므로, 둘 다 동일하게 `valid: false`로 응답한다.
> - **계정 상태(WITHDRAWN) 재확인 추가**: 토큰 발급 이후 계정이 탈퇴되었을 가능성을 고려하여, 토큰 존재 여부뿐 아니라 매핑된 계정이 여전히 ACTIVE인지도 함께 확인한다.
> - **요청 주기 제한 불필요**: 토큰은 UUID로 엔트로피가 매우 커서 사실상 추측이 불가능하므로, 비밀번호 찾기(재설정 링크 요청) API와 달리 별도의 rate limit이 필요하지 않다.
> - 이 API는 "무효한 토큰"을 시스템 오류가 아닌 정상적인 결과로 취급하므로, `AuthException` 기반 에러 응답이 아닌 200 + `valid: false`로 응답한다 (파라미터 자체가 누락된 경우만 예외적으로 400 처리).

---

## 3. API 명세

### Query Parameter

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| token | 재설정 URL에 포함된 토큰 | String | 필수 | N | `a1b2c3d4-...` |

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| valid | 토큰 유효 여부 (계정 탈퇴 여부까지 반영된 최종 결과) | Boolean | 필수 | N | `true` |

**Example**

```json
// 유효한 토큰 (토큰 존재 + 계정 ACTIVE)
{
  "valid": true
}

// 무효/만료된 토큰, 또는 토큰은 유효하나 계정이 WITHDRAWN 상태
{
  "valid": false
}

// 실패 - token 파라미터 누락
{
  "errorCode": "MISSING_TOKEN",
  "message": "잘못된 접근입니다."
}
```

### Status

| status | response content |
|---|---|
| 200 | 정상 처리 (`valid` 값으로 프론트에서 폼 노출 여부 결정) |
| 400 | token 파라미터 누락 |

---

## 4. 구현 참고 (팀 컨벤션 반영)

### 계층 구조

```
com.sist.web.restcontroller.AuthRestController   ← GET /auth/password/reset/validate
com.sist.web.service.AuthService / AuthServiceImpl
com.sist.web.mapper.AuthMapper
resources/mybatis/mapper/auth-mapper.xml
```

### 처리 로직 (Service 레이어)

1. `token` 파라미터 존재 확인 (null 또는 빈 문자열) → 없으면 `AuthException("MISSING_TOKEN", ...)` (400)
2. Redis `pwReset:{token}` 조회 → 값(`userId`) 확인
    - 키 없음(만료/무효) → `valid: false` 응답 (200)
3. 조회된 `userId`로 `AuthMapper.findUserStatusById(userId)` 조회 (reissue.md에서 정의한 쿼리 재사용)
    - `status == WITHDRAWN` → `valid: false` 응답 (200)
    - `status == ACTIVE` → `valid: true` 응답 (200)

> 참고: 위 2~3번 과정에서 Redis 키나 계정 상태에 어떠한 변경(삭제 등)도 가하지 않는다. 순수 조회(peek)만 수행하며, 실제 토큰 소비는 "비밀번호 재설정" API에서 이루어진다.

### 예외 처리
- `AuthException` → `AuthExceptionHandler`가 `{ errorCode, message }`로 응답 (`MISSING_TOKEN` → 400)
- 그 외 "무효한 토큰" 케이스는 예외를 던지지 않고 정상 응답(`valid: false`)으로 처리

### Mapper 예시 (SQL)

```xml
<!-- findUserStatusById는 reissue.md에서 이미 정의됨 - 재사용 -->
```

---

## 5. 열린 이슈

없음 (토큰 소비 여부(읽기 전용으로 확정), 계정 상태(WITHDRAWN) 재확인 포함 여부 모두 확정됨)