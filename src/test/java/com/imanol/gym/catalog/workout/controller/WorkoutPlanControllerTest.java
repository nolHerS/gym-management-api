package com.imanol.gym.catalog.workout.controller;

import com.imanol.gym.catalog.workout.dto.WorkoutPlanResponse;
import com.imanol.gym.catalog.workout.dto.WorkoutPlanRequest;
import com.imanol.gym.catalog.workout.entity.WorkoutPlan;
import com.imanol.gym.catalog.workout.entity.WorkoutPlanStatus;
import com.imanol.gym.catalog.workout.mapper.WorkoutPlanDayMapper;
import com.imanol.gym.catalog.workout.mapper.WorkoutPlanExerciseMapper;
import com.imanol.gym.catalog.workout.mapper.WorkoutPlanMapper;
import com.imanol.gym.catalog.workout.service.WorkoutPlanService;
import com.imanol.gym.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkoutPlanController.class)
@ContextConfiguration(classes = {
        WorkoutPlanController.class,
        GlobalExceptionHandler.class
})
@AutoConfigureMockMvc(addFilters = false)
class WorkoutPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkoutPlanService workoutPlanService;

    @MockitoBean
    private WorkoutPlanMapper workoutPlanMapper;

    @MockitoBean
    private WorkoutPlanDayMapper workoutPlanDayMapper;

    @MockitoBean
    private WorkoutPlanExerciseMapper workoutPlanExerciseMapper;

    @Test
    void shouldCreateWorkoutPlanWithApiResponse() throws Exception {
        WorkoutPlan plan = new WorkoutPlan();
        WorkoutPlanResponse response = response();
        when(workoutPlanService.createForAuthenticatedTrainer(
                eq(2L),
                any(WorkoutPlanRequest.class)
        )).thenReturn(plan);
        when(workoutPlanMapper.toResponse(plan)).thenReturn(response);

        mockMvc.perform(post("/api/workout-plans/clients/2")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "startDate": "2026-09-07",
                                  "days": [{
                                    "dayOfWeek": 1,
                                    "exercises": [{
                                      "exerciseId": 5,
                                      "orderIndex": 1,
                                      "sets": 4,
                                      "repetitions": 8,
                                      "restSeconds": 90
                                    }]
                                  }]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void shouldRejectInvalidPlanRequest() throws Exception {
        mockMvc.perform(post("/api/workout-plans/clients/2")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "startDate": "2026-09-07",
                                  "days": [{
                                    "dayOfWeek": 8,
                                    "exercises": []
                                  }]
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(workoutPlanService);
    }

    @Test
    void shouldReturnClientPlans() throws Exception {
        WorkoutPlan plan = new WorkoutPlan();
        WorkoutPlanResponse response = response();
        when(workoutPlanService.findForAuthenticatedTrainer(2L, null))
                .thenReturn(List.of(plan));
        when(workoutPlanMapper.toResponse(plan)).thenReturn(response);

        mockMvc.perform(get("/api/workout-plans/clients/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].clientId").value(2));
    }

    private WorkoutPlanResponse response() {
        return new WorkoutPlanResponse(
                10L,
                2L,
                1L,
                null,
                LocalDate.of(2026, 9, 7),
                null,
                WorkoutPlanStatus.ACTIVE,
                List.of(),
                null,
                null
        );
    }
}
