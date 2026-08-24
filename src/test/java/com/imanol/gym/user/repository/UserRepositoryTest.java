package com.imanol.gym.user.repository;

import com.imanol.gym.user.entity.User;
import com.imanol.gym.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0");

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveUser() {

        User user = createUser(
                "Imanol",
                "García",
                "imanol@test.com"
        );

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId())
                .isNotNull();

        assertThat(savedUser.getEmail())
                .isEqualTo("imanol@test.com");

        assertThat(savedUser.getRole())
                .isEqualTo(UserRole.CLIENT);

        assertThat(savedUser.getActive())
                .isTrue();
    }

    @Test
    void shouldFindUserByEmail() {

        User user = userRepository.save(
                createUser(
                        "Imanol",
                        "García",
                        "imanol@test.com"
                )
        );

        Optional<User> result =
                userRepository.findByEmail("imanol@test.com");

        assertThat(result)
                .isPresent();

        assertThat(result.get().getId())
                .isEqualTo(user.getId());

        assertThat(result.get().getEmail())
                .isEqualTo("imanol@test.com");
    }

    @Test
    void shouldReturnEmptyWhenUserEmailDoesNotExist() {

        Optional<User> result =
                userRepository.findByEmail(
                        "nonexistent@test.com"
                );

        assertThat(result)
                .isEmpty();
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {

        userRepository.save(
                createUser(
                        "Imanol",
                        "García",
                        "imanol@test.com"
                )
        );

        boolean exists =
                userRepository.existsByEmail(
                        "imanol@test.com"
                );

        assertThat(exists)
                .isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {

        boolean exists =
                userRepository.existsByEmail(
                        "nonexistent@test.com"
                );

        assertThat(exists)
                .isFalse();
    }

    private User createUser(
            String firstName,
            String lastName,
            String email
    ) {

        User user = new User();

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword("password123");
        user.setRole(UserRole.CLIENT);
        user.setActive(true);

        return user;
    }
}