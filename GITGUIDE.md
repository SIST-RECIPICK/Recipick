# 🌱 Git 협업 가이드

> 우리 팀 Git 브랜치 전략과 작업 흐름을 정리한 문서입니다.
> Git이 처음인 팀원도 따라 할 수 있도록 **기본 명령어 설명**부터 넣었습니다.

---

## 📖 목차
1. [기본 개념](#1-기본-개념)
2. [기본 명령어 설명](#2-기본-명령어-설명)
3. [브랜치 흐름표](#3-브랜치-흐름표)
4. [단계별 협업 흐름](#4-단계별-협업-흐름)
5. [충돌(Conflict) 해결법](#5-충돌conflict-해결법)
6. [커밋 메시지 규칙](#6-커밋-메시지-규칙)

---

## 1. 기본 개념

| 용어 | 설명 |
|---|---|
| **브랜치(branch)** | 작업을 담는 독립된 줄기. 서로 영향을 주지 않고 따로 작업 가능 |
| **main** | 최종 완성/배포용 브랜치. 함부로 건드리지 않음 |
| **dev** | 개발 통합용 브랜치. 팀원들의 기능이 여기로 모임 |
| **feature/○○** | 각자 기능을 개발하는 작업용 브랜치 (예: `feature/login`) |
| **원격(origin)** | GitHub에 올라가 있는 저장소 (모두가 공유) |
| **로컬(local)** | 내 컴퓨터에 있는 저장소 |
| **PR (Pull Request)** | "내 브랜치를 다른 브랜치에 합쳐주세요" 하고 요청하는 것 |

**전체 그림 한 줄 요약**
> 개발자는 `dev`에서 `feature`를 따서 작업 → 완성되면 PR로 `dev`에 합침 → 팀장이 `dev`을 `main`으로 배포

---

## 2. 기본 명령어 설명

> 💡 처음 보는 명령어는 여기서 찾아보세요.

| 명령어 | 설명 |
|---|---|
| `git clone <주소>` | 원격 저장소를 내 컴퓨터로 통째로 복제 (처음 1번) |
| `git switch <브랜치명>` | 해당 브랜치로 **이동** |
| `git switch -c <브랜치명>` | 브랜치를 **생성 + 이동** (`-c` = create) |
| `git branch` | 브랜치 목록 확인 (`*` 표시가 현재 내 위치) |
| `git pull origin <브랜치명>` | 원격의 해당 브랜치를 **내려받기(+합치기)** |
| `git add .` | 변경한 파일 전체를 커밋 대상으로 **선별** |
| `git commit -m "메시지"` | 선별한 변경사항을 **기록(저장)** |
| `git push origin <브랜치명>` | 내 브랜치를 원격 저장소에 **올리기** |
| `git merge <브랜치명>` | 다른 브랜치를 **현재 브랜치에 합치기** |
| `git status` | 현재 상태 확인 (변경/충돌 파일 등) |
| `git log --oneline --graph --all` | 커밋 이력을 그래프로 보기 |

**⚠️ 자주 헷갈리는 포인트**
- `pull` = 내려받기 ⬇️ / `push` = 올리기 ⬆️ (방향 반대!)
- `pull`은 **지금 내가 서 있는 브랜치**로 코드가 들어옵니다.
- `origin`은 원격 저장소 이름입니다. `git pull dev`(❌) → `git pull origin dev`(✅)

---

## 3. 브랜치 흐름표

```
main ●─────────────────────────────────────────● (v1.0.0 배포)
      \                                        ↑
       \                                   PR merge
        \                                (dev → main)
         ●──────●──────────●──────────────●  dev
          \      \          ↑              ↑
           \      \      PR merge       PR merge
            \      \   (feature → dev)
             \      \
              \      ●──●──●  feature/recipe   (개발자 B)
               \
                ●──●──●──●  feature/login    (개발자 A)

────────────────────────────────────────────────────
방향 정리
  dev ─→ feature : 최신 코드 받아오기 (작업 중, 로컬)   ⬇️
  feature ─→ dev : 완성한 기능 올리기 (완료 시, PR)     ⬆️
```

---

## 4. 단계별 협업 흐름

### 0단계 · 초기 세팅

```bash
# [팀장] main에서 dev 생성 후 원격에 올리기
git switch main            # main으로 이동
git switch -c dev      # dev 생성 + 이동 (main 복사됨)
git push origin dev    # 원격에 dev 올리기
```

```bash
# [개발자] 저장소 받아서 dev 최신화
git clone https://github.com/SIST-RECIPICK/Recipick.git      # 저장소 복제 (맨처음 1번 → 이미 했으면 안해도 됨)
git switch dev         # dev으로 이동
git pull origin dev    # dev 최신 상태로 받기
```

---

### 1단계 · feature 브랜치 생성

```bash
# [개발자 A] dev 최신 받고 → 로그인 브랜치 따기
git switch dev
git pull origin dev
git switch -c feature/login
```

```bash
# [개발자 B] dev 최신 받고 → 레시피 브랜치 따기
git switch dev
git pull origin dev
git switch -c feature/recipe
```

> ✅ **feature는 항상 최신 dev에서 따세요.** (pull first!)

---

### 2단계 · 작업 → 커밋 → 원격 push

```bash
# [개발자] 개발 후 커밋하고 내 브랜치를 원격에 올리기
git add .                          # 변경사항 선별
git commit -m "로그인 기능 구현"    # 기록
git push origin feature/login      # 원격에 올리기
```

> 💡 커밋은 **지금 선 브랜치에만** 쌓입니다. push해도 dev/main과 자동으로 합쳐지지 않습니다. (합쳐지는 건 오직 merge 때!)
> ### ⚠️ 절대 하면 안 되는 실수

| 실수 | 왜 문제인가 |
| --- | --- |
| dev또는 main에서 직접 작업 | dev와 main은 항상 오류 없는 상태여야 합니다 |
| push만 하고 PR 안 하기 | push는 내 브랜치까지만 갑니다. PR + Merge를 해야 dev에 반영됩니다 |
| 작업 시작 전 pull 안 하기 | 최신화 안 된 채로 작업하면 나중에 충돌이 커집니다 |

---

### 3단계 · 작업 중 dev 최신화 (매일 아침 권장)

> **목적:** `dev → 내 feature`로 최신 코드 **받아오기**. 충돌 미리 예방.
> 아래 **방법 ① 또는 ② 중 하나만** 하면 됩니다. (결과 동일)

**방법 ① — 짧게 (feature에서 바로 당기기)**
```bash
# [개발자]
git switch feature/login      # 내 브랜치에 선다
git pull origin dev       # 내 브랜치로 origin dev을 당겨 바로 합침
```

**방법 ② — 단계별 (dev 갱신 후 merge)**
```bash
# [개발자]
git switch dev
git pull origin dev       # dev을 최신으로
git switch feature/login
git merge dev             # dev을 내 브랜치에 합침
```

> ⚠️ **왜 dev만 받으면 안 되나요?**
> `pull`은 현재 선 브랜치로 들어옵니다. dev만 최신화하면 정작 내 feature에는 최신 코드가 없습니다. 그래서 **내 feature에 dev을 합쳐야** 합니다.
>
> 👉 충돌이 나면 → [5. 충돌 해결법](#5-충돌conflict-해결법)

---

### 4단계 · 기능 완성 → PR로 dev에 올리기

> **목적:** `feature → dev`으로 완성품 **올리기** (3단계와 방향 반대 ⬆️)

```
[개발자]
1. GitHub 접속
2. feature/login → dev 으로 PR(Pull Request) 생성
3. 팀 리뷰 후 Merge 버튼 클릭
```

---

### 5단계 · dev → main 배포

```bash
# [팀장] dev을 main으로 병합 후 배포
git switch main
git pull origin main
git merge dev
git push origin main
```

```bash
# [팀장] (선택) 배포 버전 태그 붙이기
git tag v1.0.0
git push origin v1.0.0
```

> ✅ **권장:** 5단계도 GitHub에서 `dev → main` PR로 처리하면 이력이 남아 관리에 좋습니다.

---

## 5. 충돌(Conflict) 해결법

> 충돌은 **같은 파일의 같은 부분을 두 사람이 다르게 고쳤을 때** 발생합니다.
> 잘못된 게 아니라 **자연스러운 상황**이니 당황하지 마세요.

### 충돌 발생 시 화면
```
CONFLICT (content): Merge conflict in config.js
Automatic merge failed; fix conflicts and then commit the result.
```

### 충돌 파일을 열면
```
<<<<<<< HEAD
const timeout = 3000;        // 내가 쓴 코드
=======
const timeout = 5000;        // dev에 있던(상대) 코드
>>>>>>> dev
```

| 기호 | 의미 |
|---|---|
| `<<<<<<< HEAD` ~ `=======` | 내 코드 |
| `=======` ~ `>>>>>>> dev` | 상대 코드 |

### 해결 순서
1. 어떤 코드를 남길지 **직접 판단**해서 하나로 정리
2. `<<<<<<<`, `=======`, `>>>>>>>` **기호 3줄 모두 삭제**

정리 예시 👇
```
const timeout = 5000;
```

3. 정리 후 마무리 커밋
```bash
git add .
git commit -m "dev 병합 충돌 해결"
git push origin feature/login
```

> 💡 **충돌 예방 3대 습관**
> 1. **자주 pull** — 차이가 작을수록 충돌도 작음
> 2. **작은 단위로 자주 커밋/merge** — 브랜치를 오래 방치할수록 충돌 커짐
> 3. **담당 파일 나누기** — 같은 파일 동시 작업 피하기

---

## 6. 커밋 메시지 규칙

> 커밋할 때 **`타입: 내용`** 형식으로 통일합니다. 예: `기능: 로그인 화면 구현`

### 기본 형식
```
[이름] 타입: 작업 내용
```
- 타입과 내용 사이에 `:` + 한 칸 띄우기
- 내용은 **무엇을 했는지 한 줄로 간결하게**

### 타입 종류 (이것만 기억)

| 타입 | 언제 쓰나 | 예시 |
|---|---|---|
| `기능` | 새 기능 추가 | `기능: 회원가입 API 구현` |
| `수정` | 버그 수정 | `수정: 로그인 시 토큰 오류 해결` |
| `스타일` | 코드 포맷·들여쓰기 (기능 변화 없음) | `스타일: 코드 정렬 정리` |
| `리팩터` | 기능 그대로, 코드 구조 개선 | `리팩터: 중복 로직 함수로 분리` |
| `문서` | README·주석 등 문서 | `문서: 가이드에 설치법 추가` |
| `설정` | 환경설정·패키지 등 | `설정: gitignore 추가` |

### 좋은 예 / 나쁜 예
```
✅ 기능: 게시글 목록 페이징 구현
✅ 수정: 이미지 업로드 시 확장자 검사 추가
✅ 문서: 협업 가이드 브랜치 흐름표 수정

❌ 수정함                  (타입 없음, 뭘 했는지 모름)
❌ 기능: ㅁㄴㅇㄹ           (내용 불명확)
❌ 기능 : 로그인 구현       (콜론 앞 공백 ❌)
```

### 실제 사용
```bash
git commit -m "[홍길동] 기능: 로그인 화면 구현"
```

---

### ☀️ 매일 루틴
```bash
# 1. 아침: dev 최신화 (방법 ① 추천)
git switch feature/login
git pull origin dev

# 2. 작업 → 커밋 → 올리기
git add .
git commit -m "작업 내용"
git push origin feature/login

# 3. 기능 완료 → GitHub에서 PR → dev merge
```

### 🧭 두 방향 merge, 헷갈릴 때 이것만!
- **dev → feature** = 받기 (작업 중 · 로컬 · 3단계) ⬇️
- **feature → dev** = 올리기 (완료 · PR · 4단계) ⬆️
