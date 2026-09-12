package com.imanol.gym.catalog.workout.session.controller;

import com.imanol.gym.catalog.workout.session.dto.*;
import com.imanol.gym.catalog.workout.session.entity.*;
import com.imanol.gym.catalog.workout.session.mapper.WorkoutSessionMapper;
import com.imanol.gym.catalog.workout.session.service.WorkoutSessionService;
import com.imanol.gym.common.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.*;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkoutSessionController.class)
@ContextConfiguration(classes = {WorkoutSessionController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class WorkoutSessionControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean WorkoutSessionService service;
    @MockitoBean WorkoutSessionMapper mapper;

    @Test void createReturnsCreatedApiResponse() throws Exception {
        WorkoutSession s = new WorkoutSession(); WorkoutSessionResponse response = response();
        when(service.create(any())).thenReturn(s); when(mapper.toResponse(s)).thenReturn(response);
        mvc.perform(post("/api/workout-sessions").contentType(APPLICATION_JSON)
                        .content("{\"workoutPlanId\":4}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Workout session created successfully"))
                .andExpect(jsonPath("$.data.id").value(8));
    }

    @Test void validatesCreateAndSetRequests() throws Exception {
        mvc.perform(post("/api/workout-sessions").contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Validation failed"));
        mvc.perform(post("/api/workout-sessions/8/sets").contentType(APPLICATION_JSON)
                        .content("{\"sessionExerciseId\":1,\"setNumber\":0}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test void listsAndGetsSessions() throws Exception {
        WorkoutSession s = new WorkoutSession(); when(service.search(WorkoutSessionStatus.IN_PROGRESS, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 12))).thenReturn(List.of(s));
        when(mapper.toResponse(s)).thenReturn(response()); when(service.find(8L)).thenReturn(s);
        mvc.perform(get("/api/workout-sessions?from=2026-09-01&to=2026-09-12&status=IN_PROGRESS"))
                .andExpect(status().isOk());
        verify(service).search(WorkoutSessionStatus.IN_PROGRESS, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 12));
        mvc.perform(get("/api/workout-sessions/8")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test void updatesSessionSetExerciseAndTransitions() throws Exception {
        WorkoutSession s = new WorkoutSession(); WorkoutSessionSet set = new WorkoutSessionSet();
        WorkoutSessionExercise e = new WorkoutSessionExercise();
        when(service.update(eq(8L), any())).thenReturn(s); when(service.saveSet(eq(8L), any())).thenReturn(set);
        when(service.updateExercise(eq(8L), eq(3L), any())).thenReturn(e);
        when(service.finish(8L)).thenReturn(s); when(service.cancel(8L)).thenReturn(s);
        when(mapper.toResponse(s)).thenReturn(response()); when(mapper.toResponse(set)).thenReturn(null);
        when(mapper.toResponse(e)).thenReturn(null);
        mvc.perform(patch("/api/workout-sessions/8").contentType(APPLICATION_JSON).content("{\"notes\":\"x\",\"durationSeconds\":20}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/workout-sessions/8/sets").contentType(APPLICATION_JSON)
                        .content("{\"sessionExerciseId\":3,\"setNumber\":1,\"actualRepetitions\":8,\"completed\":true}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/workout-sessions/8/exercises/3").contentType(APPLICATION_JSON).content("{\"completed\":true}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/workout-sessions/8/finish")).andExpect(status().isOk());
        mvc.perform(patch("/api/workout-sessions/8/cancel")).andExpect(status().isOk());
    }

    @Test void mapsOwnershipNotFoundConflictAndIllegalArgument() throws Exception {
        when(service.find(8L)).thenThrow(new org.springframework.security.access.AccessDeniedException("no"));
        mvc.perform(get("/api/workout-sessions/8")).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        when(service.find(9L)).thenThrow(new ResourceNotFoundException("missing"));
        mvc.perform(get("/api/workout-sessions/9")).andExpect(status().isNotFound());
        when(service.find(10L)).thenThrow(new ResourceConflictException("terminal"));
        mvc.perform(get("/api/workout-sessions/10")).andExpect(status().isConflict());
        when(service.find(11L)).thenThrow(new IllegalArgumentException("invalid"));
        mvc.perform(get("/api/workout-sessions/11")).andExpect(status().isBadRequest());
    }

    private WorkoutSessionResponse response() {
        return new WorkoutSessionResponse(8L, 4L, 2L, WorkoutSessionStatus.IN_PROGRESS,
                LocalDateTime.of(2026, 9, 12, 10, 0), null, null, null, null, 0L, List.of());
    }
}
