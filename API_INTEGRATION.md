# API Integration Guide

This document explains how to integrate the frontend with a Spring Boot backend.

## Current State

The frontend is currently using **mock mode** which simulates API calls by storing data in localStorage. This allows development to continue while the backend is being implemented.

## Project Structure

```
src/
├── services/
│   ├── api.js          # Base API client with HTTP methods
│   └── authService.js  # Authentication service (login, register, logout)
├── components/
│   └── Auth.jsx        # UI-only authentication component
└── App.jsx             # Main app component
```

## Switching to Real API

To switch from mock mode to the real Spring Boot backend:

1. **Set the environment variable**:
   - Copy `.env.example` to `.env`
   - Update `VITE_API_BASE_URL` to point to your Spring Boot backend

2. **Disable mock mode**:
   - Open `src/services/authService.js`
   - Change `const USE_MOCK = true` to `const USE_MOCK = false`

3. **Ensure Spring Boot is running** on the configured port (default: 8080)

## Expected Spring Boot API Endpoints

The frontend expects the following REST API endpoints:

### Authentication Endpoints

#### 1. Register User
```
POST /api/auth/register
Content-Type: application/json

Request Body:
{
  "email": "user@example.com",
  "password": "password123"
}

Response (201 Created):
{
  "user": {
    "id": 1,
    "email": "user@example.com",
    "createdAt": "2025-11-15T10:00:00Z"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Error Response (400 Bad Request):
{
  "message": "User with this email already exists"
}
```

#### 2. Login User
```
POST /api/auth/login
Content-Type: application/json

Request Body:
{
  "email": "user@example.com",
  "password": "password123"
}

Response (200 OK):
{
  "user": {
    "id": 1,
    "email": "user@example.com",
    "createdAt": "2025-11-15T10:00:00Z"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Error Response (401 Unauthorized):
{
  "message": "Invalid email or password"
}
```

#### 3. Logout User
```
POST /api/auth/logout
Authorization: Bearer <token>

Response (200 OK):
{
  "message": "Logged out successfully"
}
```

## Spring Boot Configuration Required

### CORS Configuration

Add CORS configuration to allow the frontend to connect:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5174", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

### Security Configuration

If using Spring Security, ensure proper configuration for JWT tokens:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }
}
```

## Data Models

### User Entity (Spring Boot)

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // Should be hashed with BCrypt

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters and setters
}
```

### DTOs

```java
// LoginRequest.java
public class LoginRequest {
    private String email;
    private String password;
    // Getters and setters
}

// RegisterRequest.java
public class RegisterRequest {
    private String email;
    private String password;
    // Getters and setters
}

// AuthResponse.java
public class AuthResponse {
    private UserDTO user;
    private String token;
    // Getters and setters
}

// UserDTO.java
public class UserDTO {
    private Long id;
    private String email;
    private LocalDateTime createdAt;
    // Getters and setters (no password!)
}
```

## File Storage

For storing user data in a file (as requested), you can implement a simple JSON file storage:

```java
@Service
public class FileStorageService {

    private final String FILE_PATH = "users.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<User> loadUsers() {
        try {
            File file = new File(FILE_PATH);
            if (file.exists()) {
                return objectMapper.readValue(file,
                    new TypeReference<List<User>>() {});
            }
            return new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load users", e);
        }
    }

    public void saveUsers(List<User> users) {
        try {
            objectMapper.writeValue(new File(FILE_PATH), users);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save users", e);
        }
    }
}
```

## Testing the Integration

1. Start your Spring Boot backend on port 8080
2. Start the frontend with `npm run dev`
3. Try creating a new account - the frontend will send a POST request to your backend
4. Check the Spring Boot logs to see the incoming requests
5. Verify user data is stored in the file/database

## Frontend API Client Features

The `api.js` client provides:

- **Automatic token handling**: Adds JWT token to Authorization header
- **Error handling**: Catches and formats API errors
- **JSON serialization**: Automatically converts request/response data
- **HTTP methods**: GET, POST, PUT, DELETE
- **Base URL configuration**: Set via environment variable

## Next Steps

1. Create the Spring Boot backend project
2. Implement the authentication endpoints
3. Add JWT token generation and validation
4. Implement file-based storage or use a database (H2, PostgreSQL, etc.)
5. Test the integration with the frontend
6. Add password hashing (BCrypt)
7. Add input validation and error handling

## Security Considerations

- **Never store passwords in plain text** - use BCrypt or similar
- **Use HTTPS in production**
- **Implement proper JWT token validation**
- **Add rate limiting for auth endpoints**
- **Validate all input on the backend**
- **Use secure session management**
