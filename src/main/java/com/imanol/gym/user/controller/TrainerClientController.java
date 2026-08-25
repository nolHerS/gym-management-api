package com.imanol.gym.user.controller;

import com.imanol.gym.common.dto.ApiResponse;
import com.imanol.gym.user.dto.TrainerClientRequest;
import com.imanol.gym.user.dto.TrainerClientResponse;
import com.imanol.gym.user.entity.TrainerClient;
import com.imanol.gym.user.mapper.TrainerClientMapper;
import com.imanol.gym.user.service.TrainerClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainer-clients")
@RequiredArgsConstructor
public class TrainerClientController {

    private final TrainerClientService trainerClientService;
    private final TrainerClientMapper trainerClientMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<TrainerClientResponse>> assignClient(
            @Valid @RequestBody TrainerClientRequest request
    ) {

        TrainerClient trainerClient =
                trainerClientService.assignClient(
                        request.trainerId(),
                        request.clientId()
                );

        TrainerClientResponse response =
                trainerClientMapper.toResponse(trainerClient);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                HttpStatus.CREATED.value(),
                                "Client assigned to trainer successfully",
                                response
                        )
                );
    }

    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<ApiResponse<List<TrainerClientResponse>>>
    findAllByTrainerId(
            @PathVariable Long trainerId
    ) {

        List<TrainerClientResponse> response =
                trainerClientService
                        .findAllByTrainerId(trainerId)
                        .stream()
                        .map(trainerClientMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Trainer clients retrieved successfully",
                        response
                )
        );
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<ApiResponse<List<TrainerClientResponse>>>
    findAllByClientId(
            @PathVariable Long clientId
    ) {

        List<TrainerClientResponse> response =
                trainerClientService
                        .findAllByClientId(clientId)
                        .stream()
                        .map(trainerClientMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Client trainers retrieved successfully",
                        response
                )
        );
    }
}