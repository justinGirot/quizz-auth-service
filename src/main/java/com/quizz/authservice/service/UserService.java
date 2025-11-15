package com.quizz.authservice.service;

import com.quizz.authservice.constant.ErrorMessages;
import com.quizz.authservice.dto.UserDTO;
import com.quizz.authservice.entity.User;
import com.quizz.authservice.exception.ResourceNotFoundException;
import com.quizz.authservice.mapper.UserMapper;
import com.quizz.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User service implementation.
 * Handles user management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        log.debug("Getting user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.format(ErrorMessages.USER_NOT_FOUND_WITH_ID, id)));
        return UserMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        log.debug("Getting user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.format(ErrorMessages.USER_NOT_FOUND_WITH_EMAIL, email)));
        return UserMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.debug("Getting all users");
        return userRepository.findAll().stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }
}
