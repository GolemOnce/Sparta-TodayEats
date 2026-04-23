
## Code Convention 
### 1. 네이밍 규칙

| 구분 | 규칙 | 예시 |
|------|------|------|
| 클래스 | PascalCase | UserController |
| 메서드 | camelCase + 동사 시작 | createUser(), findUserById(), updateUser(), deleteUser() |
| 변수 | camelCase | userId |
| Boolean 변수 | is / has 접두어 | isActive, hasPermission |
| 상수 | SCREAMING_SNAKE_CASE | MAX_PAGE_SIZE |
| Enum | PascalCase | UserStatus |
| Enum 값 | SCREAMING_SNAKE_CASE | ACTIVE, INACTIVE |
| DTO | PascalCase + Request/Response | UserRequest, UserResponse |

---

### 2. 계층별 규칙

#### Controller

- 요청을 받아 Service로 전달 후 응답 반환
- 비즈니스 로직, 조건 처리 금지


**성공**
```json
{
  "status": 200,
  "message": "SUCCESS",
  "data": {}
}
```
실패

```json
{
  "status": 400,
  "message": "VALIDATION_ERROR",
  "errors": []
}
```

목록 조회
```json
{
    "status": 200,
    "message": "SUCCESS",
    "data": {
        "content": [],
        "page": 0,
        "size": 10,
        "totalElements": 0,
        "totalPages": 0,
        "sort": "createdAt,DESC"
    }
}
```
상태 코드 규칙

- 201: CREATED
- 204: NO_CONTENT
- 200: SUCCESS

---
#### Service

- 트랜잭션 단위로 비즈니스 로직 처리
- 도메인 핵심 로직 담당

```java
@Transactional(readOnly = true)
public class UserService {

    public UserResponse findUser(Long id) { }

    @Transactional
    public Long createUser(UserRequest request) { }
}

```
---
#### Repository

- JPA 메서드 우선 사용
- 복잡한 쿼리는 QueryDSL 또는 @Query 사용

```java
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByStatus(UserStatus status);

    boolean existsByEmail(String email);

    @Query("select u from User u where u.age >= :age")
    Page<User> findUsersOlderThan(@Param("age") int age, Pageable pageable);
}
```

---
### 3. 로깅

- Slf4j 사용

``` java
log.info("User created: {}", userId);
```


### 4. 테스트 코드

- JUnit5 + Mockito 사용
- Service / Repository 단위 테스트 필수
- Given - When - Then 패턴 사용

```java
@DisplayName("UserService 테스트")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Nested
    @DisplayName("findUser()")
    class FindUser {

        @Test
        @DisplayName("성공 - 사용자 조회")
        void success() {
            // given
            Long userId = 1L;
            User user = new User(userId, "test@email.com");
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            UserResponse result = userService.findUser(userId);

            // then
            assertThat(result.id()).isEqualTo(userId);
        }

        @Test
        @DisplayName("실패 - 사용자 없음")
        void fail_user_not_found() {
            // given
            Long userId = 1L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.findUser(userId))
                .isInstanceOf(UserNotFoundException.class);
        }
    }
}
```

---
## Github Rules

### 1. 브랜치 전략

* `main` : 운영(배포) 브랜치 (CI/CD 트리거)
* `develop` : 개발 통합 브랜치 (CI 트리거)
* `feature/#이슈번호-*` : 기능 개발 브랜치
    * 예: `feature/#5-login`, `feature/6-payment-api`
* `fix/#이슈번호-*` : 버그 수정 브랜치
* `refactor/#이슈번호-*` : 리팩토링 브랜치

---

### 2. 브랜치 흐름

1. `develop`에서 `feature/#이슈번호-*` 브랜치 생성
2. 기능 개발 후 `develop`으로 PR (PR시 `Closes #이슈번호`)
3. 코드 리뷰(CodeRabbit활용) 후 머지
4. `develop` -> `main`으로 PR, 머지(CI/CD 자동 배포)

---

### 3. 커밋 메시지 규칙

```
타입: 간단한 설명

(선택) 상세 설명
```

- 커밋 타입

| 타입                 | 설명                   |
| ------------------ | -------------------- |
| ✨ feat             | 새로운 기능 추가            |
| 🐛 fix             | 버그 수정                |
| 📄 docs            | 문서 수정                |
| 🔤 style           | 코드 스타일 변경 (로직 변경 없음) |
| ♻️ refactor        | 리팩토링                 |
| 🎥 test            | 테스트 코드               |
| 🛠️ chore          | 빌드/설정/기타             |
| 👗 design          | UI/스타일 변경            |
| 💬 comment         | 주석 추가/수정             |
| 🚚 rename          | 파일/폴더 이름 변경          |
| 🔥 remove          | 파일 삭제                |
| 💥 BREAKING CHANGE | 호환성 깨지는 변경           |
| ⚠️ HOTFIX          | 긴급 수정                |

---

### 4. Pull Request 규칙

* 제목: `[타입] 작업 내용`
    * 예: `[feat] 로그인 API 구현`
* 내용: 
* 최소 1명 이상 리뷰 후 merge
* `rebase merge` 사용
* 가능하면 작은 단위로 PR 생성

---

### 5. Pull 규칙
* pull시 rebase merge
```bash
// 기본 설정 변경
git config --global pull.rebase true

// 혹은 --rebase 옵션 추가
git pull --rebase origin <브랜치명>
```


---

### 6. 금지 사항

* `main`에 직접 push 금지
* 너무 큰 PR 금지
* 의미 없는 커밋 메시지 금지

    * 예: `수정`, `fix`, `commit`
