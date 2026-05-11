package com.voting.userservice.controller;

import com.voting.userservice.dto.ApiResponse;
import com.voting.userservice.dto.AuthResponse;
import com.voting.userservice.dto.LoginRequest;
import com.voting.userservice.dto.RegisterRequest;
import com.voting.userservice.dto.UserResponseDTO;
import com.voting.userservice.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponseDTO registeredUser = userService.registerUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(registeredUser, "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<com.voting.userservice.dto.AuthValidationResponse>> validate(
            org.springframework.security.core.Authentication authentication) {

        // At this point, the JwtAuthenticationFilter has already validated the token
        // and populated the Authentication object with our JwtPrincipal.
        com.voting.userservice.security.JwtPrincipal principal = (com.voting.userservice.security.JwtPrincipal) authentication
                .getPrincipal();

        com.voting.userservice.dto.AuthValidationResponse validationResponse = com.voting.userservice.dto.AuthValidationResponse
                .builder()
                .userId(principal.getUserId())
                .username(principal.getUsername())
                .role(principal.getRole())
                .build();

        return ResponseEntity.ok(ApiResponse.success(validationResponse, "Token is valid"));
    }
}
