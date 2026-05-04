package com.voting.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Temporary Security Configuration for Day 9.
 * <p>
 * Provides the BCryptPasswordEncoder bean needed by UserServiceImpl
 * and permits all requests until the full JWT-based security setup (Day 11).
 * </p>
 *
 * TODO: Day 11 — Replace permitAll() with proper endpoint authorization,
 *       add JwtAuthenticationFilter, and configure AuthenticationManager.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * BCrypt password encoder with default strength (10 rounds).
     * <p>
     * Why BCrypt over SHA-256?
     * - BCrypt is intentionally slow (adaptive cost factor)
     * - Each hash includes a unique salt automatically
     * - Industry standard for password storage (OWASP recommendation)
     * </p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Temporary security filter chain — permits all requests.
     * This will be hardened in Day 11 with JWT filters and role-based access.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth ->
                auth.anyRequest().permitAll());

        return http.build();
    }
}
