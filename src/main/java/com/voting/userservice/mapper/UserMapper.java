package com.voting.userservice.mapper;

import com.voting.userservice.dto.RegisterRequest;
import com.voting.userservice.dto.UserResponseDTO;
import com.voting.userservice.model.User;
import org.springframework.stereotype.Component;

/**
 * Maps between User entity and DTOs.
 * <p>
 * Design note: Password is intentionally NOT mapped here.
 * The service layer is responsible for hashing and setting it,
 * keeping security concerns out of the mapper.
 * </p>
 */
@Component
public class UserMapper {

    /**
     * Converts a RegisterRequest DTO to a User entity.
     * <p>
     * Password is excluded — the service layer handles BCrypt hashing
     * before setting it on the entity. This enforces the principle
     * that mappers are pure data transformers with no security logic.
     * </p>
     *
     * @param request the registration request DTO
     * @return a partially-built User entity (password not set)
     */
    public User toEntity(RegisterRequest request) {
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .role(request.getRole())
                // password deliberately omitted — set by service after hashing
                .build();
    }

    /**
     * Converts a User entity to a UserResponseDTO.
     * <p>
     * Sensitive fields (password, updatedAt) are excluded
     * to prevent data leakage in API responses.
     * </p>
     *
     * @param user the User entity
     * @return a safe-to-expose response DTO
     */
    public UserResponseDTO toResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
