# 회원탈퇴 (하드) — 배치 스케줄러

## 1. 기능 개요

| 항목 | 내용 |
|---|---|
| 기능명 | 회원탈퇴 (하드) |
| 카테고리 | 인증 |
| 실행 주체 | 배치 스케줄러 (REST API 아님) |
| 실행 주기 | 매일 자정 (`0 0 0 * * *`) |
| 설명 | 소프트탈퇴 후 30일 경과한 계정을 익명화 처리한다 |

---

## 2. 요구사항 정의

### 동작 흐름

1. 하드탈퇴 스케줄러 실행 (매일 자정)
2. 삭제 대상 조회: `status = WITHDRAWN` + `withdrawn_at`이 현재 시각 기준 30일 이전
3. 대상 사용자마다 반복 처리 (**건당 개별 트랜잭션** — 한 명 실패가 나머지 처리를 막지 않도록)
    - 3-1. 삭제 직전 재검증: 조회 시점과 실제 처리 시점 사이 텀이 있을 수 있으므로, 처리 직전 해당 유저의 `status`가 여전히 `WITHDRAWN`인지 다시 확인
    - 3-2. 대상 O (여전히 WITHDRAWN) → 익명화 처리
        - 3-2-1. `users` 테이블의 개인정보 컬럼을 **유저마다 겹치지 않는** 값으로 대체 (아래 4번 구현 참고 참조 — 고정값 사용 시 유니크 제약 위반으로 배치 실패함)
        - 3-2-2. `status`를 `DELETE`로 변경
        - 3-2-3. 해당 사용자가 등록한 콘텐츠 데이터(레시피 등)는 삭제하지 않음 (참조 무결성 보존 — 이 때문에 `users` 행 자체는 남기고 값만 익명화)
        - 3-2-4. Access/Refresh Token 잔재 확인 및 제거 → **실질적으로 항상 스킵됨** (아래 4번 구현 참고 참조)
        - 3-2-5. `local_accounts` (및 향후 `social_accounts`) 행은 완전 삭제 — 다른 데이터가 참조하지 않는 인증 전용 테이블이므로 남길 이유 없음
    - 3-3. 대상 X (그 사이 계정이 복구되어 `status`가 `ACTIVE`로 바뀐 경우) → 이번 배치에서 해당 유저는 건너뜀 (skip)

> 원본 요구사항 대비 추가/확정된 부분:
> - **익명화 값은 사용자마다 고유해야 함**: `email`, `nickname` 등 유니크 제약이 걸린 컬럼을 고정 문자열로 덮어쓰면 두 번째 대상자부터 제약 위반으로 배치가 실패한다. `userId`를 포함해 유저마다 겹치지 않는 값을 생성한다.
> - **`local_accounts`(인증 자격 정보) 행은 완전 삭제로 확정**: "사용자가 작성한 데이터는 삭제하지 않는다"는 원칙은 콘텐츠 데이터(예: 레시피)에 한정되며, 다른 테이블이 참조하지 않는 인증 전용 테이블은 하드탈퇴 시점에 완전히 제거한다.
> - **Token 잔재 제거는 실질적으로 no-op**: Refresh Token 최대 TTL(14일, Rolling)이 하드탈퇴 유예기간(30일)보다 짧으므로, 이 시점엔 이미 자연 만료되어 Redis에 남아있을 수 없다. 또한 현재 Redis 구조(`refresh:{토큰값}` → `userId`)로는 `userId` 기준 역조회 자체가 불가능하다(비밀번호 재설정 기능에서 "다른 기기 세션 무효화"를 스콥 제외하며 이미 확인된 제약). 별도 코드 없이 "자연 만료로 이미 해결됨"으로 문서화한다.
> - **배치 실행 결과는 애플리케이션 로그(INFO)로만 기록**하며, 별도 로그 테이블은 만들지 않는다.

---

## 3. 처리 명세

### 처리 대상 조건

| 조건 | 설명 |
|---|---|
| status | `WITHDRAWN` |
| withdrawnAt | 현재 시각 기준 30일 이전 |

### 처리 결과 (애플리케이션 로그로 기록)

| 필드 | 설명 | 예 |
|---|---|---|
| processedCount | 이번 배치에서 익명화 처리된 건수 | `12` |
| skippedCount | 처리 직전 재검증 시 이미 복구되어 스킵된 건수 | `1` |
| failedUserIds | 개별 트랜잭션 실패한 userId 목록 (다음 배치에 재시도됨) | `[]` |

**로그 예시**

```json
{
  "executedAt": "2026-09-07T00:00:00",
  "processedCount": 12,
  "skippedCount": 1,
  "failedUserIds": []
}
```

### 실행 결과 구분

| 구분 | 내용 |
|---|---|
| 정상 완료 | 대상자 전원(스킵 제외) 처리 완료 |
| 부분 실패 | 일부 건 트랜잭션 실패 → `failedUserIds`에 담아 INFO 로그로 남김, 다음날 배치가 동일 조건으로 재조회하므로 자동 재시도됨 |

---

## 4. 구현 참고 (팀 컨벤션 반영)

### 계층 구조

```
com.sist.web.scheduler.HardWithdrawScheduler   ← @Scheduled(cron = "0 0 0 * * *")
com.sist.web.service.AuthService / AuthServiceImpl   ← 유저 1건 익명화 처리(@Transactional)
com.sist.web.mapper.AuthMapper
resources/mybatis/mapper/auth-mapper.xml
com.sist.web.vo.UsersVO
```

> `scheduler`는 이번에 새로 추가되는 패키지 (기존 `exception`, `dto`, `security`처럼 인증 도메인 전용으로 분리).

### 처리 로직

1. **스케줄러(`HardWithdrawScheduler`)**: `AuthMapper.findHardDeleteCandidates(cutoffDate)`로 대상 `userId` 목록 조회 (`cutoffDate` = `LocalDateTime.now().minusDays(30)`, Java에서 계산해 파라미터로 전달)
2. 목록을 순회하며 각 `userId`에 대해 `AuthService.anonymizeUser(userId)` 호출
    - **주의**: 스케줄러 클래스 내부에서 자기 자신의 `@Transactional` 메서드를 호출하면 Spring AOP 프록시가 적용되지 않아 트랜잭션이 걸리지 않는다. 반드시 별도 스프링 빈(`AuthService`)의 메서드를 호출하는 구조여야 건당 트랜잭션이 실제로 분리된다.
    - 개별 호출은 `try-catch`로 감싸서, 특정 유저 처리 중 예외가 발생해도 나머지 유저 처리를 계속 진행한다 (실패 userId는 리스트에 누적).
3. **`AuthService.anonymizeUser(userId)` (`@Transactional`)**
    1. `AuthMapper.findUserStatusById(userId)` 재조회 (reissue.md에서 정의한 쿼리 재사용) → `status != WITHDRAWN`이면 skip 처리(카운트만 증가, 예외 아님)
    2. 익명화 값 생성: `email = "deleted_" + userId + "@withdrawn.recipick"`, `nickname = "탈퇴회원" + userId` (※ 컬럼 길이 제약 확인 필요)
    3. `AuthMapper.anonymizeUser(userId, email, nickname)` 실행 → `profile_image_url`, `introduction`은 NULL 처리, `status = 'DELETE'`
    4. `AuthMapper.deleteLocalAccount(userId)` 실행 (`local_accounts` 행 완전 삭제)
4. 배치 종료 후 `processedCount`, `skippedCount`, `failedUserIds`를 INFO 레벨로 로그 기록

### 예외 처리
- 개별 유저 처리 실패는 예외를 상위로 전파하지 않고 스케줄러 루프에서 캐치 → `failedUserIds`에 추가 후 다음 유저로 계속 진행
- `AuthExceptionHandler`는 REST 요청에 대한 응답 처리기이므로 이 배치 로직에는 관여하지 않음 (컨트롤러를 거치지 않는 내부 스케줄러이기 때문)

### Mapper 예시 (SQL)

```xml
<select id="findHardDeleteCandidates" resultType="long" parameterType="java.time.LocalDateTime">
    SELECT id
    FROM users
    WHERE status = 'WITHDRAWN'
      AND withdrawn_at &lt;= #{cutoffDate}
</select>

<update id="anonymizeUser" parameterType="map">
    UPDATE users
    SET email = #{email},
        nickname = #{nickname},
        profile_image_url = NULL,
        introduction = NULL,
        status = 'DELETE'
    WHERE id = #{userId}
</update>

<delete id="deleteLocalAccount" parameterType="long">
    DELETE FROM local_accounts
    WHERE user_id = #{userId}
</delete>

<!-- findUserStatusById는 reissue.md에서 이미 정의됨 - 재사용 -->
```

---

## 5. 열린 이슈

없음 (익명화 값의 유니크 보장 방식, `local_accounts` 행 완전 삭제, Token 잔재 제거 단계 no-op 처리, 실행 결과 기록 방식(애플리케이션 로그) 모두 확정됨)