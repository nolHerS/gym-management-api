package com.imanol.gym.user.repository;

import com.imanol.gym.common.repository.BaseRepository;
import com.imanol.gym.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
            select count(tc) > 0 from TrainerClient tc
            where tc.trainer.id = :trainerId and tc.client.id = :clientId
            """)
    boolean existsTrainerClientRelationship(@Param("trainerId") Long trainerId,
                                            @Param("clientId") Long clientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);
}