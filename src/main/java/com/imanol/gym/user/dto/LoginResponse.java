package com.imanol.gym.user.dto;

public record LoginResponse(String token, String tokenType, long expiresIn) {
}
