# API Integration Guide

This document explains how to integrate the frontend with a Spring Boot backend using **httpOnly cookies** for secure authentication.

## Current State

The frontend is configured to use **httpOnly cookies** for JWT token storage (more secure than localStorage).

- **Authentication tokens**: Stored in httpOnly cookies (managed by backend)
- **User data**: Stored in localStorage (non-sensitive data only)
- **Mock mode**: Available for development (set `USE_MOCK = true` in authService.js)

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

The frontend expects the following REST API endpoints with **httpOnly cookie authentication**:

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
Set-Cookie: token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=604800

{
  "user": {
    "id": 1,
    "email": "user@example.com",
    "createdAt": "2025-11-15T10:00:00Z"
  }
}

Error Response (400 Bad Request):
{
  "message": "User with this email already exists"
}

IMPORTANT:
- Token must be set as an httpOnly cookie (NOT in response body)
- Cookie should have Secure flag (HTTPS only in production)
- Use SameSite=Strict or Lax for CSRF protection
- Max-Age: 604800 seconds = 7 days (adjust as needed)
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
Set-Cookie: token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=604800

{
  "user": {
    "id": 1,
    "email": "user@example.com",
    "createdAt": "2025-11-15T10:00:00Z"
  }
}

Error Response (401 Unauthorized):
{
  "message": "Invalid email or password"
}

IMPORTANT:
- Token must be set as an httpOnly cookie (NOT in response body)
- Same cookie settings as register endpoint
```

#### 3. Logout User
```
POST /api/auth/logout
Cookie: token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

Response (200 OK):
Set-Cookie: token=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0

{
  "message": "Logged out successfully"
}

IMPORTANT:
- Clear the cookie by setting Max-Age=0 or Expires to past date
- Frontend automatically sends cookie with request (credentials: 'include')
```

## Spring Boot Configuration Required

### CORS Configuration

**CRITICAL**: When using httpOnly cookies, you MUST enable credentials in CORS:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // Specify exact origins - cannot use "*" with credentials
                .allowedOrigins("http://localhost:5174", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)  // REQUIRED for cookies
                .exposedHeaders("Set-Cookie");
    }
}
```

**IMPORTANT NOTES:**
- `allowCredentials(true)` is REQUIRED to send/receive cookies
- Cannot use `allowedOrigins("*")` when credentials are enabled
- Must specify exact frontend origins
- Add production URLs before deploying

### Security Configuration

Spring Security configuration for httpOnly cookie authentication:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for API endpoints (using SameSite cookie protection instead)
            // If you want CSRF protection, enable it and configure accordingly
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // Add custom JWT filter to read token from cookie
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### Setting httpOnly Cookies in Controller

Here's how to set the JWT token as an httpOnly cookie in your authentication controller:

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody RegisterRequest request,
            HttpServletResponse response) {

        try {
            // Create user
            User user = authService.register(request.getEmail(), request.getPassword());

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail());

            // Set httpOnly cookie
            setAuthCookie(response, token);

            // Return user data (NOT the token)
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("user", new UserDTO(user));

            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response) {

        try {
            // Authenticate user
            User user = authService.login(request.getEmail(), request.getPassword());

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail());

            // Set httpOnly cookie
            setAuthCookie(response, token);

            // Return user data (NOT the token)
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("user", new UserDTO(user));

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        // Clear the cookie by setting Max-Age to 0
        Cookie cookie = new Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(0); // Delete cookie
        response.addCookie(cookie);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Logged out successfully");
        return ResponseEntity.ok(responseBody);
    }

    /**
     * Helper method to set authentication cookie
     */
    private void setAuthCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);  // Cannot be accessed by JavaScript
        cookie.setSecure(false);   // Set to true in production (HTTPS only)
        cookie.setPath("/");       // Available for all paths
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days in seconds
        // cookie.setSameSite("Strict"); // Requires Servlet API 6.0+ or manual header
        response.addCookie(cookie);

        // For SameSite with older Servlet API, set header manually:
        response.addHeader("Set-Cookie",
            String.format("token=%s; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=%d",
                token, 7 * 24 * 60 * 60));
    }
}
```

### Reading Token from Cookie

Create a JWT filter to read the token from the cookie:

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract token from cookie
            String token = getTokenFromCookie(request);

            if (token != null && jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from httpOnly cookie
     */
    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
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

### httpOnly Cookie Security

**Why httpOnly cookies are more secure:**
- ✅ **XSS Protection**: JavaScript cannot access the token, even if XSS attack occurs
- ✅ **Automatic transmission**: Browser handles cookie sending/receiving
- ✅ **Secure flag**: Enforce HTTPS-only transmission in production
- ✅ **SameSite protection**: Prevents CSRF attacks when set to Strict/Lax

**Important security measures:**
- ✅ **Never store passwords in plain text** - use BCrypt or Argon2
- ✅ **Use HTTPS in production** - set `Secure` flag on cookies
- ✅ **Set SameSite=Strict** - prevents CSRF attacks
- ✅ **Implement proper JWT token validation** - verify signature, expiration
- ✅ **Add rate limiting for auth endpoints** - prevent brute force attacks
- ✅ **Validate all input on the backend** - email format, password strength
- ✅ **Set appropriate cookie expiration** - don't make tokens last forever
- ✅ **Enable CORS credentials properly** - only allow trusted origins

### Development vs Production

**Development (localhost):**
```java
cookie.setSecure(false); // HTTP is OK for localhost
```

**Production:**
```java
cookie.setSecure(true);  // HTTPS only
// Use environment variable:
cookie.setSecure(!environment.getProperty("app.env").equals("development"));
```

### CSRF Protection

With `SameSite=Strict` cookies, you get CSRF protection without additional tokens. However, for extra security in production, consider:

1. **SameSite=Strict**: Best protection, but may break some legitimate cross-site scenarios
2. **SameSite=Lax**: Good protection, allows some safe cross-site requests
3. **CSRF Tokens**: Additional layer if not using SameSite

### Token Expiration

- **Short-lived tokens** (15-60 minutes): More secure, requires refresh token mechanism
- **Medium-lived tokens** (7 days): Balance of security and UX (recommended)
- **Long-lived tokens** (30+ days): Convenient but less secure

Consider implementing refresh tokens for better security with longer sessions.
