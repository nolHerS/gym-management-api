package com.imanol.gym.user.controller;

import com.imanol.gym.common.exception.ResourceAlreadyExistsException;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import com.imanol.gym.user.dto.UserRequest;
import com.imanol.gym.user.dto.UserResponse;
import com.imanol.gym.user.entity.User;
import com.imanol.gym.user.entity.UserRole;
import com.imanol.gym.user.mapper.UserMapper;
import com.imanol.gym.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @Test
    void shouldCreateUser() throws Exception {

        User user = createUser();

        UserResponse response = createUserResponse();

        when(userMapper.toEntity(any(UserRequest.class)))
                .thenReturn(user);

        when(userService.createUser(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/users")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "firstName": "Imanol",
                                            "lastName": "Garcia",
                                            "email": "imanol@test.com",
                                            "password": "password123",
                                            "role": "CLIENT"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("Imanol"))
                .andExpect(jsonPath("$.data.lastName").value("Garcia"))
                .andExpect(jsonPath("$.data.email")
                        .value("imanol@test.com"))
                .andExpect(jsonPath("$.data.role").value("CLIENT"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void shouldFindUserById() throws Exception {

        User user = createUser();

        UserResponse response = createUserResponse();

        when(userService.findById(1L))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/users/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("Imanol"))
                .andExpect(jsonPath("$.data.email")
                        .value("imanol@test.com"))
                .andExpect(jsonPath("$.data.role").value("CLIENT"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @WithMockUser(username = "imanol@test.com")
    void shouldFindAuthenticatedUser() throws Exception {

        User user = createUser();

        UserResponse response = createUserResponse();

        when(userService.findByEmail("imanol@test.com"))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/users/me")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email")
                        .value("imanol@test.com"))
                .andExpect(jsonPath("$.data.role").value("CLIENT"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @WithMockUser(username = "missing@test.com")
    void shouldReturnNotFoundWhenAuthenticatedUserDoesNotExist() throws Exception {

        when(userService.findByEmail("missing@test.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/api/users/me")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Resource not found with email: missing@test.com"));
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {

        User user = createUser();

        when(userMapper.toEntity(any(UserRequest.class)))
                .thenReturn(user);

        when(userService.createUser(user))
                .thenThrow(
                        new ResourceAlreadyExistsException(
                                "User already exists with email: imanol@test.com"
                        )
                );

        mockMvc.perform(
                        post("/api/users")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "firstName": "Imanol",
                                            "lastName": "Garcia",
                                            "email": "imanol@test.com",
                                            "password": "password123",
                                            "role": "CLIENT"
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "User already exists with email: imanol@test.com"
                        ));
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {

        when(userService.findById(1L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Resource not found with id: 1"
                        )
                );

        mockMvc.perform(
                        get("/api/users/1")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Resource not found with id: 1"));
    }

    @Test
    void shouldReturnBadRequestWhenFirstNameIsBlank() throws Exception {

        mockMvc.perform(
                        post("/api/users")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "firstName": "",
                                            "lastName": "Garcia",
                                            "email": "imanol@test.com",
                                            "password": "password123",
                                            "role": "CLIENT"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    private User createUser() {

        User user = new User();

        user.setId(1L);
        user.setFirstName("Imanol");
        user.setLastName("Garcia");
        user.setEmail("imanol@test.com");
        user.setPassword("password123");
        user.setRole(UserRole.CLIENT);
        user.setActive(true);

        return user;
    }

    private UserResponse createUserResponse() {

        LocalDateTime now = LocalDateTime.now();

        return new UserResponse(
                1L,
                "Imanol",
                "Garcia",
                "imanol@test.com",
                UserRole.CLIENT,
                true,
                now,
                now
        );
    }
}