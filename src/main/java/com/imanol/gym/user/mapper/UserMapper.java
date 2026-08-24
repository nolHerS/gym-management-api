package com.imanol.gym.user.mapper;

import com.imanol.gym.user.dto.UserRequest;
import com.imanol.gym.user.dto.UserResponse;
import com.imanol.gym.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRequest request);

    UserResponse toResponse(User user);
}