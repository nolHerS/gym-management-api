package com.imanol.gym.user.mapper;

import com.imanol.gym.user.dto.TrainerClientResponse;
import com.imanol.gym.user.entity.TrainerClient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainerClientMapper {

    @Mapping(
            target = "trainerId",
            source = "trainer.id"
    )
    @Mapping(
            target = "clientId",
            source = "client.id"
    )
    TrainerClientResponse toResponse(
            TrainerClient trainerClient
    );
}