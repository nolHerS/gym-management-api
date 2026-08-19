package com.imanol.gym.catalog.exercise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExerciseRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        String description,

        @NotNull
        Long categoryId

) {
}