package com.imanol.gym;

import com.imanol.gym.security.JwtService;
import com.imanol.gym.user.entity.TrainerClient;
import com.imanol.gym.user.entity.User;
import com.imanol.gym.user.entity.UserRole;
import com.imanol.gym.user.repository.TrainerClientRepository;
import com.imanol.gym.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Date;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class Phase3HttpSecurityTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired TrainerClientRepository trainerClientRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    @Value("${app.jwt.secret}")
    String jwtSecret;

    private User trainerA;
    private User trainerB;
    private User clientA;
    private User clientB;

    @BeforeEach
    void fixtures() {
        trainerA = user("phase3-trainer-a@example.com", UserRole.TRAINER);
        trainerB = user("phase3-trainer-b@example.com", UserRole.TRAINER);
        clientA = user("phase3-client-a@example.com", UserRole.CLIENT);
        clientB = user("phase3-client-b@example.com", UserRole.CLIENT);

        if (!trainerClientRepository.existsByTrainerIdAndClientId(trainerA.getId(), clientA.getId())) {
            TrainerClient relation = new TrainerClient();
            relation.setTrainer(trainerA);
            relation.setClient(clientA);
            trainerClientRepository.saveAndFlush(relation);
        }
    }

    @Test
    void unauthenticatedAndInvalidJwtRequestsReturn401ApiResponse() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + expiredToken()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void invalidLoginsReturn401ApiResponse() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"phase3-client-a@example.com","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"missing-phase3@example.com","password":"password123"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void clientCatalogWritesReturn403() throws Exception {
        String token = token(clientA);
        String exercise = """
                {"name":"Blocked exercise","description":"test","categoryId":1}
                """;

        mockMvc.perform(post("/api/exercises").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(exercise))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        mockMvc.perform(put("/api/exercises/1").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(exercise))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/exercises/1/activate").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/exercise-categories").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"name":"Blocked category"}
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/workout-templates").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"name":"Blocked template"}
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/workout-templates/1").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"name":"Blocked template"}
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/workout-templates/1/exercises").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"exerciseId":1,"orderIndex":1}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void trainerCanReachTrainerOnlyEndpoint() throws Exception {
        mockMvc.perform(post("/api/exercises").header("Authorization", "Bearer " + token(trainerA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Allowed exercise","description":"test","categoryId":999999}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void duplicateTrainerClientRelationReturns409() throws Exception {
        String body = "{\"trainerId\":" + trainerA.getId() + ",\"clientId\":" + clientB.getId() + "}";
        mockMvc.perform(post("/api/trainer-clients").header("Authorization", "Bearer " + token(trainerA))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/trainer-clients").header("Authorization", "Bearer " + token(trainerA))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", anyOf(is("Trainer-client relationship already exists"),
                        is("The request conflicts with the current resource state"))));
    }

    @Test
    void httpIdorRequestsAreRejected() throws Exception {
        String clientToken = token(clientA);
        String trainerToken = token(trainerA);

        mockMvc.perform(get("/api/users/" + clientB.getId()).header("Authorization", "Bearer " + clientToken))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/users/" + trainerB.getId()).header("Authorization", "Bearer " + trainerToken))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/trainer-clients/trainer/" + trainerB.getId())
                        .header("Authorization", "Bearer " + trainerToken))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/trainer-clients/client/" + clientB.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(post("/api/trainer-clients")
                        .header("Authorization", "Bearer " + trainerA.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trainerId\":" + trainerB.getId() + ",\"clientId\":" + clientB.getId() + "}"))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/workout-plans/clients/" + clientB.getId())
                        .header("Authorization", "Bearer " + trainerToken))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/workout-plans/999999")
                        .header("Authorization", "Bearer " + trainerToken))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(patch("/api/workout-plans/999999")
                        .header("Authorization", "Bearer " + trainerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/workout-plans/999999")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(patch("/api/workout-plans/999999")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(post("/api/workout-plans/999999/days")
                        .header("Authorization", "Bearer " + trainerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(put("/api/workout-plans/exercises/999999")
                        .header("Authorization", "Bearer " + trainerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/nutrition-plans/999999")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(post("/api/nutrition-plans/clients/" + clientB.getId())
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void invalidRequestsReturn400AndExerciseReferencesRemainCompatible() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("""
                        {"firstName":"A","lastName":"B","email":"invalid","password":"123","role":"TRAINER"}
                        """))
                .andExpect(status().isBadRequest());

        String token = token(trainerA);
        String invalidExercise = """
                {"orderIndex":0,"sets":0,"repetitions":0,"restSeconds":-1}
                """;
        mockMvc.perform(post("/api/workout-plans/%d/days/1/exercises".formatted(999999L))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(invalidExercise))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/workout-plans/999999")
                        .header("Authorization", "Bearer " + token(trainerA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"NOT_A_STATUS"}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/workout-plans/me/week?weekStart=not-a-date")
                        .header("Authorization", "Bearer " + token(clientA)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/foods").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"name":"Invalid food","calories":-1}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/workout-plans/clients/%d".formatted(clientA.getId()))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"startDate":"2026-02-01","endDate":"2026-01-01","days":[]}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void workoutPlanExerciseReferenceSupportsExerciseAndTemplateReferences() {
        var exerciseReference = new com.imanol.gym.catalog.workout.dto.WorkoutPlanExerciseRequest(
                null, 1L, 1, 3, 10, 60);
        var templateReference = new com.imanol.gym.catalog.workout.dto.WorkoutPlanExerciseRequest(
                1L, null, 1, 3, 10, 60);
        var missingReference = new com.imanol.gym.catalog.workout.dto.WorkoutPlanExerciseRequest(
                null, null, 1, 3, 10, 60);
        var bothReferences = new com.imanol.gym.catalog.workout.dto.WorkoutPlanExerciseRequest(
                1L, 1L, 1, 3, 10, 60);

        org.junit.jupiter.api.Assertions.assertTrue(exerciseReference.hasExerciseReference());
        org.junit.jupiter.api.Assertions.assertTrue(templateReference.hasExerciseReference());
        org.junit.jupiter.api.Assertions.assertFalse(missingReference.hasExerciseReference());
        org.junit.jupiter.api.Assertions.assertTrue(bothReferences.hasExerciseReference());
    }

    private User user(String email, UserRole role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setFirstName("Phase3");
            user.setLastName(role.name());
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(role);
            user.setActive(true);
            return userRepository.saveAndFlush(user);
        });
    }

    private String token(User user) {
        return jwtService.generateToken(org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail()).password(user.getPassword()).roles(user.getRole().name()).build());
    }

    private String expiredToken() {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        return Jwts.builder().subject(clientA.getEmail()).issuedAt(new Date(0))
                .expiration(new Date(1)).signWith(key).compact();
    }
}
