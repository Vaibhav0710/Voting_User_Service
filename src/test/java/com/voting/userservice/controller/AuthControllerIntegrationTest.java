package com.voting.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voting.userservice.dto.LoginRequest;
import com.voting.userservice.dto.RegisterRequest;
import com.voting.userservice.model.enums.Role;
import com.voting.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void authFlow_ShouldSucceed_WhenValidRequests() throws Exception {
        // 1. Register
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", is("testuser")))
                .andExpect(jsonPath("$.data.email", is("test@example.com")))
                .andExpect(jsonPath("$.data.role", is(Role.ROLE_VOTER.name())));

        // 2. Login
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.username", is("testuser")))
                .andReturn();

        String responseString = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseString).get("data").get("token").asText();

        // 3. Validate
        mockMvc.perform(get("/api/v1/auth/validate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", is("testuser")))
                .andExpect(jsonPath("$.data.role", is(Role.ROLE_VOTER.name())));
    }

    @Test
    void register_ShouldFail_WhenUsernameAlreadyExists() throws Exception {
        // Register first user
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Register second user with same username
        RegisterRequest duplicateRequest = new RegisterRequest();
        duplicateRequest.setUsername("testuser");
        duplicateRequest.setEmail("other@example.com");
        duplicateRequest.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("already taken")));
    }

    @Test
    void login_ShouldFail_WhenCredentialsAreInvalid() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistent", "wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_IgnoresRequestedAdminRole_AndCreatesVoter() throws Exception {
        String body = "{\"username\":\"sneaky\",\"email\":\"sneaky@example.com\","
                + "\"password\":\"password123\",\"role\":\"ROLE_ADMIN\"}";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role", is(Role.ROLE_VOTER.name())));
    }

    @Test
    void validate_ShouldReturn401_WhenTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validate_ShouldReturn401_WhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate"))
                .andExpect(status().isUnauthorized());
    }
}
