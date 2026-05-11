package com.voting.userservice.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class JwtPrincipal {
    private final UUID userId;
    private final String username;
    private final String role;
}
