package com.imanol.gym.user.service;

import com.imanol.gym.common.exception.ResourceAlreadyExistsException;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import com.imanol.gym.user.entity.TrainerClient;
import com.imanol.gym.user.entity.User;
import com.imanol.gym.user.entity.UserRole;
import com.imanol.gym.user.repository.TrainerClientRepository;
import com.imanol.gym.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainerClientServiceImpl
        implements TrainerClientService {

    private final TrainerClientRepository trainerClientRepository;
    private final UserRepository userRepository;

    public TrainerClientServiceImpl(
            TrainerClientRepository trainerClientRepository,
            UserRepository userRepository
    ) {
        this.trainerClientRepository = trainerClientRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TrainerClient assignClient(
            Long trainerId,
            Long clientId
    ) {

        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Trainer not found with id: " + trainerId
                        )
                );

        User client = userRepository.findById(clientId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Client not found with id: " + clientId
                        )
                );

        if (trainer.getRole() != UserRole.TRAINER) {
            throw new IllegalArgumentException(
                    "User with id " + trainerId
                            + " is not a trainer"
            );
        }

        if (client.getRole() != UserRole.CLIENT) {
            throw new IllegalArgumentException(
                    "User with id " + clientId
                            + " is not a client"
            );
        }

        if (trainerClientRepository
                .existsByTrainerIdAndClientId(
                        trainerId,
                        clientId
                )) {

            throw new ResourceAlreadyExistsException(
                    "Trainer-client relationship already exists"
            );
        }

        TrainerClient trainerClient =
                new TrainerClient();

        trainerClient.setTrainer(trainer);
        trainerClient.setClient(client);

        return trainerClientRepository.save(trainerClient);
    }

    @Override
    public List<TrainerClient> findAllByTrainerId(
            Long trainerId
    ) {

        return trainerClientRepository
                .findAllByTrainerId(trainerId);
    }

    @Override
    public List<TrainerClient> findAllByClientId(
            Long clientId
    ) {

        return trainerClientRepository
                .findAllByClientId(clientId);
    }
}