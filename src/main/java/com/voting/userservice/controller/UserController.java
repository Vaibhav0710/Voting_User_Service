package com.voting.userservice.controller;

import com.voting.userservice.dto.ApiResponse;
import com.voting.userservice.dto.UserResponseDTO;
import com.voting.userservice.dto.UserRoleResponse;
import com.voting.userservice.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * User profile lookups. A voter may read only their own profile; ADMIN may read anyone's.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUser(id), "User fetched successfully"));
    }

    @GetMapping("/{id}/role")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserRoleResponse>> getRole(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getRole(id), "Role fetched successfully"));
    }
}
