package com.imanol.gym.catalog.nutrition.controller;

import com.imanol.gym.catalog.nutrition.dto.*;
import com.imanol.gym.catalog.nutrition.entity.NutritionPlan;
import com.imanol.gym.catalog.nutrition.entity.NutritionPlanStatus;
import com.imanol.gym.catalog.nutrition.mapper.NutritionPlanMapper;
import com.imanol.gym.catalog.nutrition.service.NutritionPlanService;
import com.imanol.gym.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nutrition-plans")
@RequiredArgsConstructor
public class NutritionPlanController {
    private final NutritionPlanService nutritionPlanService;
    private final NutritionPlanMapper nutritionPlanMapper;

    @PostMapping("/clients/{clientId}")
    public ResponseEntity<ApiResponse<NutritionPlanResponse>> create(
            @PathVariable Long clientId,
            @Valid @RequestBody NutritionPlanRequest request) {
        NutritionPlan plan = nutritionPlanService
                .createForAuthenticatedTrainer(clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                HttpStatus.CREATED.value(), "Nutrition plan created successfully",
                nutritionPlanMapper.toResponse(plan)));
    }

    @GetMapping("/clients/{clientId}")
    public ResponseEntity<ApiResponse<List<NutritionPlanResponse>>> findByClient(
            @PathVariable Long clientId,
            @RequestParam(required = false) NutritionPlanStatus status) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(),
                "Client nutrition plans retrieved successfully",
                nutritionPlanService.findForAuthenticatedTrainer(clientId, status)
                        .stream().map(nutritionPlanMapper::toResponse).toList()));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<NutritionPlanResponse>>> findMine() {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(),
                "Nutrition plans retrieved successfully",
                nutritionPlanService.findForAuthenticatedClient().stream()
                        .map(nutritionPlanMapper::toResponse).toList()));
    }

    @GetMapping("/me/active")
    public ResponseEntity<ApiResponse<List<NutritionPlanResponse>>> findActiveMine() {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(),
                "Active nutrition plans retrieved successfully",
                nutritionPlanService.findActiveForAuthenticatedClient().stream()
                        .map(nutritionPlanMapper::toResponse).toList()));
    }

    @GetMapping("/me/{id}")
    public ResponseEntity<ApiResponse<NutritionPlanResponse>> findMineById(
            @PathVariable Long id) {
        return findById(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NutritionPlanResponse>> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(),
                "Nutrition plan retrieved successfully",
                nutritionPlanMapper.toResponse(
                        nutritionPlanService.findByIdForAuthenticatedUser(id))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NutritionPlanResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody NutritionPlanUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(),
                "Nutrition plan updated successfully",
                nutritionPlanMapper.toResponse(
                        nutritionPlanService.updateForAuthenticatedTrainer(id, request))));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        nutritionPlanService.deactivateForAuthenticatedTrainer(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(),
                "Nutrition plan deactivated successfully", null));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Void>> complete(@PathVariable Long id) {
        nutritionPlanService.completeForAuthenticatedTrainer(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(),
                "Nutrition plan completed successfully", null));
    }
}
