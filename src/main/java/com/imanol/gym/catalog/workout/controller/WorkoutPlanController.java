package com.imanol.gym.catalog.workout.controller;

import com.imanol.gym.catalog.workout.dto.*;
import com.imanol.gym.catalog.workout.entity.WorkoutPlan;
import com.imanol.gym.catalog.workout.entity.WorkoutPlanDay;
import com.imanol.gym.catalog.workout.entity.WorkoutPlanExercise;
import com.imanol.gym.catalog.workout.entity.WorkoutPlanStatus;
import com.imanol.gym.catalog.workout.mapper.WorkoutPlanDayMapper;
import com.imanol.gym.catalog.workout.mapper.WorkoutPlanExerciseMapper;
import com.imanol.gym.catalog.workout.mapper.WorkoutPlanMapper;
import com.imanol.gym.catalog.workout.service.WorkoutPlanService;
import com.imanol.gym.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/workout-plans")
@RequiredArgsConstructor
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;
    private final WorkoutPlanMapper workoutPlanMapper;
    private final WorkoutPlanDayMapper workoutPlanDayMapper;
    private final WorkoutPlanExerciseMapper workoutPlanExerciseMapper;

    @PostMapping("/clients/{clientId}")
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> create(
            @PathVariable Long clientId,
            @Valid @RequestBody WorkoutPlanRequest request
    ) {
        WorkoutPlan plan = workoutPlanService
                .createForAuthenticatedTrainer(clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "Workout plan created successfully",
                        workoutPlanMapper.toResponse(plan)
                )
        );
    }

    @GetMapping("/clients/{clientId}")
    public ResponseEntity<ApiResponse<List<WorkoutPlanResponse>>> findByClient(
            @PathVariable Long clientId,
            @RequestParam(required = false) WorkoutPlanStatus status
    ) {
        List<WorkoutPlanResponse> response = workoutPlanService
                .findForAuthenticatedTrainer(clientId, status)
                .stream()
                .map(workoutPlanMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Client workout plans retrieved successfully",
                response
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<WorkoutPlanResponse>>> findMine() {
        List<WorkoutPlanResponse> response = workoutPlanService
                .findForAuthenticatedClient()
                .stream()
                .map(workoutPlanMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Workout plans retrieved successfully",
                response
        ));
    }

    @GetMapping("/me/week")
    public ResponseEntity<ApiResponse<List<WorkoutPlanResponse>>> findMyWeek(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate weekStart
    ) {
        List<WorkoutPlanResponse> response = workoutPlanService
                .findWeekForAuthenticatedClient(weekStart)
                .stream()
                .map(workoutPlanMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Weekly workout plans retrieved successfully",
                response
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Workout plan retrieved successfully",
                workoutPlanMapper.toResponse(
                        workoutPlanService.findByIdForAuthenticatedUser(id)
                )
        ));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Workout plan updated successfully",
                workoutPlanMapper.toResponse(
                        workoutPlanService.updateForAuthenticatedUser(id, request)
                )
        ));
    }

    @PostMapping("/{planId}/days")
    public ResponseEntity<ApiResponse<WorkoutPlanDayResponse>> addDay(
            @PathVariable Long planId,
            @Valid @RequestBody WorkoutPlanDayRequest request
    ) {
        WorkoutPlanDay day = workoutPlanService
                .addDayForAuthenticatedTrainer(planId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "Workout plan day created successfully",
                        workoutPlanDayMapper.toResponse(day)
                )
        );
    }

    @DeleteMapping("/{planId}/days/{dayOfWeek}")
    public ResponseEntity<ApiResponse<Void>> deleteDay(
            @PathVariable Long planId,
            @PathVariable Integer dayOfWeek
    ) {
        workoutPlanService.deleteDayForAuthenticatedTrainer(
                planId,
                dayOfWeek
        );
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Workout plan day deleted successfully",
                null
        ));
    }

    @PostMapping("/{planId}/days/{dayOfWeek}/exercises")
    public ResponseEntity<ApiResponse<WorkoutPlanExerciseResponse>>
    addExercise(
            @PathVariable Long planId,
            @PathVariable Integer dayOfWeek,
            @Valid @RequestBody WorkoutPlanExerciseRequest request
    ) {
        WorkoutPlanExercise exercise = workoutPlanService
                .addExerciseForAuthenticatedTrainer(
                        planId,
                        dayOfWeek,
                        request
                );
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "Workout plan exercise created successfully",
                        workoutPlanExerciseMapper.toResponse(exercise)
                )
        );
    }

    @PutMapping("/exercises/{id}")
    public ResponseEntity<ApiResponse<WorkoutPlanExerciseResponse>>
    updateExercise(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanExerciseRequest request
    ) {
        WorkoutPlanExercise exercise = workoutPlanService
                .updateExerciseForAuthenticatedUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Workout plan exercise updated successfully",
                workoutPlanExerciseMapper.toResponse(exercise)
        ));
    }

    @DeleteMapping("/exercises/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExercise(
            @PathVariable Long id
    ) {
        workoutPlanService.deleteExerciseForAuthenticatedUser(id);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Workout plan exercise deleted successfully",
                null
        ));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable Long id
    ) {
        workoutPlanService.deactivateForAuthenticatedUser(id);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Workout plan deactivated successfully",
                null
        ));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Void>> complete(
            @PathVariable Long id
    ) {
        workoutPlanService.completeForAuthenticatedUser(id);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Workout plan completed successfully",
                null
        ));
    }
}
