# 💊 IPillGood Client

<p align="center">
  <img width="100%" alt="아필굿 배너" src="https://github.com/user-attachments/assets/2a35db81-a847-4106-b1f4-cbe670963a8e" />
</p>

### 안녕하세요, 아필굿(I Pill Good) 백엔드 팀입니다. 😆

<br>

## 📌 프로젝트 소개

나에게 맞는 영양제 추천부터 루틴 형성까지, 개인 맞춤 영양제 추천 웹앱 서비스 **아필굿(I Pill Good)**!

> 💊 Pill Good? Feel Good!

웰니스 트렌드 속에서 영양제에 관심을 갖는 사람들은 늘고 있지만, 수백 가지 제품과 넘쳐나는 정보 앞에서 정작 '나에게 맞는 영양제'를 찾지 못하는 경우가 많습니다.

아필굿은 나이·성별·직업·건강 고민 등 개인 건강 정보를 기반으로 AI가 맞춤 영양제를 추천하고, 컨디션에 따라 오늘 필요한 영양제까지 제안하는 웹앱 서비스입니다. 식약처 데이터를 활용한 성분 투명도 리포트로 광고가 아닌 근거 있는 정보를 제공하며, 일일 섭취 체크·캘린더·캐릭터 성장 요소를 통해 꾸준히 먹는 습관까지 함께 만들어갑니다.

| 목표                     | 내용                                                                 |
| ------------------------ | -------------------------------------------------------------------- |
| 💊 맞춤 추천 & 루틴 형성 | 개인 건강 정보 기반 AI 추천 + 일일 섭취 체크로 꾸준한 복용 습관 형성 |
| 💡 신뢰할 수 있는 정보   | 식약처 데이터 기반 성분 투명도 리포트 제공                           |
| 🔎 탐색 & 비교           | 필터링 기반 랭킹·검색, 성분·함량 직접 비교                           |
| 🗄️ 영양제 통합 관리      | 보유 영양제 관리 + 병용 금기 안내                                    |
| 🧪 컨디션 기반 추천      | 주간 컨디션 기록 기반 영양제 & 대체 음식 추천                        |

<br>

## 👥 팀원 및 백엔드 역할 분담

<table border="1">
  <tr>
    <td align="center" width="25%"><a href="https://github.com/zeoueon"><img src="https://avatars.githubusercontent.com/u/163366999?v=4" width="100px" height="100px" style="object-fit:cover" alt="여니" /></a><br /><a href="https://github.com/zeoueon"><sub>@zeoueon</sub></a></td>
    <td align="center" width="25%"><a href="https://github.com/HYUNJOON-SUNG"><img src="https://avatars.githubusercontent.com/u/201899113?v=4" width="100px" height="100px" style="object-fit:cover" alt="카망" /></a><br /><a href="https://github.com/HYUNJOON-SUNG"><sub>@HYUNJOON</sub></a></td>
    <td align="center" width="25%"><a href="https://github.com/yeongjun25"><img src="https://avatars.githubusercontent.com/u/214789007?v=4" width="100px" height="100px" style="object-fit:cover" alt="제이" /></a><br /><a href="https://github.com/yeongjun25"><sub>@yeongjun25</sub></a></td>
    <td align="center" width="25%"><a href="https://github.com/LeeJaeJun1"><img src="https://avatars.githubusercontent.com/u/108278044?v=4" width="100px" height="100px" style="object-fit:cover" alt="주니" /></a><br /><a href="https://github.com/LeeJaeJun1"><sub>@LeeJaeJun1</sub></a></td>
  </tr>
  <tr>
    <td align="center"><b>여니 | 윤서연</b></td>
    <td align="center"><b>카망 | 성현준</b></td>
    <td align="center"><b>제이 | 유영준</b></td>
    <td align="center"><b>주니 | 이재준</b></td>
  </tr>
  <tr>
    <td align="center">👑 팀장</td>
    <td align="center">팀원</td>
    <td align="center">팀원</td>
    <td align="center">팀원</td>
  </tr>

</table>

<br>

## 🛠 기술 스택

| 분류             | 기술                                           |
|----------------|----------------------------------------------|
| Language       | Java 21                                      |
| Framework      | Spring Boot 3.5.9                            |
| Database       | PostgreSQL                                   |
| Cache / Stream | Redis                                        |
| ORM            | Spring Data JPA + QueryDSL 5.1.0             |
| Auth           | Spring Security + OAuth2 + JWT (JJWT 0.12.3) |
| Storage        | AWS S3                                       | 
| API Docs       | springdoc-openapi 2.8.5 (Swagger UI)         |
| CI/CD          | GitHub Actions → AWS ECR → EC2               |
| Container      | Docker (eclipse-temurin:21-jre)              |

| Category       | Stack                                                                                                                             | 도입 이유                               |
|----------------|-----------------------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| Web Framework  | ![Spring MVC](https://img.shields.io/badge/Spring%20MVC-6DB33F?style=for-the-badge&logo=spring&logoColor=white)                   | REST API 기반 서비스 구조를 설계하고 확장하기 위해 사용 |
| Security       | ![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white) | JWT 기반 인증 및 역할별 접근 제어를 분리하기 위해 도입   |
| Authentication | ![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)                            | 무상태 인증 방식으로 확장성과 보안을 동시에 확보하기 위해 사용 |

| Category     | Stack                                                                                                             | 도입 이유                                               |
|--------------|-------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------|
| RDBMS        | ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4479A1?style=for-the-badge&logo=postgreSQL&logoColor=white) | 무결성과 정합성을 갖춘 DB, 플러그인 확장으로 의미 기반 유사도 검색을 구현하기 위해 도입 |
| Cache/Stream | ![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)                | 자주 조회되는 데이터를 캐싱, I/O 비동기 처리를 위한 Stream 자료구조 활용      |

| Category  | Stack                                                                                                                          | 도입 이유                                    |
|-----------|--------------------------------------------------------------------------------------------------------------------------------|------------------------------------------|
| Container | ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)                          | 환경 차이 없이 동일한 실행 환경을 보장하기 위해 컨테이너 기반으로 배포 |
| CI/CD     | ![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white) | 테스트 및 배포를 자동화하여 안정적인 릴리즈를 위해 사용          |



<br>

## 📁 프로젝트 패키지 구조

**설계 원칙**

- 도메인형 구조를 따른다.
- `domain`(비즈니스 도메인)과 `global`(도메인 공통 인프라)로 역할을 분리한다.
- 도메인 패키지는 `controller / dto / service / repository / entity / converter / code / exception` 구조를 동일하게 따른다.
```
com.ipillgood.server
├── IPillGoodServerApplication
├── domain                      # 비즈니스 도메인
│   ├── auth                    # 도메인 단위 패키지
│   │   ├── controller          # API 엔드포인트
│   │   │   └── docs            # Swagger 인터페이스 (AuthApi)
│   │   ├── dto                 # 요청/응답 DTO (AuthRequest, AuthResponse)
│   │   ├── service             # 비즈니스 로직
│   │   ├── repository          # 영속성 계층
│   │   ├── entity               # JPA 엔티티
│   │   ├── converter            # Entity ↔ DTO 변환 (AuthConverter)
│   │   ├── code                  # 응답 코드 카탈로그 (Success/Error)
│   │   │   ├── AuthSuccessCode
│   │   │   └── AuthErrorCode
│   │   └── exception            # 도메인 예외 클래스 (AuthException)
│   └── ...                      # member, pill … 도메인 추가 시 동일 구조로
└── global                        # 도메인 공통 인프라
    ├── apiPayload                # 공통 응답 규격
    │   ├── ApiResponse           # 표준 응답 래퍼
    │   ├── code                  # 공통 코드 (Base/General × Success/Error)
    │   ├── exception             # 공통 예외 (GeneralException)
    │   └── handler                # 전역 핸들러 / Advice
    ├── config                     # 설정 (SwaggerConfig …)
    ├── entity                     # 공통 엔티티 (BaseEntity)
    └── ...                        # security, util … 공통 모듈 추가 시
```

<br>

## 📝 코드 컨벤션

### 1. 네이밍 규칙

**기본**

| 대상             | 규칙                | 예시                      |
| ---------------- | ------------------- | ------------------------- |
| 클래스/인터페이스 | `PascalCase`         | `UserController`          |
| 메서드/변수/필드  | `camelCase`          | `userId`                  |
| 상수              | `UPPER_SNAKE_CASE`   | `MAX_RETRY_COUNT`         |
| 패키지            | 전부 소문자(언더스코어 X) | `com.ipillgood.server` |

- 약어는 한 단어처럼 취급하여 첫 글자만 대문자로 쓴다.
    - ✅ `userDto`, `httpClient`, `jsonParser`, `userId`
    - ❌ `userDTO`, `HTTPClient`, `JSONParser`, `userID`
      **계층 접미사**

| 계층      | 접미사        | 예시              |
| --------- | ------------- | ----------------- |
| 컨트롤러  | `Controller`  | `UserController`  |
| 서비스    | `Service`     | `UserService`     |
| 레포지토리 | `Repository`  | `UserRepository`  |

- 인터페이스와 구현은 상황에 따라 분리한다.

### 2. DTO 네이밍 / 구조

- DTO는 도메인별로 **요청·응답 컨테이너를 분리**하고, 그 안에 중첩 `record`로 정의한다.
    - 요청 컨테이너: `XxxRequest`
    - 응답 컨테이너: `XxxResponse`
- DTO가 클래스명에 들어갈 경우 `Dto`로 네이밍한다.
    - `SignUpDTO` ❌ → `SignUpDto` ✅
- 
### 3. API 응답 포맷

모든 컨트롤러 응답은 공통 래퍼 `ApiResponse<T>`로 감싼다.

```java
public record ApiResponse<T>(
        ...
) {
    public static <T> ApiResponse<T> success(...) {
 
        ...
 
    }
    public static <T> ApiResponse<T> fail(...) {
 
        ...
 
    }
}
```

> 구체적인 필드는 프로젝트 세팅 후 별도 안내 예정입니다.

### 4. 예외 처리

- **커스텀 예외 + 글로벌 핸들러** 구조를 사용한다.
- 컨트롤러/서비스에서 `try-catch`로 직접 응답을 만들지 않고, 예외를 던지면 글로벌 핸들러가 응답을 통일한다.
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handle(CustomException e) {
        return ResponseEntity
            .status(e.getStatus())
            .body(ApiResponse.fail(e.getMessage()));
    }
}
```

- 에러 응답 포맷도 위 `ApiResponse`로 통일한다.
- 에러 코드/메시지는 `enum`(예: `ErrorCode`)으로 관리하는 것을 권장한다.

### 5. HTTP 메서드 / URL 규칙 (REST)

- REST 규칙을 따른다.
- 모든 API는 `/api/v1`로 시작한다.
- 리소스는 **소문자 + 복수형 명사**로 표현한다.
- URL에 **동사를 쓰지 않는다.** (단, 상태 변경처럼 HTTP Method로 표현하기 어려운 경우엔 허용한다.)
  | 동작      | 메서드   | URL                     |
  | --------- | -------- | ------------------------ |
  | 목록 조회 | `GET`    | `/api/v1/users`          |
  | 단건 조회 | `GET`    | `/api/v1/users/{id}`     |
  | 생성      | `POST`   | `/api/v1/users`          |
  | 전체 수정 | `PUT`    | `/api/v1/users/{id}`     |
  | 부분 수정 | `PATCH`  | `/api/v1/users/{id}`     |
  | 삭제      | `DELETE` | `/api/v1/users/{id}`     |

- ✅ `GET /api/v1/users/{id}`
- ❌ `GET /api/v1/getUser?id=1`

### 6. 코드 스타일 / IntelliJ 설정

- **저장 시 자동 정리(Actions on Save)**: 불필요한 `import`문은 항상 제거한다.
- **파일 끝 줄바꿈**: POSIX 표준에 따라 파일 마지막 줄은 줄 바꿈으로 끝나도록 한다.
  <br>

## 🌿 브랜치, 커밋, 이슈, PR 컨벤션

### 1️⃣ Branch

`main → develop → feature` 흐름을 따른다.

| 브랜치     | 역할                                        |
| ---------- | ------------------------------------------- |
| `main`     | 배포 가능한 안정 버전. 직접 작업하지 않는다. |
| `develop`  | 개발 통합 브랜치. 기능 작업은 모두 여기로 합친다. |

- 모든 기능 브랜치는 `develop`에서 분기하고, 작업 후 `develop`으로 다시 합친다.
- `develop`에서 충분히 검증된 내용을 마일스톤/배포 시점에 `main`으로 합친다.
```
main        ●────────────────────●  (배포 시점에만 머지)
             \                  /
develop       ●──●──●──●──●──●─●     (개발 통합)
                  \      /
feature/12-...     ●──●            (기능 작업 후 develop으로)
```

**브랜치 네이밍**: `<타입>/<이슈번호>-<작업-내용>`

```
feature/12-jwt-login
fix/23-duplicate-email
refactor/30-user-service
```

- `<타입>/`는 접두사(prefix)로 앞에 붙인다.
- 이슈 번호는 `#` 없이 숫자만 쓴다.
- 작업 내용은 소문자 + 하이픈(`-`)으로 연결한다. (공백/언더스코어 사용 X)
- 영어로 간결하게 작성한다.
  | 타입       | 용도                    |
  | ---------- | ----------------------- |
  | `feature`  | 새로운 기능 개발         |
  | `fix`      | 버그 수정                |
  | `refactor` | 리팩토링 (기능 변화 없음) |
  | `docs`     | 문서 작업                |
  | `test`     | 테스트 코드              |
  | `chore`    | 빌드, 설정 등 기타 작업   |

---

### 2️⃣ Commit

Conventional Commits 기반으로 작성한다.

```
<타입>: <설명>
 
<본문 (선택)>
```

```
feat: JWT 로그인 기능 구현
 
- 기존 세션 방식 대신 JWT 토큰 기반 인증으로 변경했다.
- 액세스 토큰 만료 시간은 1시간으로 설정했다.
```

```
fix: 회원가입 시 중복 이메일 검증 추가
```

| 타입       | 용도                          |
| ---------- | ----------------------------- |
| `feat`     | 새로운 기능 추가                |
| `fix`      | 버그 수정                       |
| `refactor` | 리팩토링 (기능 변화 없음)         |
| `docs`     | 문서 작업                       |
| `test`     | 테스트 코드 추가 / 수정          |
| `chore`    | 빌드, 설정 등 기타 작업          |
| `style`    | 코드 포맷팅 등 (동작에 영향 없는 변경) |
| `perf`     | 성능 개선                       |

**작성 규칙**

- 타입은 소문자 영어, 설명/본문은 한글로 작성한다.
- 타입과 설명 사이에는 콜론(`:`)과 공백 한 칸을 둔다. → `feat: ...`
- 제목(첫 줄)은 50자 이내로 간결하게 작성하고, 마침표는 붙이지 않는다.
- 제목에는 "무엇을 했는지"를 적는다.
- 본문이 필요하면 제목과 본문 사이를 한 줄 비운다.
