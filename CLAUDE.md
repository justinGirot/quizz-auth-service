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

## Project Structure (When Implemented)

```
src/main/java/com/quizz/authservice/
├── config/          # Security, CORS, JWT configuration
├── controller/      # REST controllers
├── dto/            # Request/Response DTOs
├── entity/         # JPA entities (User, Role)
├── repository/     # Spring Data JPA repositories
├── service/        # Business logic
├── security/       # JWT utilities, UserDetails implementation
└── exception/      # Custom exceptions and handlers

src/main/resources/
├── application.yml          # Main configuration
├── application-dev.yml      # Dev profile
└── application-prod.yml     # Production profile
```

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
