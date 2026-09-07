# 닉네임 중복확인 (Nickname Duplicate Check)

## 1. 기능 개요

| 항목 | 내용 |
|---|---|
| 기능명 | 닉네임 중복확인 |
| 카테고리 | 인증 |
| 사용자 | 유저 |
| Method | GET |
| URL | `/auth/nickname/check` |
| 설명 | 회원가입 시 입력한 닉네임의 사용 가능 여부를 확인한다 |

---

## 2. 요구사항 정의 (개선본)

### 화면 요소
- 닉네임 입력창

### 동작 흐름

1. 사용자가 닉네임 입력창에 값을 입력한다.
2. 입력창에서 포커스가 벗어나는 시점(blur), 프론트에서 자동으로 중복검사 API 호출 (프론트 전용 로직 — 백엔드 스펙에는 영향 없음)
3. 닉네임 형식 검증
    - 3-1. 형식/길이 오류 → 오류 메시지 표시, 서버도 400으로 재검증
4. 서버 응답에 따라 분기
    - 4-1. `available: true` → "사용 가능한 닉네임입니다." 안내
    - 4-2. `available: false` → "이미 사용중인 닉네임입니다." 안내
5. 닉네임 값이 변경되면 검증 상태를 초기화한다 (서버는 회원가입 시점에 재검증하므로 최종 방어선은 서버에 있음).

> 원본 요구사항 대비 추가/명확화된 부분: (1) 형식/길이 오류 케이스, (2) 값 변경 시 상태 초기화. "input창 이탈 시 자동 검사"는 순수 프론트 로직이라 API 스펙에는 영향 없음.

### 닉네임 정책 (확정)

- 길이: 2~10자
- 허용 문자: 한글, 영문, 숫자만 (특수문자, 공백 불가)
- 정규식 예시: `^[가-힣a-zA-Z0-9]{2,10}$`

---

## 3. API 명세

### Request

**Query Parameter**

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| nickname | 확인할 닉네임 | String | 필수 | N | `안뇽가리` |

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| available | 사용 가능 여부 | Boolean | 필수 | N | `true` |

**Example**

```json
// 닉네임 사용 가능
{
  "available": true
}

// 닉네임 중복
{
  "available": false
}
```

### Status

| status | response content |
|---|---|
| 200 | 정상 처리 |
| 400 | 닉네임 형식/길이 오류 |

---

## 4. 구현 참고 (팀 컨벤션 반영)

### 계층 구조

```
com.sist.web.restcontroller.AuthRestController   ← GET /auth/nickname/check
com.sist.web.service.AuthService / AuthServiceImpl
com.sist.web.mapper.AuthMapper
resources/mybatis/mapper/auth-mapper.xml
com.sist.web.vo.UsersVO
```

### 처리 로직 (Service 레이어)

1. 닉네임 길이/형식 검증 (정규식 `^[가-힣a-zA-Z0-9]{2,10}$`) → 실패 시 `AuthException("INVALID_NICKNAME_FORMAT", "닉네임은 2~10자의 한글, 영문, 숫자만 사용 가능합니다.")` 발생
2. `AuthMapper.findUserByNickname(nickname)` 조회
    - 결과 존재 → `available: false`
    - 결과 없음 → `available: true`

### 예외 처리
- `AuthException` → `AuthExceptionHandler`가 `{ errorCode, message }`로 응답 (400)
- 컨트롤러에는 try-catch 불필요

### Mapper 예시 (SQL)

```xml
<select id="findUserByNickname" resultType="com.sist.web.vo.UsersVO" parameterType="String">
    SELECT id, nickname
    FROM users
    WHERE nickname = #{nickname}
</select>
```

### AuthRestController 예시 (이메일 중복확인과 같은 컨트롤러에 이어서 작성)

```java
@GetMapping("/nickname/check")
public ResponseEntity<NicknameCheckResponse> checkNickname(
        @RequestParam("nickname") String nickname
) {
    return ResponseEntity.ok(authService.checkNickname(nickname));
}
```

---

## 5. 열린 이슈

- [x] 닉네임 길이/형식 정책 확정 (2~10자, 한글/영문/숫자) — 회원가입 명세서 작성 시 동일하게 반영