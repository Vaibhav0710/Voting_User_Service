package com.voting.userservice.controller;

import com.voting.userservice.model.User;
import com.voting.userservice.model.enums.Role;
import com.voting.userservice.repository.UserRepository;
import com.voting.userservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;

    private User voter;
    private User otherVoter;
    private User admin;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        voter = save("alice", Role.ROLE_VOTER);
        otherVoter = save("bob", Role.ROLE_VOTER);
        admin = save("root", Role.ROLE_ADMIN);
    }

    private User save(String username, Role role) {
        return userRepository.save(User.builder()
                .username(username)
                .email(username + "@example.com")
                .password("{noop}irrelevant")
                .role(role)
                .build());
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user.getUsername(), user.getId(), user.getRole().name());
    }

    @Test
    void getUser_self_200() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", voter.getId()).header("Authorization", bearer(voter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("alice"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void getUser_otherUserAsVoter_403() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", otherVoter.getId()).header("Authorization", bearer(voter)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUser_anyUserAsAdmin_200() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", otherVoter.getId()).header("Authorization", bearer(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void getUser_unknownIdAsAdmin_404() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID()).header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRole_authenticated_returnsRole() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}/role", admin.getId()).header("Authorization", bearer(voter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"));
    }

    @Test
    void getRole_anonymous_401() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}/role", admin.getId()))
                .andExpect(status().isUnauthorized());
    }
}
