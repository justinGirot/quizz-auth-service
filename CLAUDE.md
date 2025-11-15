# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the **Auth Service** for the Quiz Application - a microservices-based system. This service handles user registration, authentication, JWT token management, and user profile operations.

**Technology Stack:**
- Java 21
- Spring Boot 3.4.0
- Spring Security (JWT-based authentication)
- Spring Data JPA
- H2 Database (file-based, production should use PostgreSQL)
- Lombok
- Maven build tool

**Service Details:**
- Port: 8081
- Database: H2 file-based at `./data/auth.db`
- Part of microservices architecture with API Gateway (port 8080)

## Build and Run Commands

### Build the project
```bash
./mvnw clean install
```

### Run the application
```bash
./mvnw spring-boot:run
```

### Run tests
```bash
./mvnw test
```

### Run a single test class
```bash
./mvnw test -Dtest=UserServiceTest
```

### Run a single test method
```bash
./mvnw test -Dtest=UserServiceTest#testUserRegistration
```

### Package the application
```bash
./mvnw package
```

### Run with specific profile
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Architecture Context

This service is part of a larger microservices ecosystem:

**Microservices System:**
- **Frontend** (React + Vite, port 5174) → **API Gateway** (Spring Cloud Gateway, port 8080) → Backend Services
- **Auth Service** (port 8081) - This service
- **Question Service** (port 8082) - Manages quiz questions
- **Quiz Service** (port 8083) - Manages quizzes and sessions

**Communication Flow:**
1. All client requests go through API Gateway at port 8080
2. Gateway routes `/api/auth/**` to this service at port 8081
3. This service generates JWT tokens for authenticated users
4. Other services validate JWT tokens (either directly or via this service)

## Key Responsibilities

This service must implement:

1. **User Registration** (`POST /api/auth/register`)
   - Email validation
   - Password hashing with BCrypt
   - User creation with default role

2. **User Authentication** (`POST /api/auth/login`)
   - Credential validation
   - JWT token generation
   - Session tracking

3. **Token Management**
   - JWT token generation with user details and roles
   - Token validation
   - Token expiration handling

4. **User Profile**
   - Get current user (`GET /api/auth/me`)
   - Update profile (`PUT /api/auth/profile`)

5. **Logout** (`POST /api/auth/logout`)

## Expected Data Models

### User Entity
```java
User {
  Long id;
  String email;           // Unique, validated
  String password;        // BCrypt hashed
  String firstName;
  String lastName;
  LocalDateTime createdAt;
  LocalDateTime lastLogin;
  Set<Role> roles;        // USER, ADMIN, etc.
}
```

### JWT Token Structure
```json
{
  "sub": "user@example.com",
  "userId": 123,
  "roles": ["USER"],
  "exp": 1234567890,
  "iat": 1234567890
}
```

## Configuration Notes

### Required Configuration (application.yml or application.properties)
- Server port: 8081
- Database: H2 file-based at `./data/auth.db`
- JWT secret key (environment variable in production)
- JWT expiration time (default: 24 hours / 86400000 ms)
- CORS configuration (allow API Gateway at localhost:8080)

### Environment Variables for Production
```
SERVER_PORT=8081
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/auth_db
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000
```

## Security Requirements

1. **Password Security:**
   - All passwords must be hashed with BCrypt (strength 10+)
   - Never log or expose passwords
   - Validate password strength on registration

2. **JWT Security:**
   - Use strong secret key (256-bit minimum)
   - Set appropriate expiration times
   - Include only necessary claims
   - Validate tokens on protected endpoints

3. **Input Validation:**
   - Use Spring Validation (@Valid, @NotBlank, @Email, etc.)
   - Sanitize all inputs
   - Return appropriate error messages without exposing internals

4. **CORS:**
   - Configure to allow API Gateway (port 8080)
   - Don't use wildcard (*) in production

## Project Structure

```
src/main/java/com/quizz/authservice/
├── config/
│   ├── properties/     # Configuration properties beans (@ConfigurationProperties)
│   └── *.java         # Security, CORS, JWT configuration classes
├── constant/          # Constants classes (final utility classes)
├── controller/        # REST controllers
├── dto/              # Request/Response DTOs
├── entity/           # JPA entities (User, Role)
├── repository/       # Spring Data JPA repositories
├── service/          # Business logic and domain services
├── security/         # JWT utilities, UserDetails implementation, filters
└── exception/        # Custom exceptions and handlers

src/main/resources/
├── application.yml          # Main configuration
├── application-dev.yml      # Dev profile
└── application-prod.yml     # Production profile
```

## Clean Code Principles & Best Practices

**IMPORTANT:** This project follows clean code and clean architecture principles. All code must adhere to these standards.

### 1. Configuration Management

**✅ DO:**
- Use `@ConfigurationProperties` beans for all configuration
- Place properties classes in `config/properties/` package
- Validate properties with Jakarta validation annotations (`@NotBlank`, `@Positive`, etc.)
- Inject properties beans via constructor injection
- Group related properties in nested static classes

**❌ DON'T:**
- Use `@Value` annotations scattered throughout the code
- Hardcode configuration values in service/controller classes
- Use magic numbers or strings

**Example:**
```java
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    @NotBlank private String secret;
    @Positive private long expiration;

    private CookieProperties cookie = new CookieProperties();

    @Data
    public static class CookieProperties {
        @NotBlank private String name = "token";
        @Positive private int maxAge = 604800;
        private boolean httpOnly = true;
    }
}
```

### 2. Constants Management

**✅ DO:**
- Create dedicated constants classes in `constant/` package
- Use `public static final` for constants
- Make constants classes `final` with private constructor
- Group related constants in nested static classes
- Document each constant with Javadoc

**❌ DON'T:**
- Scatter constants across multiple classes
- Use magic strings or numbers inline
- Create public constants in non-constant classes

**Example:**
```java
public final class SecurityConstants {

    private SecurityConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String TOKEN_PREFIX = "Bearer ";

    public static final class Cookie {
        private Cookie() {
            throw new UnsupportedOperationException("Utility class");
        }

        public static final String SET_COOKIE_HEADER = "Set-Cookie";
        public static final String AUTH_COOKIE_FORMAT = "%s=%s; HttpOnly; %sPath=%s; Max-Age=%d; SameSite=%s";
    }
}
```

### 3. Service Layer Principles

**✅ DO:**
- Create dedicated service classes for specific responsibilities
- Follow Single Responsibility Principle (SRP)
- Inject dependencies via constructor (use `@RequiredArgsConstructor`)
- Keep controllers thin - business logic belongs in services
- Document service methods with Javadoc

**❌ DON'T:**
- Put business logic in controllers
- Create "God" services that do everything
- Use field injection (`@Autowired` on fields)

**Example:**
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CookieService {
    private final JwtProperties jwtProperties;
    private final AppProperties appProperties;

    public void setAuthenticationCookie(HttpServletResponse response, String token) {
        // Cookie logic here
    }
}
```

### 4. Dependency Injection

**✅ DO:**
- Use constructor injection exclusively
- Use Lombok's `@RequiredArgsConstructor` for cleaner code
- Inject interfaces, not concrete implementations (when applicable)
- Mark dependencies as `private final`

**❌ DON'T:**
- Use field injection (`@Autowired` on fields)
- Use setter injection
- Create dependencies with `new` keyword in business code

### 5. Controller Design

**✅ DO:**
- Keep controllers thin and focused on HTTP concerns
- Delegate business logic to service layer
- Use proper HTTP status codes
- Document endpoints with OpenAPI annotations
- Inject only necessary services

**❌ DON'T:**
- Put business logic in controllers
- Use `@Value` annotations in controllers
- Create helper methods for business logic

### 6. Code Organization

**✅ DO:**
- Follow package-by-feature or package-by-layer consistently
- Keep related code together
- Use meaningful package names
- Maintain clear separation of concerns

**❌ DON'T:**
- Mix different architectural layers
- Create circular dependencies
- Put everything in one package

### 7. Authentication & Cookie Management

**Current Implementation:**
- JWT tokens are stored in **httpOnly cookies** (not Authorization headers)
- Cookie configuration is in `JwtProperties.CookieProperties`
- Cookie operations are handled by `CookieService`
- `JwtAuthenticationFilter` reads tokens from cookies (with fallback to Authorization header)

**Cookie Security Settings:**
- `HttpOnly`: Prevents JavaScript access (XSS protection)
- `Secure`: HTTPS only in production
- `SameSite=Strict`: CSRF protection
- `Max-Age`: 7 days (configurable)

## Integration with Other Services

**Service-to-Service Communication:**
- Question Service and Quiz Service may call this service to validate tokens
- Consider creating a `/api/auth/validate` endpoint for token validation
- User IDs from this service are referenced in other services

**Database Isolation:**
- This service owns the User database
- Other services should NEVER directly access this database
- User information is shared via APIs only

## Health Check

Spring Boot Actuator is included. Health endpoint:
```bash
curl http://localhost:8081/actuator/health
```

## Testing Strategy

1. **Unit Tests:** Test services, repositories, and utilities in isolation
2. **Integration Tests:** Test controllers with @SpringBootTest and @WebMvcTest
3. **Security Tests:** Use Spring Security Test for authentication/authorization
4. **Test Database:** Use H2 in-memory database for tests (@DataJpaTest)

## Common Issues to Avoid

1. Don't store JWT tokens in the database (stateless authentication)
2. Don't include passwords in API responses
3. Don't use the same secret key across environments
4. Ensure Lombok annotation processors are configured in Maven
5. Remember that this service runs behind API Gateway - base path includes `/api/auth`

## Related Documentation

- See ARCHITECTURE.md for complete microservices system design
- See HELP.md for Spring Boot reference documentation
- Full system architecture available at: https://github.com/justinGirot (Quizz repositories)
