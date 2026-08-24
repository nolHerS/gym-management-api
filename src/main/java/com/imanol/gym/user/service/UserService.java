package com.imanol.gym.user.service;

import com.imanol.gym.common.service.BaseService;
import com.imanol.gym.user.entity.User;

import java.util.Optional;

public interface UserService extends BaseService<User,Long> {

    Optional<User> findByEmail(String email);

    User createUser(User user);
}