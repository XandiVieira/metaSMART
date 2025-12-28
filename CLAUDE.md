# Metasmart Project Guidelines

## Code Style

- **Always format code and clean unused imports** before committing
- **Prefer functional style** (streams, lambdas, Optional) over imperative loops
- **Use `var` for local variables** when the type is obvious from the right-hand side
- **Use meaningful names** for variables, classes, and methods - names should be self-documenting
- **Minimal comments** - only add when logic is not self-evident; code should be self-explanatory
- **Never mention AI, Claude, or any AI assistant** in code, comments, or commit messages

## Logging

- Use **SLF4J with Lombok's @Slf4j annotation** at the top of classes
- Log at appropriate levels: ERROR for exceptions, WARN for recoverable issues, INFO for key events, DEBUG for development

```java
@Slf4j
@Service
public class MyService {
    public void process() {
        log.info("Processing started");
    }
}
```

## Testing

- Use **JUnit 5** with **Mockito** for unit tests
- Always use `@ExtendWith(MockitoExtension.class)` on test classes
- Use `@Mock` to mock dependencies
- **Instantiate real objects/entities** that are needed in tests (don't mock domain objects)
- Test class naming: `{ClassName}Test.java`
- Follow AAA pattern: Arrange, Act, Assert

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnUserWhenFound() {
        // Arrange
        User user = new User("John", "john@email.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        User result = userService.findById(1L);

        // Assert
        assertThat(result.getName()).isEqualTo("John");
    }
}
```

## Exception Handling

- Use **global exception handler** with `@RestControllerAdvice`
- Create **custom exceptions** extending `RuntimeException` for business logic errors
- Exception classes go in `com.relyon.metasmart.exception` package
- All exception messages must be defined in constants

## Constants & Configuration

- **All strings must be in constants or config files** - no hardcoded strings in business logic
- Use `com.relyon.metasmart.constant` package for constants classes
- Use `application.yaml` for externalized configuration
- Group related constants in dedicated classes (e.g., `ErrorMessages`, `ApiPaths`)

## API Documentation (Swagger/OpenAPI)

- Document APIs with **Swagger/OpenAPI** annotations
- Keep documentation **minimal and clean** - only essential tags
- Use `@Operation(summary = "...")` for endpoint description
- Use `@ApiResponse` only for non-obvious response codes
- Avoid verbose descriptions; let method names be descriptive

## Project Structure

```
com.relyon.metasmart
├── config/              # Spring configuration classes
├── constant/            # Constants and static values
├── controller/          # REST controllers
├── entity/              # Entity-based organization
│   ├── AuditableEntity.java  # Base entity with audit fields
│   ├── goal/            # Goal entity package
│   │   ├── Goal.java
│   │   ├── GoalCategory.java
│   │   ├── GoalStatus.java
│   │   └── dto/         # Goal DTOs
│   │       ├── GoalRequest.java
│   │       ├── UpdateGoalRequest.java
│   │       └── GoalResponse.java
│   ├── user/            # User entity package
│   │   ├── User.java
│   │   └── dto/
│   │       ├── RegisterRequest.java
│   │       ├── LoginRequest.java
│   │       └── AuthResponse.java
│   ├── progress/        # Progress tracking
│   │   ├── ProgressEntry.java
│   │   ├── Milestone.java
│   │   └── dto/
│   ├── actionplan/      # Action items for goals
│   │   ├── ActionItem.java
│   │   └── dto/
│   ├── obstacle/        # Obstacle diary entries
│   │   ├── ObstacleEntry.java
│   │   └── dto/
│   └── template/        # Goal templates
│       ├── GoalTemplate.java
│       └── dto/
├── exception/           # Custom exceptions and global handler
├── mapper/              # MapStruct mappers
├── repository/          # Spring Data repositories
└── service/             # Business logic services
```

## Entity Organization

- Each entity type has its own package under `entity/`
- Entity classes go directly in the entity subpackage
- DTOs go in `dto/` subpackage within the entity package
- Enums related to the entity go in the same package
- Naming: `{Action}Request.java` for input, `{Entity}Response.java` for output

## Mapping (MapStruct)

- Use **MapStruct** for DTO ↔ Entity mapping
- Mappers go in `com.relyon.metasmart.mapper` package
- Mapper interfaces use `@Mapper` annotation (Spring component model is configured globally)
- Naming convention: `{Entity}Mapper.java`
- Use `@Mapping(target = "field", ignore = true)` for fields not to be mapped
- Use `@MappingTarget` for update operations
- Inject mappers in services via constructor injection

```java
@Mapper
public interface GoalMapper {
    GoalResponse toResponse(Goal goal);
    Goal toEntity(GoalRequest request);
}
```

## Entities

- **All entities must extend `AuditableEntity`** for automatic audit fields (createdAt, updatedAt, createdBy, updatedBy)
- Use `@SuperBuilder` instead of `@Builder` when extending AuditableEntity
- Entity classes must have `@NoArgsConstructor` and `@AllArgsConstructor`

## General Conventions

- **DTOs for API layer** - never expose entities directly in controllers
- **Constructor injection** over field injection (Lombok's `@RequiredArgsConstructor`)
- **Immutable objects** when possible - use Lombok's `@Builder` and `@Value`
- **Optional** for nullable returns instead of null checks
- **Validate input** at controller level using `@Valid` and Bean Validation

## API Testing (Postman)

- **ALWAYS update the Postman collection** whenever:
  - Adding new endpoints
  - Modifying existing endpoints (URL, method, request/response body)
  - Adding new request/response fields
  - Changing authentication requirements
  - Adding new API folders/features
- Collection location: `postman/Metasmart.postman_collection.json`
- Keep requests organized in folders by feature/domain
- Include test scripts to auto-save tokens on successful auth responses
- Use collection variables for `baseUrl` and `token`
- The collection must always reflect the current state of the API

## Build & Run

```bash
# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Build
./mvnw clean package
```

## Production Readiness

**Before every push to remote**, ensure the application is production ready:

1. **All tests pass** - `./mvnw test`
2. **Build succeeds** - `./mvnw clean package`
3. **No compiler warnings** - clean compile output
4. **No hardcoded secrets** - all sensitive data in environment variables
5. **Logging is appropriate** - no sensitive data logged, proper log levels
6. **Postman collection updated** - reflects current API state

The goal is to release ASAP - keep the codebase always close to or in production-ready state.

## API Base URL

- Context path: `/relyon/metasmart`
- Full base URL: `http://localhost:8080/relyon/metasmart`
- API version prefix: `/api/v1`

## Tech Stack

- Java 21
- Spring Boot 4.0.1
- PostgreSQL
- Spring Security
- Spring Data JPA
- MapStruct
- Lombok
- Maven
