package com.quizz.authservice.service;

import com.quizz.authservice.dto.UserDTO;

import java.util.List;

/**
 * User service interface.
 * Defines operations for user management.
 */
public interface IUserService {

    /**
     * Get user by ID.
     * @param id User ID
     * @return UserDTO with user details
     * @throws com.quizz.authservice.exception.ResourceNotFoundException if user not found
     */
    UserDTO getUserById(Long id);

    /**
     * Get user by email.
     * @param email User email
     * @return UserDTO with user details
     * @throws com.quizz.authservice.exception.ResourceNotFoundException if user not found
     */
    UserDTO getUserByEmail(String email);

    /**
     * Get all users.
     * @return List of all users
     */
    List<UserDTO> getAllUsers();
}
