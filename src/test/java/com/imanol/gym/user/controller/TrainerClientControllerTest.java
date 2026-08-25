package com.imanol.gym.user.controller;

import com.imanol.gym.common.exception.ResourceAlreadyExistsException;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import com.imanol.gym.user.dto.TrainerClientResponse;
import com.imanol.gym.user.entity.TrainerClient;
import com.imanol.gym.user.entity.User;
import com.imanol.gym.user.entity.UserRole;
import com.imanol.gym.user.mapper.TrainerClientMapper;
import com.imanol.gym.user.service.TrainerClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerClientController.class)
@AutoConfigureMockMvc(addFilters = false)
class TrainerClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainerClientService trainerClientService;

    @MockitoBean
    private TrainerClientMapper trainerClientMapper;

    @Test
    void shouldAssignClientToTrainer() throws Exception {

        TrainerClient trainerClient =
                createTrainerClient();

        TrainerClientResponse response =
                createTrainerClientResponse();

        when(trainerClientService.assignClient(1L, 2L))
                .thenReturn(trainerClient);

        when(trainerClientMapper.toResponse(trainerClient))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/trainer-clients")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "trainerId": 1,
                                            "clientId": 2
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.trainerId").value(1))
                .andExpect(jsonPath("$.data.clientId").value(2));
    }

    @Test
    void shouldFindAllClientsByTrainerId() throws Exception {

        TrainerClient firstRelationship =
                createTrainerClient();

        TrainerClient secondRelationship =
                createTrainerClient();

        secondRelationship.setId(2L);

        TrainerClientResponse firstResponse =
                createTrainerClientResponse();

        TrainerClientResponse secondResponse =
                new TrainerClientResponse(
                        2L,
                        1L,
                        3L,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );

        when(trainerClientService.findAllByTrainerId(1L))
                .thenReturn(
                        List.of(
                                firstRelationship,
                                secondRelationship
                        )
                );

        when(trainerClientMapper.toResponse(firstRelationship))
                .thenReturn(firstResponse);

        when(trainerClientMapper.toResponse(secondRelationship))
                .thenReturn(secondResponse);

        mockMvc.perform(
                        get("/api/trainer-clients/trainer/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].trainerId").value(1))
                .andExpect(jsonPath("$.data[0].clientId").value(2))
                .andExpect(jsonPath("$.data[1].trainerId").value(1))
                .andExpect(jsonPath("$.data[1].clientId").value(3));
    }

    @Test
    void shouldFindAllTrainersByClientId() throws Exception {

        TrainerClient firstRelationship =
                createTrainerClient();

        TrainerClient secondRelationship =
                createTrainerClient();

        secondRelationship.setId(2L);

        TrainerClientResponse firstResponse =
                createTrainerClientResponse();

        TrainerClientResponse secondResponse =
                new TrainerClientResponse(
                        2L,
                        3L,
                        2L,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );

        when(trainerClientService.findAllByClientId(2L))
                .thenReturn(
                        List.of(
                                firstRelationship,
                                secondRelationship
                        )
                );

        when(trainerClientMapper.toResponse(firstRelationship))
                .thenReturn(firstResponse);

        when(trainerClientMapper.toResponse(secondRelationship))
                .thenReturn(secondResponse);

        mockMvc.perform(
                        get("/api/trainer-clients/client/2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].trainerId").value(1))
                .andExpect(jsonPath("$.data[0].clientId").value(2))
                .andExpect(jsonPath("$.data[1].trainerId").value(3))
                .andExpect(jsonPath("$.data[1].clientId").value(2));
    }

    @Test
    void shouldReturnConflictWhenRelationshipAlreadyExists()
            throws Exception {

        when(trainerClientService.assignClient(1L, 2L))
                .thenThrow(
                        new ResourceAlreadyExistsException(
                                "Trainer-client relationship already exists"
                        )
                );

        mockMvc.perform(
                        post("/api/trainer-clients")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "trainerId": 1,
                                            "clientId": 2
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "Trainer-client relationship already exists"
                ));
    }

    @Test
    void shouldReturnNotFoundWhenTrainerDoesNotExist()
            throws Exception {

        when(trainerClientService.assignClient(1L, 2L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Trainer not found with id: 1"
                        )
                );

        mockMvc.perform(
                        post("/api/trainer-clients")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "trainerId": 1,
                                            "clientId": 2
                                        }
                                        """)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(
                        "Trainer not found with id: 1"
                ));
    }

    @Test
    void shouldReturnBadRequestWhenTrainerOrClientIdIsNull()
            throws Exception {

        mockMvc.perform(
                        post("/api/trainer-clients")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "clientId": 2
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/trainer-clients")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "trainerId": 1
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    private TrainerClient createTrainerClient() {

        User trainer = new User();
        trainer.setId(1L);
        trainer.setFirstName("Trainer");
        trainer.setLastName("Test");
        trainer.setEmail("trainer@test.com");
        trainer.setPassword("password123");
        trainer.setRole(UserRole.TRAINER);
        trainer.setActive(true);

        User client = new User();
        client.setId(2L);
        client.setFirstName("Client");
        client.setLastName("Test");
        client.setEmail("client@test.com");
        client.setPassword("password123");
        client.setRole(UserRole.CLIENT);
        client.setActive(true);

        TrainerClient trainerClient =
                new TrainerClient();

        trainerClient.setId(1L);
        trainerClient.setTrainer(trainer);
        trainerClient.setClient(client);

        return trainerClient;
    }

    private TrainerClientResponse createTrainerClientResponse() {

        LocalDateTime now = LocalDateTime.now();

        return new TrainerClientResponse(
                1L,
                1L,
                2L,
                now,
                now
        );
    }
}