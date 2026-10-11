package com.voting.userservice.dto;

import java.util.UUID;

/**
 * Role lookup result for {@code GET /api/v1/users/{id}/role}.
 */
public record UserRoleResponse(UUID userId, String role) {
}
