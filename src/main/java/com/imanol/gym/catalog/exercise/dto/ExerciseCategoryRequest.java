package com.imanol.gym.catalog.exercise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExerciseCategoryRequest(

        @NotBlank
        @Size(max = 100)
        String name

) {
}