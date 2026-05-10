package com.voting.userservice.service;

import com.voting.userservice.dto.RegisterRequest;
import com.voting.userservice.dto.UserResponseDTO;
import com.voting.userservice.dto.LoginRequest;
import com.voting.userservice.dto.AuthResponse;

/**
 * Contract for user-related business operations.
 * <p>
 * Coding to an interface (not implementation) enables:
 * - Isolated unit testing via mocks
 * - Swapping implementations without modifying dependents
 * - Clean dependency injection boundaries
 * </p>
 */
public interface IUserService {

    /**
     * Registers a new user in the system.
     *
     * @param request validated registration payload
     * @return the created user's public-facing details
     * @throws com.voting.userservice.exception.DuplicateResourceException
     *         if the username or email is already taken
     */
    UserResponseDTO registerUser(RegisterRequest request);

    /**
     * Authenticates a user and returns a signed JWT access token.
     *
     * @param request login payload with username/email and password
     * @return token payload details for the client
     */
    AuthResponse login(LoginRequest request);
}
