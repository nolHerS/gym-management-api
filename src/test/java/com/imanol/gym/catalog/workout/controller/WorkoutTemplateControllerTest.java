package com.imanol.gym.catalog.workout.controller;

import com.imanol.gym.catalog.workout.dto.WorkoutTemplateRequest;
import com.imanol.gym.catalog.workout.dto.WorkoutTemplateResponse;
import com.imanol.gym.catalog.workout.entity.WorkoutTemplate;
import com.imanol.gym.catalog.workout.mapper.WorkoutTemplateMapper;
import com.imanol.gym.catalog.workout.service.WorkoutTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkoutTemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkoutTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkoutTemplateService workoutTemplateService;

    @MockitoBean
    private WorkoutTemplateMapper workoutTemplateMapper;

    @Test
    void shouldCreateWorkoutTemplate() throws Exception {

        WorkoutTemplate template = createTemplate();

        WorkoutTemplateResponse response = createResponse();

        when(workoutTemplateMapper.toEntity(any(WorkoutTemplateRequest.class)))
                .thenReturn(template);

        when(workoutTemplateService.create(template))
                .thenReturn(template);

        when(workoutTemplateMapper.toResponse(template))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/workout-templates")
                                .contentType(APPLICATION_JSON)
                                .content("""
                            {
                                "name": "Hypertrophy 4 Days",
                                "description": "Four day hypertrophy routine"
                            }
                            """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name")
                        .value("Hypertrophy 4 Days"))
                .andExpect(jsonPath("$.data.description")
                        .value("Four day hypertrophy routine"))
                .andExpect(jsonPath("$.data.active").value(true));

        verify(workoutTemplateService).create(template);
    }

    @Test
    void shouldFindAllWorkoutTemplates() throws Exception {

        WorkoutTemplate template = createTemplate();

        WorkoutTemplateResponse response = createResponse();

        when(workoutTemplateService.findAll())
                .thenReturn(List.of(template));

        when(workoutTemplateMapper.toResponse(template))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/workout-templates")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name")
                        .value("Hypertrophy 4 Days"))
                .andExpect(jsonPath("$.data[0].active").value(true));
    }

    @Test
    void shouldFindWorkoutTemplateById() throws Exception {

        WorkoutTemplate template = createTemplate();

        WorkoutTemplateResponse response = createResponse();

        when(workoutTemplateService.findById(1L))
                .thenReturn(template);

        when(workoutTemplateMapper.toResponse(template))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/workout-templates/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name")
                        .value("Hypertrophy 4 Days"))
                .andExpect(jsonPath("$.data.description")
                        .value("Four day hypertrophy routine"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void shouldUpdateWorkoutTemplate() throws Exception {

        WorkoutTemplate template = createTemplate();
        template.setName("Strength 3 Days");
        template.setDescription("Three day strength routine");

        WorkoutTemplateResponse response = new WorkoutTemplateResponse(
                1L,
                "Strength 3 Days",
                "Three day strength routine",
                true
        );

        when(workoutTemplateMapper.toEntity(any(WorkoutTemplateRequest.class)))
                .thenReturn(template);

        when(workoutTemplateService.update(eq(1L), eq(template)))
                .thenReturn(template);

        when(workoutTemplateMapper.toResponse(template))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/workout-templates/1")
                                .contentType(APPLICATION_JSON)
                                .content("""
                            {
                                "name": "Strength 3 Days",
                                "description": "Three day strength routine"
                            }
                            """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name")
                        .value("Strength 3 Days"))
                .andExpect(jsonPath("$.data.description")
                        .value("Three day strength routine"))
                .andExpect(jsonPath("$.data.active").value(true));

        verify(workoutTemplateService).update(1L, template);
    }

    @Test
    void shouldActivateWorkoutTemplate() throws Exception {

        doNothing()
                .when(workoutTemplateService)
                .activate(1L);

        mockMvc.perform(
                        patch("/api/workout-templates/1/activate")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Workout template activated successfully"));

        verify(workoutTemplateService).activate(1L);
    }

    @Test
    void shouldDeactivateWorkoutTemplate() throws Exception {

        doNothing()
                .when(workoutTemplateService)
                .deactivate(1L);

        mockMvc.perform(
                        patch("/api/workout-templates/1/deactivate")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message")
                        .value("Workout template deactivated successfully"));

        verify(workoutTemplateService).deactivate(1L);
    }

    @Test
    void shouldReturnBadRequestWhenNameIsBlank() throws Exception {

        mockMvc.perform(
                        post("/api/workout-templates")
                                .contentType(APPLICATION_JSON)
                                .content("""
                            {
                                "name": "",
                                "description": "Test workout"
                            }
                            """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(workoutTemplateService);
    }

    @Test
    void shouldReturnBadRequestWhenNameExceedsMaximumLength()
            throws Exception {

        String name = "a".repeat(151);

        mockMvc.perform(
                        post("/api/workout-templates")
                                .contentType(APPLICATION_JSON)
                                .content("""
                            {
                                "name": "%s",
                                "description": "Test workout"
                            }
                            """.formatted(name))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(workoutTemplateService);
    }

    private WorkoutTemplate createTemplate() {

        WorkoutTemplate template = new WorkoutTemplate();
        template.setId(1L);
        template.setName("Hypertrophy 4 Days");
        template.setDescription("Four day hypertrophy routine");
        template.setActive(true);

        return template;
    }

    private WorkoutTemplateResponse createResponse() {

        return new WorkoutTemplateResponse(
                1L,
                "Hypertrophy 4 Days",
                "Four day hypertrophy routine",
                true
        );
    }
}