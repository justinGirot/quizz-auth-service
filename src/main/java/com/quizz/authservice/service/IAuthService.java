package com.quizz.authservice.service;

import com.quizz.authservice.dto.AuthResponse;
import com.quizz.authservice.dto.LoginRequest;
import com.quizz.authservice.dto.RegisterRequest;

/**
 * Authentication service interface.
 * Defines operations for user registration and authentication.
 */
public interface IAuthService {

    /**
     * Register a new user.
     * @param request Registration request with user details
     * @return AuthResponse with user details and JWT token
     * @throws com.quizz.authservice.exception.EmailAlreadyExistsException if email already exists
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate user and generate JWT token.
     * @param request Login request with credentials
     * @return AuthResponse with user details and JWT token
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
     */
    AuthResponse login(LoginRequest request);
}
