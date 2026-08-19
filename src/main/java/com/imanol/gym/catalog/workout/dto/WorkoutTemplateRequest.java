package com.imanol.gym.catalog.workout.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkoutTemplateRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        String description

) {
}