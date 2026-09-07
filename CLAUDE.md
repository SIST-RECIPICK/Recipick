## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).

---
# CLAUDE.md - Recipick 인증 도메인 개발 가이드

## 프로젝트 개요
- Spring Boot + MyBatis + Oracle(RDS) + Redis
- 담당 범위: 인증 도메인 (회원가입, 로그인, 로그아웃, 토큰재발급, 비밀번호찾기/재설정, 계정복구, 회원탈퇴)

## 절대 규칙
- git add, git commit, git push는 절대 직접 실행하지 않는다. 변경사항 완료 후 요약만 보고한다.
- 한 번에 하나의 API 엔드포인트만 구현한다. 여러 기능을 한꺼번에 처리하지 않는다.
- 커밋 메시지는 제안만 하고 실제 커밋은 사용자가 직접 한다.

## 아키텍처 (MyBatis 기반 - JPA 사용 안 함)
- 데이터 클래스: `@Entity` 사용 금지. VO 클래스로 작성 (예: `UsersVO`, `LocalAccountVO`, `SocialAccountVO`)
- VO 필드명: 카멜케이스가 아니라 DB 컬럼명 그대로 스네이크 케이스 사용 (예: `user_id`, `created_at`)
- VO는 `@Data`(Lombok) 사용
- Mapper: `@Mapper` 인터페이스 + SQL은 XML 파일에 분리 작성
    - 파일 위치: `resources/mybatis/mapper/{인터페이스명 소문자-하이픈}-mapper.xml`
    - namespace: Mapper 인터페이스 풀 패키지 경로
    - resultType: VO 클래스 풀 패키지 경로
- Service: 인터페이스(`XxxService`) + 구현체(`XxxServiceImpl`) 분리. `@RequiredArgsConstructor`로 생성자 주입
- Controller: `XxxRestController` 네이밍. `@RestController` + `@RequiredArgsConstructor`

## 패키지 구조 (레이어 기준, 도메인 기준 아님)
- `com.sist.web.vo`, `com.sist.web.mapper`, `com.sist.web.service`, `com.sist.web.restcontroller`, `com.sist.web.config`
- 새 도메인이라고 별도 하위 패키지(`auth/`)를 만들지 않고 기존 레이어 폴더에 그대로 추가한다.

## API 경로
- /api 프리픽스 없이 /도메인/액션 형식 (예: /auth/login, /auth/signup)

## 예외 처리 (인증 도메인 신규 도입 - 팀 전체에 영향 없도록 설계)
- `AuthException`(커스텀 RuntimeException, errorCode 필드 포함) 클래스를 만들어 인증 도메인 예외 전용으로 사용한다.
- `@RestControllerAdvice` + `@ExceptionHandler(AuthException.class)`로 전역 처리하되, **반드시 AuthException 타입만 잡는다.** `Exception.class`처럼 포괄적으로 잡지 않는다 (다른 도메인 컨트롤러의 기존 try-catch 동작에 영향을 주지 않기 위함).
- 공통 에러 응답 포맷: `{ errorCode, message }`
- 인증 도메인 컨트롤러는 try-catch 없이 Service에서 AuthException을 던지는 방식으로 작성한다.

## Security (공유 파일 - 별도 조율 필요)
- SecurityConfig.java는 다른 팀원 담당이지만 JWT 필터 등록을 위해 수정이 필요하다.
- 수정 전 반드시 담당자에게 확인하고, 작업 직전 git pull로 최신화 후 최소 범위로만 수정한다.
- 현재 상태: STATELESS, CSRF disable, formLogin disable, anyRequest().permitAll()
- 변경 필요: 인증 필요/불필요 경로 분리, PasswordEncoder Bean 추가, JWT 필터를 addFilterBefore()로 등록

## 커밋 메시지 규칙 (GITGUIDE.md 기준)
- 형식: `타입: 내용` (예: 기능: 이메일 중복확인 API 구현)
- 타입: 기능 / 수정 / 스타일 / 리팩터 / 문서 / 설정
- 콜론 뒤 한 칸 띄우기, 콜론 앞 공백 금지

## Git 브랜치
- feature/기능명 브랜치에서 작업, develop으로 PR