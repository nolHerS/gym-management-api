package com.imanol.gym.user.repository;

import com.imanol.gym.common.repository.BaseRepository;
import com.imanol.gym.user.entity.TrainerClient;

import java.util.List;
import java.util.Optional;

public interface TrainerClientRepository
        extends BaseRepository<TrainerClient, Long> {

    boolean existsByTrainerIdAndClientId(
            Long trainerId,
            Long clientId
    );

    List<TrainerClient> findAllByTrainerId(
            Long trainerId
    );

    List<TrainerClient> findAllByClientId(
            Long clientId
    );

    Optional<TrainerClient> findByTrainerIdAndClientId(
            Long trainerId,
            Long clientId
    );
}