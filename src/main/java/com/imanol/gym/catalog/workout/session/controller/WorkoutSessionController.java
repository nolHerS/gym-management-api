package com.imanol.gym.catalog.workout.session.controller;
import com.imanol.gym.catalog.workout.session.dto.*;
import com.imanol.gym.catalog.workout.session.entity.*;
import com.imanol.gym.catalog.workout.session.mapper.WorkoutSessionMapper;
import com.imanol.gym.catalog.workout.session.service.WorkoutSessionService;
import com.imanol.gym.common.dto.ApiResponse;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat; import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate; import java.util.List;

@RestController @RequestMapping("/api/workout-sessions") @RequiredArgsConstructor
public class WorkoutSessionController {
    private final WorkoutSessionService service; private final WorkoutSessionMapper mapper;
    @PostMapping public ResponseEntity<ApiResponse<WorkoutSessionResponse>> create(@Valid @RequestBody WorkoutSessionCreateRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(201,"Workout session created successfully",mapper.toResponse(service.create(r))));
    }
    @GetMapping public ResponseEntity<ApiResponse<List<WorkoutSessionResponse>>> list(
            @RequestParam(required=false) WorkoutSessionStatus status,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(200,"Workout sessions retrieved successfully",service.search(status,from,to).stream().map(mapper::toResponse).toList()));
    }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<WorkoutSessionResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(200,"Workout session retrieved successfully",mapper.toResponse(service.find(id))));
    }
    @PatchMapping("/{id}") public ResponseEntity<ApiResponse<WorkoutSessionResponse>> update(@PathVariable Long id,@Valid @RequestBody WorkoutSessionUpdateRequest r) {
        return ResponseEntity.ok(ApiResponse.success(200,"Workout session updated successfully",mapper.toResponse(service.update(id,r))));
    }
    @PostMapping("/{id}/sets") public ResponseEntity<ApiResponse<WorkoutSessionSetResponse>> set(@PathVariable Long id,@Valid @RequestBody WorkoutSessionSetRequest r) {
        return ResponseEntity.ok(ApiResponse.success(200,"Workout session set saved successfully",mapper.toResponse(service.saveSet(id,r))));
    }
    @PatchMapping("/{sessionId}/exercises/{exerciseId}") public ResponseEntity<ApiResponse<WorkoutSessionExerciseResponse>> exercise(@PathVariable Long sessionId,@PathVariable Long exerciseId,@RequestBody WorkoutSessionExerciseUpdateRequest r) {
        return ResponseEntity.ok(ApiResponse.success(200,"Workout session exercise updated successfully",mapper.toResponse(service.updateExercise(sessionId,exerciseId,r))));
    }
    @PatchMapping("/{id}/finish") public ResponseEntity<ApiResponse<WorkoutSessionResponse>> finish(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(200,"Workout session finished successfully",mapper.toResponse(service.finish(id))));
    }
    @PatchMapping("/{id}/cancel") public ResponseEntity<ApiResponse<WorkoutSessionResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(200,"Workout session cancelled successfully",mapper.toResponse(service.cancel(id))));
    }
}
