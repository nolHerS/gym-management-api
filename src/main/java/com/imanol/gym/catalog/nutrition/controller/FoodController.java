package com.imanol.gym.catalog.nutrition.controller;

import com.imanol.gym.catalog.nutrition.dto.FoodRequest;
import com.imanol.gym.catalog.nutrition.dto.FoodResponse;
import com.imanol.gym.catalog.nutrition.entity.Food;
import com.imanol.gym.catalog.nutrition.mapper.FoodMapper;
import com.imanol.gym.catalog.nutrition.service.FoodService;
import com.imanol.gym.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodController {
    private final FoodService foodService;
    private final FoodMapper foodMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FoodResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(),
                "Foods retrieved successfully", foodService.findAll().stream()
                        .map(foodMapper::toResponse).toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(),
                "Food retrieved successfully",
                foodMapper.toResponse(foodService.findById(id))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FoodResponse>> create(
            @Valid @RequestBody FoodRequest request) {
        Food food = foodService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                HttpStatus.CREATED.value(), "Food created successfully",
                foodMapper.toResponse(food)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodResponse>> update(
            @PathVariable Long id, @Valid @RequestBody FoodRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(),
                "Food updated successfully",
                foodMapper.toResponse(foodService.update(id, request))));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        foodService.activate(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(),
                "Food activated successfully", null));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        foodService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(),
                "Food deactivated successfully", null));
    }
}
