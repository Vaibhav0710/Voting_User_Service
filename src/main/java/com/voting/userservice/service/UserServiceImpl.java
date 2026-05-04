package com.voting.userservice.service;

import com.voting.userservice.dto.RegisterRequest;
import com.voting.userservice.dto.UserResponseDTO;
import com.voting.userservice.exception.DuplicateResourceException;
import com.voting.userservice.mapper.UserMapper;
import com.voting.userservice.model.User;
import com.voting.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core implementation of user business operations.
 * <p>
 * Uses constructor injection via Lombok's {@code @RequiredArgsConstructor}
 * for immutable, test-friendly dependencies.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user with the following guarantees:
     * <ol>
     *   <li>Username uniqueness is enforced</li>
     *   <li>Email uniqueness is enforced</li>
     *   <li>Password is BCrypt-hashed before persistence</li>
     *   <li>The raw password is never stored or returned</li>
     * </ol>
     *
     * @param request validated registration payload
     * @return public-facing details of the newly created user
     * @throws DuplicateResourceException if username or email is already taken
     */
    @Override
    @Transactional
    public UserResponseDTO registerUser(RegisterRequest request) {

        // --- Step 1: Proactive uniqueness validation ---
        // We check BOTH fields independently so the user gets a specific error
        // rather than fixing one only to discover the other also conflicts.
        validateUniqueness(request);

        // --- Step 2: Map DTO → Entity (password excluded by design) ---
        User user = userMapper.toEntity(request);

        // --- Step 3: Hash password and set on entity ---
        // BCrypt generates a unique salt per hash, so identical passwords
        // produce different hashes — defending against rainbow table attacks.
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // --- Step 4: Persist ---
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: username={}, id={}", savedUser.getUsername(), savedUser.getId());

        // --- Step 5: Map Entity → Response DTO (password excluded) ---
        return userMapper.toResponseDTO(savedUser);
    }

    /**
     * Validates that both username and email are available.
     * <p>
     * Throws on the FIRST conflict found. In a real-world scenario,
     * you might collect all violations and return them together —
     * but for registration, failing fast on the first conflict
     * is the standard UX pattern.
     * </p>
     */
    private void validateUniqueness(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(
                    "Username '" + request.getUsername() + "' is already taken"
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email '" + request.getEmail() + "' is already registered"
            );
        }
    }
}
