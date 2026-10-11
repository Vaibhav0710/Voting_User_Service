package com.voting.userservice.config;

import com.voting.userservice.model.User;
import com.voting.userservice.model.enums.Role;
import com.voting.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSeederTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private AdminSeeder seeder(String username, String email, String password) {
        return new AdminSeeder(userRepository, passwordEncoder, username, email, password);
    }

    @Test
    void createsAdminWithHashedPassword_whenMissing() {
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode("S3cure-pass!")).thenReturn("hashed");

        seeder("admin", "admin@example.com", "S3cure-pass!").run();

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getRole()).isEqualTo(Role.ROLE_ADMIN);
        assertThat(saved.getValue().getPassword()).isEqualTo("hashed");
    }

    @Test
    void doesNothing_whenAdminAlreadyExists() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        seeder("admin", "admin@example.com", "S3cure-pass!").run();

        verify(userRepository, never()).save(any());
    }

    @Test
    void skips_whenCredentialsNotConfigured() {
        seeder("", "", "").run();

        verify(userRepository, never()).save(any());
    }
}
