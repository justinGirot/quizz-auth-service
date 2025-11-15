package com.quizz.authservice.dto;

import com.quizz.authservice.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User data transfer object")
public class UserDTO {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "User email address", example = "user@example.com")
    private String email;

    @Schema(description = "User first name", example = "John")
    private String firstName;

    @Schema(description = "User last name", example = "Doe")
    private String lastName;

    @Schema(description = "User roles")
    private Set<String> roles;

    @Schema(description = "Account creation timestamp", example = "2025-11-15T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last login timestamp", example = "2025-11-15T10:00:00")
    private LocalDateTime lastLogin;
}
