package com.imanol.gym.user.dto;

import com.imanol.gym.user.entity.UserRole;

import java.time.LocalDateTime;

public record UserResponse(

        Long id,
        String firstName,
        String lastName,
        String email,
        UserRole role,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}