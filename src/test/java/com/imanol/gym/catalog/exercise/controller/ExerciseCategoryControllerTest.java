package com.imanol.gym.catalog.exercise.controller;

import com.imanol.gym.catalog.exercise.dto.ExerciseCategoryResponse;
import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;
import com.imanol.gym.catalog.exercise.mapper.ExerciseCategoryMapper;
import com.imanol.gym.catalog.exercise.service.ExerciseCategoryService;
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

@WebMvcTest(ExerciseCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExerciseCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExerciseCategoryService exerciseCategoryService;

    @MockitoBean
    private ExerciseCategoryMapper exerciseCategoryMapper;

    @Test
    void shouldCreateExerciseCategory() throws Exception {

        ExerciseCategory category = new ExerciseCategory();
        category.setId(1L);
        category.setName("Chest");
        category.setActive(true);

        ExerciseCategoryResponse response = new ExerciseCategoryResponse(
                1L,
                "Chest",
                true,
                null,
                null
        );

        when(exerciseCategoryMapper.toEntity(any()))
                .thenReturn(category);

        when(exerciseCategoryService.create(category))
                .thenReturn(category);

        when(exerciseCategoryMapper.toResponse(category))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/exercise-categories")
                                .contentType(APPLICATION_JSON)
                                .content("""
                            {
                                "name": "Chest"
                            }
                            """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Chest"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldFindAllExerciseCategories() throws Exception {

        ExerciseCategory category = new ExerciseCategory();
        category.setId(1L);
        category.setName("Chest");
        category.setActive(true);

        ExerciseCategoryResponse response = new ExerciseCategoryResponse(
                1L,
                "Chest",
                true,
                null,
                null
        );

        when(exerciseCategoryService.findAll())
                .thenReturn(List.of(category));

        when(exerciseCategoryMapper.toResponse(category))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/exercise-categories")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Chest"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void shouldFindExerciseCategoryById() throws Exception {

        ExerciseCategory category = new ExerciseCategory();
        category.setId(1L);
        category.setName("Chest");
        category.setActive(true);

        ExerciseCategoryResponse response = new ExerciseCategoryResponse(
                1L,
                "Chest",
                true,
                null,
                null
        );

        when(exerciseCategoryService.findById(1L))
                .thenReturn(category);

        when(exerciseCategoryMapper.toResponse(category))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/exercise-categories/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Chest"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturnBadRequestWhenNameIsBlank() throws Exception {

        mockMvc.perform(
                        post("/api/exercise-categories")
                                .contentType(APPLICATION_JSON)
                                .content("""
                            {
                                "name": ""
                            }
                            """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(exerciseCategoryService);
    }

    @Test
    void shouldUpdateExerciseCategory() throws Exception {

        ExerciseCategory category = new ExerciseCategory();
        category.setId(1L);
        category.setName("Upper Chest");
        category.setActive(true);

        ExerciseCategoryResponse response = new ExerciseCategoryResponse(
                1L,
                "Upper Chest",
                true,
                null,
                null
        );

        when(exerciseCategoryMapper.toEntity(any()))
                .thenReturn(category);

        when(exerciseCategoryService.update(1L, category))
                .thenReturn(category);

        when(exerciseCategoryMapper.toResponse(category))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/exercise-categories/1")
                                .contentType(APPLICATION_JSON)
                                .content("""
                            {
                                "name": "Upper Chest"
                            }
                            """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Upper Chest"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldActivateExerciseCategory() throws Exception {

        doNothing()
                .when(exerciseCategoryService)
                .activate(1L);

        mockMvc.perform(
                        patch("/api/exercise-categories/1/activate")
                )
                .andExpect(status().isNoContent());

        verify(exerciseCategoryService).activate(1L);
    }

    @Test
    void shouldDeactivateExerciseCategory() throws Exception {

        doNothing()
                .when(exerciseCategoryService)
                .deactivate(1L);

        mockMvc.perform(
                        patch("/api/exercise-categories/1/deactivate")
                )
                .andExpect(status().isNoContent());

        verify(exerciseCategoryService).deactivate(1L);
    }

    @Test
    void shouldReturnBadRequestWhenNameExceedsMaximumLength() throws Exception {

        String name = "a".repeat(101);

        mockMvc.perform(
                        post("/api/exercise-categories")
                                .contentType(APPLICATION_JSON)
                                .content("""
                            {
                                "name": "%s"
                            }
                            """.formatted(name))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(exerciseCategoryService);
    }
}