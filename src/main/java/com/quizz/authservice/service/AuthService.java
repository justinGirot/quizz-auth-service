package com.quizz.authservice.service;

import com.quizz.authservice.constant.ErrorMessages;
import com.quizz.authservice.dto.AuthResponse;
import com.quizz.authservice.dto.LoginRequest;
import com.quizz.authservice.dto.RegisterRequest;
import com.quizz.authservice.entity.Role;
import com.quizz.authservice.entity.User;
import com.quizz.authservice.exception.EmailAlreadyExistsException;
import com.quizz.authservice.mapper.UserMapper;
import com.quizz.authservice.repository.RoleRepository;
import com.quizz.authservice.repository.UserRepository;
import com.quizz.authservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authentication service implementation.
 * Handles user registration and authentication operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(ErrorMessages.EMAIL_ALREADY_EXISTS);
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        // Assign default role
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.DEFAULT_ROLE_NOT_FOUND));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Generate JWT token
        String jwt = generateTokenForUser(savedUser);

        return AuthResponse.builder()
                .user(UserMapper.toDTO(savedUser))
                .token(jwt)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException(ErrorMessages.USER_NOT_FOUND));

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in successfully: {}", request.getEmail());

        return AuthResponse.builder()
                .user(UserMapper.toDTO(user))
                .token(jwt)
                .build();
    }

    /**
     * Generate JWT token for user.
     * @param user User entity
     * @return JWT token string
     */
    private String generateTokenForUser(User user) {
        String roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.joining(","));

        return tokenProvider.generateTokenFromEmail(user.getEmail(), user.getId(), roles);
    }
}
