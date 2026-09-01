package com.imanol.gym.user.controller;

import com.imanol.gym.common.dto.ApiResponse;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import com.imanol.gym.user.dto.UserRequest;
import com.imanol.gym.user.dto.UserResponse;
import com.imanol.gym.user.entity.User;
import com.imanol.gym.user.mapper.UserMapper;
import com.imanol.gym.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody UserRequest request
    ) {

        User user = userMapper.toEntity(request);

        User createdUser = userService.createUser(user);

        UserResponse response =
                userMapper.toResponse(createdUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                HttpStatus.CREATED.value(),
                                "User created successfully",
                                response
                        )
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> findById(
            @PathVariable Long id
    ) {

        User user = userService.findById(id);

        return userResponse(user);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> findAuthenticatedUser() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userService.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with email: " + email
                        )
                );

        return userResponse(user);
    }

    private ResponseEntity<ApiResponse<UserResponse>> userResponse(User user) {
        UserResponse response = userMapper.toResponse(user);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "User retrieved successfully",
                        response
                )
        );
    }
}