package com.voting.userservice.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * Custom principal object used in the SecurityContext.
 * Encapsulates user identity details extracted from a verified JWT token.
 */
@Getter
@AllArgsConstructor
public class JwtPrincipal {
    private final UUID userId;
    private final String username;
    private final String role;
}
