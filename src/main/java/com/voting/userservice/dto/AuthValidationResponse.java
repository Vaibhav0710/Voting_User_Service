package com.voting.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO returned by the token validation endpoint.
 * Contains core user identity and authorization context extracted from the JWT.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthValidationResponse {
    private UUID userId;
    private String username;
    private String role;
}
