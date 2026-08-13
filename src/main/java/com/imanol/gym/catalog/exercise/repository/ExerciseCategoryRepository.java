package com.imanol.gym.catalog.exercise.repository;

import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseCategoryRepository extends JpaRepository<ExerciseCategory, Long> {

    List<ExerciseCategory> findAllByActiveTrue();

}
