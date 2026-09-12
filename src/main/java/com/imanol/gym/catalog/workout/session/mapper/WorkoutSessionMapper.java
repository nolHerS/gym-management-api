package com.imanol.gym.catalog.workout.session.mapper;
import com.imanol.gym.catalog.workout.session.dto.*;
import com.imanol.gym.catalog.workout.session.entity.*;
import org.mapstruct.Mapper; import org.mapstruct.Mapping;
@Mapper(componentModel = "spring")
public interface WorkoutSessionMapper {
    @Mapping(target="workoutPlanId", source="workoutPlan.id")
    @Mapping(target="clientId", source="client.id")
    WorkoutSessionResponse toResponse(WorkoutSession e);
    @Mapping(target="exerciseId", source="exercise.id")
    @Mapping(target="exerciseName", source="exerciseNameSnapshot")
    WorkoutSessionExerciseResponse toResponse(WorkoutSessionExercise e);
    WorkoutSessionSetResponse toResponse(WorkoutSessionSet e);
}
