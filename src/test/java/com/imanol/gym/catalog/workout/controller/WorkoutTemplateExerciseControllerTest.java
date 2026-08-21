package com.imanol.gym.catalog.workout.controller;

import com.imanol.gym.catalog.exercise.dto.ExerciseResponse;
import com.imanol.gym.catalog.exercise.entity.Exercise;
import com.imanol.gym.catalog.workout.dto.WorkoutTemplateExerciseRequest;
import com.imanol.gym.catalog.workout.dto.WorkoutTemplateExerciseResponse;
import com.imanol.gym.catalog.workout.entity.WorkoutTemplate;
import com.imanol.gym.catalog.workout.entity.WorkoutTemplateExercise;
import com.imanol.gym.catalog.workout.mapper.WorkoutTemplateExerciseMapper;
import com.imanol.gym.catalog.workout.service.WorkoutTemplateExerciseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkoutTemplateExerciseController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkoutTemplateExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkoutTemplateExerciseService workoutTemplateExerciseService;

    @MockitoBean
    private WorkoutTemplateExerciseMapper workoutTemplateExerciseMapper;

    @Test
    void shouldCreateWorkoutTemplateExercise() throws Exception {

        WorkoutTemplateExercise templateExercise =
                createWorkoutTemplateExercise();

        WorkoutTemplateExerciseResponse response =
                createResponse();

        when(workoutTemplateExerciseMapper
                .toEntity(any(WorkoutTemplateExerciseRequest.class)))
                .thenReturn(templateExercise);

        when(workoutTemplateExerciseService.create(
                1L,
                1L,
                templateExercise
        )).thenReturn(templateExercise);

        when(workoutTemplateExerciseMapper.toResponse(templateExercise))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/workout-templates/1/exercises")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "exerciseId": 1,
                                            "orderIndex": 1,
                                            "sets": 4,
                                            "repetitions": 10,
                                            "restSeconds": 90
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.exercise.id").value(1))
                .andExpect(jsonPath("$.data.exercise.name")
                        .value("Bench Press"))
                .andExpect(jsonPath("$.data.orderIndex").value(1))
                .andExpect(jsonPath("$.data.sets").value(4))
                .andExpect(jsonPath("$.data.repetitions").value(10))
                .andExpect(jsonPath("$.data.restSeconds").value(90));

        verify(workoutTemplateExerciseService).create(
                1L,
                1L,
                templateExercise
        );
    }

    @Test
    void shouldFindAllWorkoutTemplateExercises() throws Exception {

        WorkoutTemplateExercise templateExercise =
                createWorkoutTemplateExercise();

        WorkoutTemplateExerciseResponse response =
                createResponse();

        when(workoutTemplateExerciseService
                .findAllByWorkoutTemplateId(1L))
                .thenReturn(List.of(templateExercise));

        when(workoutTemplateExerciseMapper.toResponse(templateExercise))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/workout-templates/1/exercises")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].exercise.id").value(1))
                .andExpect(jsonPath("$.data[0].exercise.name")
                        .value("Bench Press"))
                .andExpect(jsonPath("$.data[0].orderIndex").value(1))
                .andExpect(jsonPath("$.data[0].sets").value(4))
                .andExpect(jsonPath("$.data[0].repetitions").value(10))
                .andExpect(jsonPath("$.data[0].restSeconds").value(90));
    }

    @Test
    void shouldUpdateWorkoutTemplateExercise() throws Exception {

        WorkoutTemplateExercise templateExercise =
                createWorkoutTemplateExercise();

        templateExercise.setOrderIndex(2);
        templateExercise.setSets(3);
        templateExercise.setRepetitions(12);
        templateExercise.setRestSeconds(60);

        WorkoutTemplateExerciseResponse response =
                new WorkoutTemplateExerciseResponse(
                        1L,
                        createExerciseResponse(),
                        2,
                        3,
                        12,
                        60
                );

        when(workoutTemplateExerciseMapper
                .toEntity(any(WorkoutTemplateExerciseRequest.class)))
                .thenReturn(templateExercise);

        when(workoutTemplateExerciseService.update(
                1L,
                1L,
                templateExercise
        )).thenReturn(templateExercise);

        when(workoutTemplateExerciseMapper.toResponse(templateExercise))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/workout-template-exercises/1")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "exerciseId": 1,
                                            "orderIndex": 2,
                                            "sets": 3,
                                            "repetitions": 12,
                                            "restSeconds": 60
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.orderIndex").value(2))
                .andExpect(jsonPath("$.data.sets").value(3))
                .andExpect(jsonPath("$.data.repetitions").value(12))
                .andExpect(jsonPath("$.data.restSeconds").value(60));

        verify(workoutTemplateExerciseService).update(
                1L,
                1L,
                templateExercise
        );
    }

    @Test
    void shouldDeleteWorkoutTemplateExercise() throws Exception {

        doNothing()
                .when(workoutTemplateExerciseService)
                .deleteById(1L);

        mockMvc.perform(
                        delete("/api/workout-template-exercises/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Workout template exercise deleted successfully"));

        verify(workoutTemplateExerciseService)
                .deleteById(1L);
    }


    @ParameterizedTest
    @MethodSource("invalidWorkoutTemplateExerciseRequests")
    void shouldReturnBadRequestForInvalidRequest(
            String requestBody
    ) throws Exception {

        mockMvc.perform(
                        post("/api/workout-templates/1/exercises")
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(workoutTemplateExerciseService);
    }

    private static Stream<String> invalidWorkoutTemplateExerciseRequests() {

        return Stream.of(
                """
                {
                    "orderIndex": 1,
                    "sets": 4,
                    "repetitions": 10,
                    "restSeconds": 90
                }
                """,
                """
                {
                    "exerciseId": 1,
                    "orderIndex": 0,
                    "sets": 4,
                    "repetitions": 10,
                    "restSeconds": 90
                }
                """,
                """
                {
                    "exerciseId": 1,
                    "orderIndex": 1,
                    "sets": 0,
                    "repetitions": 10,
                    "restSeconds": 90
                }
                """
        );
    }


    private WorkoutTemplateExercise createWorkoutTemplateExercise() {

        WorkoutTemplate workoutTemplate =
                new WorkoutTemplate();

        workoutTemplate.setId(1L);
        workoutTemplate.setName("Hypertrophy Template");
        workoutTemplate.setActive(true);

        Exercise exercise = new Exercise();
        exercise.setId(1L);
        exercise.setName("Bench Press");
        exercise.setActive(true);

        WorkoutTemplateExercise templateExercise =
                new WorkoutTemplateExercise();

        templateExercise.setId(1L);
        templateExercise.setWorkoutTemplate(workoutTemplate);
        templateExercise.setExercise(exercise);
        templateExercise.setOrderIndex(1);
        templateExercise.setSets(4);
        templateExercise.setRepetitions(10);
        templateExercise.setRestSeconds(90);

        return templateExercise;
    }

    private WorkoutTemplateExerciseResponse createResponse() {

        return new WorkoutTemplateExerciseResponse(
                1L,
                createExerciseResponse(),
                1,
                4,
                10,
                90
        );
    }

    private ExerciseResponse createExerciseResponse() {

        return new ExerciseResponse(
                1L,
                "Bench Press",
                "Bench Press",
                1L,
                "Press",
                true
        );
    }
}