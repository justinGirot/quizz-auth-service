package com.quizz.authservice.mapper;

import com.quizz.authservice.dto.UserDTO;
import com.quizz.authservice.entity.User;
import lombok.experimental.UtilityClass;

import java.util.stream.Collectors;

/**
 * Mapper utility for converting between User entities and DTOs.
 * Uses utility class pattern for stateless mapping operations.
 */
@UtilityClass
public class UserMapper {

    /**
     * Convert User entity to UserDTO.
     * @param user User entity
     * @return UserDTO with mapped fields
     */
    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles() != null ?
                        user.getRoles().stream()
                                .map(role -> role.getName().name())
                                .collect(Collectors.toSet()) :
                        null)
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    /**
     * Convert User entity to UserDTO with minimal information.
     * Useful for lists and when full details are not needed.
     * @param user User entity
     * @return UserDTO with basic fields only
     */
    public static UserDTO toBasicDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
