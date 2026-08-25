package com.imanol.gym.user.service;

import com.imanol.gym.common.exception.ResourceAlreadyExistsException;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import com.imanol.gym.user.entity.TrainerClient;
import com.imanol.gym.user.entity.User;
import com.imanol.gym.user.entity.UserRole;
import com.imanol.gym.user.repository.TrainerClientRepository;
import com.imanol.gym.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerClientServiceImplTest {

    @Mock
    private TrainerClientRepository trainerClientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TrainerClientServiceImpl trainerClientService;

    private User trainer;
    private User client;

    @BeforeEach
    void setUp() {

        trainer = createUser(
                1L,
                "Trainer",
                UserRole.TRAINER
        );

        client = createUser(
                2L,
                "Client",
                UserRole.CLIENT
        );
    }

    @Test
    void shouldAssignClientToTrainer() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(trainer));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(client));

        when(trainerClientRepository
                .existsByTrainerIdAndClientId(1L, 2L))
                .thenReturn(false);

        when(trainerClientRepository.save(any(TrainerClient.class)))
                .thenAnswer(invocation -> {

                    TrainerClient relationship =
                            invocation.getArgument(0);

                    relationship.setId(1L);

                    return relationship;
                });

        TrainerClient result =
                trainerClientService.assignClient(1L, 2L);

        assertThat(result.getId())
                .isEqualTo(1L);

        assertThat(result.getTrainer())
                .isEqualTo(trainer);

        assertThat(result.getClient())
                .isEqualTo(client);

        verify(trainerClientRepository)
                .save(any(TrainerClient.class));
    }

    @Test
    void shouldThrowExceptionWhenTrainerDoesNotExist() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> trainerClientService.assignClient(1L, 2L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Trainer not found with id: 1");

        verify(userRepository, never())
                .findById(2L);

        verifyNoInteractions(trainerClientRepository);
    }

    @Test
    void shouldThrowExceptionWhenClientDoesNotExist() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(trainer));

        when(userRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> trainerClientService.assignClient(1L, 2L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Client not found with id: 2");

        verifyNoInteractions(trainerClientRepository);
    }

    @Test
    void shouldThrowExceptionWhenTrainerDoesNotHaveTrainerRole() {

        trainer.setRole(UserRole.CLIENT);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(trainer));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(client));

        assertThatThrownBy(
                () -> trainerClientService.assignClient(1L, 2L)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User with id 1 is not a trainer");

        verifyNoInteractions(trainerClientRepository);
    }

    @Test
    void shouldThrowExceptionWhenClientDoesNotHaveClientRole() {

        client.setRole(UserRole.TRAINER);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(trainer));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(client));

        assertThatThrownBy(
                () -> trainerClientService.assignClient(1L, 2L)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User with id 2 is not a client");

        verifyNoInteractions(trainerClientRepository);
    }

    @Test
    void shouldThrowExceptionWhenRelationshipAlreadyExists() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(trainer));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(client));

        when(trainerClientRepository
                .existsByTrainerIdAndClientId(1L, 2L))
                .thenReturn(true);

        assertThatThrownBy(
                () -> trainerClientService.assignClient(1L, 2L)
        )
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage(
                        "Trainer-client relationship already exists"
                );

        verify(trainerClientRepository, never())
                .save(any());
    }

    @Test
    void shouldFindAllClientsByTrainerId() {

        TrainerClient firstRelationship =
                createRelationship(trainer, client);

        User secondClient = createUser(
                3L,
                "Client Two",
                UserRole.CLIENT
        );

        TrainerClient secondRelationship =
                createRelationship(trainer, secondClient);

        when(trainerClientRepository.findAllByTrainerId(1L))
                .thenReturn(
                        List.of(
                                firstRelationship,
                                secondRelationship
                        )
                );

        List<TrainerClient> result =
                trainerClientService.findAllByTrainerId(1L);

        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .containsExactly(
                        firstRelationship,
                        secondRelationship
                );
    }

    @Test
    void shouldFindAllTrainersByClientId() {

        TrainerClient firstRelationship =
                createRelationship(trainer, client);

        User secondTrainer = createUser(
                3L,
                "Trainer Two",
                UserRole.TRAINER
        );

        TrainerClient secondRelationship =
                createRelationship(secondTrainer, client);

        when(trainerClientRepository.findAllByClientId(2L))
                .thenReturn(
                        List.of(
                                firstRelationship,
                                secondRelationship
                        )
                );

        List<TrainerClient> result =
                trainerClientService.findAllByClientId(2L);

        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .containsExactly(
                        firstRelationship,
                        secondRelationship
                );
    }

    private User createUser(
            Long id,
            String firstName,
            UserRole role
    ) {

        User user = new User();

        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName("Test");
        user.setEmail(firstName.toLowerCase()
                .replace(" ", "")
                + "@test.com");
        user.setPassword("password123");
        user.setRole(role);
        user.setActive(true);

        return user;
    }

    private TrainerClient createRelationship(
            User trainer,
            User client
    ) {

        TrainerClient relationship =
                new TrainerClient();

        relationship.setTrainer(trainer);
        relationship.setClient(client);

        return relationship;
    }
}