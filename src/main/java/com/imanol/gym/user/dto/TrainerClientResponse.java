package com.imanol.gym.user.dto;

import java.time.LocalDateTime;

public record TrainerClientResponse(

        Long id,

        Long trainerId,

        Long clientId,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}