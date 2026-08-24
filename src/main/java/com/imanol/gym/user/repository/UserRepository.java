package com.imanol.gym.user.repository;

import com.imanol.gym.common.repository.BaseRepository;
import com.imanol.gym.user.entity.User;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}