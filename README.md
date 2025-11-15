# Quiz Auth Service

Authentication and user management service for the Quiz Application microservices system.

## Features

- ✅ User registration and authentication
- ✅ JWT token generation and validation
- ✅ Role-based access control (USER, ADMIN, MODERATOR)
- ✅ Eureka service discovery integration
- ✅ OpenAPI/Swagger documentation
- ✅ Comprehensive exception handling
- ✅ H2 database for development
- ✅ Spring Security with BCrypt password hashing

## Technology Stack

- **Java**: 21
- **Spring Boot**: 3.4.0
- **Spring Cloud**: 2024.0.0 (Eureka Client)
- **Security**: Spring Security + JWT (io.jsonwebtoken 0.12.5)
- **Database**: H2 (file-based)
- **API Documentation**: SpringDoc OpenAPI 2.3.0
- **Build Tool**: Maven
- **Code Reduction**: Lombok

## Prerequisites

- Java 21 or higher
- Maven 3.6+ (or use the included Maven Wrapper)
- Eureka Server running on port 8761 (optional for local development)

## Configuration

### Application Properties

The service is configured via `application.yml`:

- **Server Port**: 8081
- **Database**: H2 file-based at `./data/auth`
- **Eureka Server**: http://localhost:8761/eureka/
- **JWT Secret**: Configurable via environment variable `JWT_SECRET`
- **JWT Expiration**: 24 hours (86400000 ms)

### Environment Variables

For production, set the following environment variables:

```bash
JWT_SECRET=your-secret-key-here-at-least-256-bits
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/auth_db
SPRING_DATASOURCE_USERNAME=your-db-username
SPRING_DATASOURCE_PASSWORD=your-db-password
```

## Build and Run

### Using Maven Wrapper (Recommended)

```bash
# Clean and build
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
./mvnw test
```

### Using Maven

```bash
# Clean and build
mvn clean install

# Run the application
mvn spring-boot:run

# Run tests
mvn test
```

### Using Java

```bash
# Build the JAR
./mvnw clean package

# Run the JAR
java -jar target/auth-service-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Authentication Endpoints (Public)

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}

Response (201 Created):
{
  "user": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["ROLE_USER"],
    "createdAt": "2025-11-15T10:00:00"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Login User
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response (200 OK):
{
  "user": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["ROLE_USER"],
    "createdAt": "2025-11-15T10:00:00",
    "lastLogin": "2025-11-15T11:30:00"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Logout User
```http
POST /api/auth/logout
Authorization: Bearer <token>

Response (200 OK):
{
  "message": "Logged out successfully"
}
```

### User Endpoints (Authenticated)

#### Get User by ID
```http
GET /api/users/{id}
Authorization: Bearer <token>

Response (200 OK):
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["ROLE_USER"],
  "createdAt": "2025-11-15T10:00:00"
}
```

#### Get User by Email
```http
GET /api/users/email/{email}
Authorization: Bearer <token>
```

#### Get All Users (Admin Only)
```http
GET /api/users
Authorization: Bearer <token>
```

## API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/api-docs

## Database Access

H2 Console is available for development:

- **URL**: http://localhost:8081/h2-console
- **JDBC URL**: jdbc:h2:file:./data/auth
- **Username**: sa
- **Password**: (leave empty)

## Health Check

```http
GET /actuator/health

Response:
{
  "status": "UP"
}
```

## Security

### JWT Token Structure

```json
{
  "sub": "user@example.com",
  "userId": 1,
  "roles": "ROLE_USER",
  "iat": 1700000000,
  "exp": 1700086400
}
```

### Password Security

- Passwords are hashed using BCrypt with strength 10
- Passwords must be at least 6 characters (configurable in RegisterRequest)
- Never logged or exposed in responses

### CORS Configuration

The service allows requests from:
- http://localhost:5173
- http://localhost:5174
- http://localhost:3000
- http://localhost:8080 (API Gateway)

## Roles

The service supports three roles:
- **ROLE_USER**: Default role for registered users
- **ROLE_ADMIN**: Administrative access
- **ROLE_MODERATOR**: Moderate access

Roles are initialized automatically on application startup.

## Error Responses

### Validation Error (400)
```json
{
  "email": "Email should be valid",
  "password": "Password must be at least 6 characters"
}
```

### Email Already Exists (400)
```json
{
  "message": "User with this email already exists"
}
```

### Invalid Credentials (401)
```json
{
  "message": "Invalid email or password"
}
```

### Resource Not Found (404)
```json
{
  "message": "User not found with id: 123"
}
```

## Integration with API Gateway

This service is designed to work behind an API Gateway (port 8080):

```
Frontend → API Gateway (8080) → Auth Service (8081)
          /api/auth/**
```

## Eureka Service Discovery

The service registers itself with Eureka Server:

- **Service Name**: auth-service
- **Instance ID**: Based on hostname and port
- **Health Check**: Enabled via Spring Boot Actuator

To run without Eureka (for local development):
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--eureka.client.enabled=false
```

## Project Structure

```
src/main/java/com/quizz/authservice/
├── config/              # Configuration classes
│   ├── SecurityConfig.java
│   ├── OpenApiConfig.java
│   └── DataInitializer.java
├── controller/          # REST controllers
│   ├── AuthController.java
│   └── UserController.java
├── dto/                # Data Transfer Objects
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── AuthResponse.java
│   ├── UserDTO.java
│   └── MessageResponse.java
├── entity/             # JPA entities
│   ├── User.java
│   └── Role.java
├── repository/         # Spring Data repositories
│   ├── UserRepository.java
│   └── RoleRepository.java
├── security/           # JWT and security components
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── UserDetailsImpl.java
│   └── UserDetailsServiceImpl.java
├── service/            # Business logic
│   ├── AuthService.java
│   └── UserService.java
├── exception/          # Custom exceptions
│   ├── ResourceNotFoundException.java
│   ├── EmailAlreadyExistsException.java
│   └── GlobalExceptionHandler.java
└── AuthServiceApplication.java
```

## Testing

```bash
# Run all tests
./mvnw test

# Run specific test
./mvnw test -Dtest=AuthServiceApplicationTests

# Run with coverage
./mvnw test jacoco:report
```

## Troubleshooting

### Issue: JAVA_HOME not set

**Solution**: Set JAVA_HOME environment variable:
```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-21

# Linux/Mac
export JAVA_HOME=/usr/lib/jvm/java-21
```

### Issue: Eureka Connection Refused

**Solution**: Either start Eureka Server or disable Eureka:
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--eureka.client.enabled=false
```

### Issue: Port 8081 already in use

**Solution**: Change the port in application.yml or use environment variable:
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8082
```

## Development Tips

1. **Auto-reload**: Use Spring Boot DevTools for auto-restart during development
2. **Database**: Data is persisted in `./data/auth.mv.db` file
3. **Logs**: Set logging level in application.yml for debugging
4. **Testing**: Use H2 in-memory database for tests (configured in test resources)

## Production Deployment

For production deployment:

1. Use PostgreSQL or MySQL instead of H2
2. Set strong JWT secret (at least 256 bits)
3. Enable HTTPS
4. Configure proper CORS origins
5. Set up rate limiting
6. Enable distributed tracing (Spring Cloud Sleuth + Zipkin)
7. Use environment-specific configuration files

## Related Services

- **API Gateway**: Port 8080
- **Question Service**: Port 8082
- **Quiz Service**: Port 8083
- **Eureka Server**: Port 8761

## License

This project is part of the Quiz Application microservices system.

## Support

For issues and questions, please refer to the main architecture documentation in `ARCHITECTURE.md` or `CLAUDE.md`.
