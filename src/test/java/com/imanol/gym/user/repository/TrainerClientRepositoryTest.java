package com.imanol.gym.user.repository;

import com.imanol.gym.user.entity.TrainerClient;
import com.imanol.gym.user.entity.User;
import com.imanol.gym.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
class TrainerClientRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0");

    @Autowired
    private TrainerClientRepository trainerClientRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveTrainerClient() {

        User trainer = createAndSaveUser(
                "Trainer",
                "One",
                "trainer@test.com",
                UserRole.TRAINER
        );

        User client = createAndSaveUser(
                "Client",
                "One",
                "client@test.com",
                UserRole.CLIENT
        );

        TrainerClient trainerClient = new TrainerClient();
        trainerClient.setTrainer(trainer);
        trainerClient.setClient(client);

        TrainerClient savedTrainerClient =
                trainerClientRepository.save(trainerClient);

        assertThat(savedTrainerClient.getId())
                .isNotNull();

        assertThat(savedTrainerClient.getTrainer().getId())
                .isEqualTo(trainer.getId());

        assertThat(savedTrainerClient.getClient().getId())
                .isEqualTo(client.getId());
    }

    @Test
    void shouldReturnTrueWhenTrainerClientRelationshipExists() {

        User trainer = createAndSaveUser(
                "Trainer",
                "One",
                "trainer@test.com",
                UserRole.TRAINER
        );

        User client = createAndSaveUser(
                "Client",
                "One",
                "client@test.com",
                UserRole.CLIENT
        );

        TrainerClient trainerClient = new TrainerClient();
        trainerClient.setTrainer(trainer);
        trainerClient.setClient(client);

        trainerClientRepository.save(trainerClient);

        boolean exists =
                trainerClientRepository
                        .existsByTrainerIdAndClientId(
                                trainer.getId(),
                                client.getId()
                        );

        assertThat(exists)
                .isTrue();
    }

    @Test
    void shouldReturnFalseWhenTrainerClientRelationshipDoesNotExist() {

        User trainer = createAndSaveUser(
                "Trainer",
                "One",
                "trainer@test.com",
                UserRole.TRAINER
        );

        User client = createAndSaveUser(
                "Client",
                "One",
                "client@test.com",
                UserRole.CLIENT
        );

        boolean exists =
                trainerClientRepository
                        .existsByTrainerIdAndClientId(
                                trainer.getId(),
                                client.getId()
                        );

        assertThat(exists)
                .isFalse();
    }

    @Test
    void shouldFindAllClientsByTrainerId() {

        User trainer = createAndSaveUser(
                "Trainer",
                "One",
                "trainer@test.com",
                UserRole.TRAINER
        );

        User firstClient = createAndSaveUser(
                "Client",
                "One",
                "client1@test.com",
                UserRole.CLIENT
        );

        User secondClient = createAndSaveUser(
                "Client",
                "Two",
                "client2@test.com",
                UserRole.CLIENT
        );

        TrainerClient firstRelationship =
                createTrainerClient(
                        trainer,
                        firstClient
                );

        TrainerClient secondRelationship =
                createTrainerClient(
                        trainer,
                        secondClient
                );

        trainerClientRepository.save(firstRelationship);
        trainerClientRepository.save(secondRelationship);

        List<TrainerClient> result =
                trainerClientRepository
                        .findAllByTrainerId(trainer.getId());

        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .extracting(
                        relationship ->
                                relationship.getClient().getEmail()
                )
                .containsExactlyInAnyOrder(
                        "client1@test.com",
                        "client2@test.com"
                );
    }

    @Test
    void shouldFindAllTrainersByClientId() {

        User firstTrainer = createAndSaveUser(
                "Trainer",
                "One",
                "trainer1@test.com",
                UserRole.TRAINER
        );

        User secondTrainer = createAndSaveUser(
                "Trainer",
                "Two",
                "trainer2@test.com",
                UserRole.TRAINER
        );

        User client = createAndSaveUser(
                "Client",
                "One",
                "client@test.com",
                UserRole.CLIENT
        );

        trainerClientRepository.save(
                createTrainerClient(
                        firstTrainer,
                        client
                )
        );

        trainerClientRepository.save(
                createTrainerClient(
                        secondTrainer,
                        client
                )
        );

        List<TrainerClient> result =
                trainerClientRepository
                        .findAllByClientId(client.getId());

        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .extracting(
                        relationship ->
                                relationship.getTrainer().getEmail()
                )
                .containsExactlyInAnyOrder(
                        "trainer1@test.com",
                        "trainer2@test.com"
                );
    }

    private User createAndSaveUser(
            String firstName,
            String lastName,
            String email,
            UserRole role
    ) {

        User user = new User();

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword("password123");
        user.setRole(role);
        user.setActive(true);

        return userRepository.save(user);
    }

    private TrainerClient createTrainerClient(
            User trainer,
            User client
    ) {

        TrainerClient trainerClient =
                new TrainerClient();

        trainerClient.setTrainer(trainer);
        trainerClient.setClient(client);

        return trainerClient;
    }
}