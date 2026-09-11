package com.imanol.gym.user.service;

import com.imanol.gym.common.exception.ResourceAlreadyExistsException;
import com.imanol.gym.common.service.BaseServiceImpl;
import com.imanol.gym.user.entity.User;
import com.imanol.gym.user.entity.UserRole;
import com.imanol.gym.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl
        extends BaseServiceImpl<User,Long>
        implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        super(userRepository);
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserServiceImpl(UserRepository userRepository) {
        this(userRepository, null);
    }

    @Override
    public Optional<User> findByEmail(String email) {

        return userRepository.findByEmail(email);
    }

    @Override
    public User createUser(User user) {
        user.setRole(UserRole.CLIENT);
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResourceAlreadyExistsException(
                    "User already exists with email: " + user.getEmail()
            );
        }

        if (passwordEncoder != null && !user.getPassword().startsWith("$2")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    @Override
    public User findByIdForAuthenticatedUser(User requester, Long id) {
        if (requester.getId().equals(id)) {
            return requester;
        }
        if (requester.getRole() != UserRole.TRAINER) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "User is not allowed");
        }
        User target = findById(id);
        if (target.getRole() == UserRole.CLIENT
                && userRepository.existsTrainerClientRelationship(
                        requester.getId(), id)) {
            return target;
        }
        throw new org.springframework.security.access.AccessDeniedException(
                "User is not related to this trainer");
    }
}