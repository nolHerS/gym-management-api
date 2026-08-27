package com.imanol.gym.user.controller;

import com.imanol.gym.user.dto.LoginRequest;
import com.imanol.gym.user.dto.LoginResponse;
import com.imanol.gym.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${app.jwt.expiration-ms:3600000}")
    private long expirationMillis;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        return new LoginResponse(
                jwtService.generateToken((org.springframework.security.core.userdetails.UserDetails)
                        authentication.getPrincipal()),
                "Bearer",
                expirationMillis
        );
    }
}
