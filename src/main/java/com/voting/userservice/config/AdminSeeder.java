package com.voting.userservice.config;

import com.voting.userservice.model.User;
import com.voting.userservice.model.enums.Role;
import com.voting.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Creates the bootstrap administrator from {@code ADMIN_USERNAME}, {@code ADMIN_EMAIL} and
 * {@code ADMIN_PASSWORD} at startup, once. This is the only way to obtain ROLE_ADMIN, since public
 * registration always creates voters. Skipped (with a warning) when the variables are not set.
 */
@Slf4j
@Component
public class AdminSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String email;
    private final String password;

    public AdminSeeder(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${admin.username:}") String username,
                       @Value("${admin.email:}") String email,
                       @Value("${admin.password:}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            log.warn("ADMIN_USERNAME/ADMIN_EMAIL/ADMIN_PASSWORD not set; no administrator account will be created");
            return;
        }
        if (userRepository.existsByUsername(username)) {
            return;
        }
        userRepository.save(User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.ROLE_ADMIN)
                .build());
        log.info("Bootstrap administrator '{}' created", username);
    }
}
