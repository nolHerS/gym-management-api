package com.imanol.gym.user.service;

import com.imanol.gym.common.exception.ResourceAlreadyExistsException;
import com.imanol.gym.user.entity.User;
import com.imanol.gym.user.entity.UserRole;
import com.imanol.gym.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setFirstName("Imanol");
        user.setLastName("García");
        user.setEmail("imanol@test.com");
        user.setPassword("password123");
        user.setRole(UserRole.CLIENT);
        user.setActive(true);
    }

    @Test
    void shouldCreateUser() {

        when(userRepository.existsByEmail(user.getEmail()))
                .thenReturn(false);

        when(userRepository.save(user))
                .thenReturn(user);

        User result = userService.createUser(user);

        assertThat(result)
                .isEqualTo(user);

        verify(userRepository)
                .existsByEmail(user.getEmail());

        verify(userRepository)
                .save(user);
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        when(userRepository.existsByEmail(user.getEmail()))
                .thenReturn(true);

        assertThatThrownBy(
                () -> userService.createUser(user)
        )
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage(
                        "User already exists with email: imanol@test.com"
                );

        verify(userRepository)
                .existsByEmail(user.getEmail());

        verify(userRepository, never())
                .save(any());
    }

    @Test
    void shouldFindUserByEmail() {

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        Optional<User> result =
                userService.findByEmail(user.getEmail());

        assertThat(result)
                .isPresent()
                .contains(user);

        verify(userRepository)
                .findByEmail(user.getEmail());
    }

    @Test
    void shouldReturnEmptyWhenUserEmailDoesNotExist() {

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.empty());

        Optional<User> result =
                userService.findByEmail(user.getEmail());

        assertThat(result)
                .isEmpty();

        verify(userRepository)
                .findByEmail(user.getEmail());
    }
}