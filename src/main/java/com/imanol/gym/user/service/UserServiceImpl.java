package com.imanol.gym.user.service;

import com.imanol.gym.common.exception.ResourceAlreadyExistsException;
import com.imanol.gym.common.service.BaseServiceImpl;
import com.imanol.gym.user.entity.User;
import com.imanol.gym.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl
        extends BaseServiceImpl<User,Long>
        implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(
            UserRepository userRepository
    ) {
        super(userRepository);
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {

        return userRepository.findByEmail(email);
    }

    @Override
    public User createUser(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResourceAlreadyExistsException(
                    "User already exists with email: " + user.getEmail()
            );
        }

        return userRepository.save(user);
    }
}