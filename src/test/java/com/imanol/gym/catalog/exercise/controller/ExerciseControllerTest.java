package com.imanol.gym.catalog.exercise.controller;

import com.imanol.gym.catalog.exercise.dto.ExerciseResponse;
import com.imanol.gym.catalog.exercise.dto.ExerciseRequest;
import com.imanol.gym.catalog.exercise.entity.Exercise;
import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;
import com.imanol.gym.catalog.exercise.mapper.ExerciseMapper;
import com.imanol.gym.catalog.exercise.service.ExerciseService;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExerciseController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExerciseService exerciseService;

    @MockitoBean
    private ExerciseMapper exerciseMapper;

    @Test
    void shouldCreateExercise() throws Exception {

        Exercise exercise = createExercise();

        ExerciseResponse response = createResponse();

        when(exerciseService.create(any(ExerciseRequest.class)))
                .thenReturn(exercise);

        when(exerciseMapper.toResponse(exercise))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/exercises")
                                .contentType(APPLICATION_JSON)
                                .content("""
                            {
                                "name": "Bench Press",
                                "description": "Barbell bench press",
                                "categoryId": 1
                            }
                            """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Bench Press"))
                .andExpect(jsonPath("$.data.description")
                        .value("Barbell bench press"))
                .andExpect(jsonPath("$.data.categoryId").value(1))
                .andExpect(jsonPath("$.data.categoryName").value("Chest"))
                .andExpect(jsonPath("$.data.active").value(true));

        verify(exerciseService).create(any(ExerciseRequest.class));
    }

    @Test
    void shouldFindAllExercises() throws Exception {

        Exercise exercise = createExercise();

        ExerciseResponse response = createResponse();

        when(exerciseService.findAll())
                .thenReturn(List.of(exercise));

        when(exerciseMapper.toResponse(exercise))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/exercises")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Bench Press"))
                .andExpect(jsonPath("$.data[0].categoryId").value(1))
                .andExpect(jsonPath("$.data[0].categoryName").value("Chest"))
                .andExpect(jsonPath("$.data[0].active").value(true));
    }

    @Test
    void shouldFindExerciseById() throws Exception {

        Exercise exercise = createExercise();

        ExerciseResponse response = createResponse();

        when(exerciseService.findById(1L))
                .thenReturn(exercise);

        when(exerciseMapper.toResponse(exercise))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/exercises/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Bench Press"))
                .andExpect(jsonPath("$.data.description")
                        .value("Barbell bench press"))
                .andExpect(jsonPath("$.data.categoryId").value(1))
                .andExpect(jsonPath("$.data.categoryName").value("Chest"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void shouldFindExercisesByCategory() throws Exception {

        Exercise exercise = createExercise();

        ExerciseResponse response = createResponse();

        when(exerciseService.findAllByCategory(1L))
                .thenReturn(List.of(exercise));

        when(exerciseMapper.toResponse(exercise))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/exercises/category/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Bench Press"))
                .andExpect(jsonPath("$.data[0].categoryId").value(1))
                .andExpect(jsonPath("$.data[0].categoryName").value("Chest"))
                .andExpect(jsonPath("$.data[0].active").value(true));

        verify(exerciseService).findAllByCategory(1L);
    }

    @Test
    void shouldUpdateExercise() throws Exception {

        Exercise exercise = createExercise();
        exercise.setName("Incline Bench Press");

        ExerciseResponse response = new ExerciseResponse(
                1L,
                "Incline Bench Press",
                "Incline barbell bench press",
                1L,
                "Chest",
                true
        );

        when(exerciseService.update(
                eq(1L),
                any(ExerciseRequest.class)
        )).thenReturn(exercise);

        when(exerciseMapper.toResponse(exercise))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/exercises/1")
                                .contentType(APPLICATION_JSON)
                                .content("""
                            {
                                "name": "Incline Bench Press",
                                "description": "Incline barbell bench press",
                                "categoryId": 1
                            }
                            """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name")
                        .value("Incline Bench Press"))
                .andExpect(jsonPath("$.data.categoryId").value(1))
                .andExpect(jsonPath("$.data.categoryName").value("Chest"))
                .andExpect(jsonPath("$.data.active").value(true));

        verify(exerciseService).update(
                eq(1L),
                any(ExerciseRequest.class)
        );
    }

    @Test
    void shouldActivateExercise() throws Exception {

        doNothing()
                .when(exerciseService)
                .activate(1L);

        mockMvc.perform(
                        patch("/api/exercises/1/activate")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Exercise activated successfully"));

        verify(exerciseService).activate(1L);
    }

    @Test
    void shouldDeactivateExercise() throws Exception {

        doNothing()
                .when(exerciseService)
                .deactivate(1L);

        mockMvc.perform(
                        patch("/api/exercises/1/deactivate")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Exercise deactivated successfully"));

        verify(exerciseService).deactivate(1L);
    }

    @Test
    void shouldReturnBadRequestWhenNameIsBlank() throws Exception {

        mockMvc.perform(
                        post("/api/exercises")
                                .contentType(APPLICATION_JSON)
                                .content("""
                            {
                                "name": "",
                                "description": "Barbell bench press",
                                "categoryId": 1
                            }
                            """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(exerciseService);
    }

    @Test
    void shouldReturnBadRequestWhenNameExceedsMaximumLength()
            throws Exception {

        String name = "a".repeat(151);

        mockMvc.perform(
                        post("/api/exercises")
                                .contentType(APPLICATION_JSON)
                                .content("""
                            {
                                "name": "%s",
                                "description": "Test exercise",
                                "categoryId": 1
                            }
                            """.formatted(name))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(exerciseService);
    }

    @Test
    void shouldReturnBadRequestWhenCategoryIdIsNull()
            throws Exception {

        mockMvc.perform(
                        post("/api/exercises")
                                .contentType(APPLICATION_JSON)
                                .content("""
                            {
                                "name": "Bench Press",
                                "description": "Barbell bench press",
                                "categoryId": null
                            }
                            """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(exerciseService);
    }

    private Exercise createExercise() {

        ExerciseCategory category = new ExerciseCategory();
        category.setId(1L);
        category.setName("Chest");
        category.setActive(true);

        Exercise exercise = new Exercise();
        exercise.setId(1L);
        exercise.setName("Bench Press");
        exercise.setDescription("Barbell bench press");
        exercise.setCategory(category);
        exercise.setActive(true);

        return exercise;
    }

    private ExerciseResponse createResponse() {

        return new ExerciseResponse(
                1L,
                "Bench Press",
                "Barbell bench press",
                1L,
                "Chest",
                true
        );
    }

    @Test
    void shouldReturnNotFoundWhenExerciseDoesNotExist() throws Exception {

        when(exerciseService.findById(99L))
                .thenThrow(new ResourceNotFoundException(
                        "Exercise not found with id: 99"
                ));

        mockMvc.perform(
                        get("/api/exercises/99")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Exercise not found with id: 99"));
    }
}