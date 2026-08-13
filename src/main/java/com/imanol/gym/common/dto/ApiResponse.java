package com.imanol.gym.common.dto;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        T data
) {

    public static <T> ApiResponse<T> success(
            int status,
            String message,
            T data) {

        return new ApiResponse<>(
                LocalDateTime.now(),
                status,
                null,
                message,
                data
        );
    }

    public static <T> ApiResponse<T> error(
            int status,
            String error,
            String message) {

        return new ApiResponse<>(
                LocalDateTime.now(),
                status,
                error,
                message,
                null
        );
    }
}