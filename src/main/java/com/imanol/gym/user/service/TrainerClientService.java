package com.imanol.gym.user.service;

import com.imanol.gym.user.entity.TrainerClient;

import java.util.List;

public interface TrainerClientService {

    TrainerClient assignClient(
            Long trainerId,
            Long clientId
    );

    List<TrainerClient> findAllByTrainerId(
            Long trainerId
    );

    List<TrainerClient> findAllByClientId(
            Long clientId
    );
}