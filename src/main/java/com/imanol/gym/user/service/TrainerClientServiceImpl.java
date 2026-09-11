package com.imanol.gym.user.service;

import com.imanol.gym.common.exception.ResourceAlreadyExistsException;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import com.imanol.gym.user.entity.TrainerClient;
import com.imanol.gym.user.entity.User;
import com.imanol.gym.user.entity.UserRole;
import com.imanol.gym.user.repository.TrainerClientRepository;
import com.imanol.gym.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
        User trainer = authenticatedUser();
        if (trainer == null) {
            trainer = userRepository.findById(trainerId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Trainer not found with id: " + trainerId));
        } else if (trainer.getRole() != UserRole.TRAINER
                || !trainer.getId().equals(trainerId)) {
            throw new AccessDeniedException(
                    "Authenticated trainer does not own this relationship");
        }

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
        User authenticated = authenticatedUser();
        if (authenticated != null
                && (authenticated.getRole() != UserRole.TRAINER
                || !authenticated.getId().equals(trainerId))) {
            throw new AccessDeniedException("Trainer relationship access denied");
        }
        return trainerClientRepository
                .findAllByTrainerId(trainerId);
    }

    @Override
    public List<TrainerClient> findAllByClientId(
            Long clientId
    ) {
        User authenticated = authenticatedUser();
        if (authenticated == null) {
            return trainerClientRepository.findAllByClientId(clientId);
        }
        if (authenticated.getRole() == UserRole.CLIENT) {
            if (!authenticated.getId().equals(clientId)) {
                throw new AccessDeniedException("Client relationship access denied");
            }
            return trainerClientRepository.findAllByClientId(clientId);
        }
        if (authenticated.getRole() != UserRole.TRAINER) {
            throw new AccessDeniedException("Relationship access denied");
        }
        return trainerClientRepository.findAllByTrainerId(authenticated.getId())
                .stream()
                .filter(relationship ->
                        relationship.getClient().getId().equals(clientId))
                .toList();
    }

    private User authenticatedUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found"));
    }
}