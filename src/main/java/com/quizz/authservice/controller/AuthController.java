package com.quizz.authservice.controller;

import com.quizz.authservice.dto.AuthResponse;
import com.quizz.authservice.dto.LoginRequest;
import com.quizz.authservice.dto.MessageResponse;
import com.quizz.authservice.dto.RegisterRequest;
import com.quizz.authservice.service.AuthService;
import com.quizz.authservice.service.CookieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for authentication endpoints.
 * Handles user registration, login, and logout with JWT cookie-based authentication.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and user registration endpoints")
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Create a new user account with email and password. JWT token is set as httpOnly cookie."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully. JWT token set in httpOnly cookie.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "User with this email already exists or validation error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {
        log.info("Registration request received for email: {}", request.getEmail());
        AuthResponse authResponse = authService.register(request);

        // Set JWT token as httpOnly cookie
        cookieService.setAuthenticationCookie(response, authResponse.getToken());

        // Return only user data (NOT the token)
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("user", authResponse.getUser());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description = "Authenticate user with email and password. JWT token is set as httpOnly cookie."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful. JWT token set in httpOnly cookie.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid email or password",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        log.info("Login request received for email: {}", request.getEmail());
        AuthResponse authResponse = authService.login(request);

        // Set JWT token as httpOnly cookie
        cookieService.setAuthenticationCookie(response, authResponse.getToken());

        // Return only user data (NOT the token)
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("user", authResponse.getUser());

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout user",
            description = "Logout current user by clearing the authentication cookie",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logged out successfully. Authentication cookie cleared.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            )
    })
    public ResponseEntity<MessageResponse> logout(HttpServletResponse response) {
        log.info("Logout request received");

        // Clear the authentication cookie
        cookieService.clearAuthenticationCookie(response);

        return ResponseEntity.ok(MessageResponse.builder()
                .message("Logged out successfully")
                .build());
    }

    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Check if the auth service is running"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Service is healthy",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageResponse.class)
            )
    )
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Auth service is running")
                .build());
    }
}
