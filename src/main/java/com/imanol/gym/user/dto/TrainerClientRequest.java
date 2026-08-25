package com.imanol.gym.user.dto;

import jakarta.validation.constraints.NotNull;

public record TrainerClientRequest(

        @NotNull
        Long trainerId,

        @NotNull
        Long clientId
) {
}