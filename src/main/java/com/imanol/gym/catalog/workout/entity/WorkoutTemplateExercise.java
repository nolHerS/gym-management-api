package com.imanol.gym.catalog.workout.entity;

import com.imanol.gym.catalog.exercise.entity.Exercise;
import com.imanol.gym.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "workout_template_exercises",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_workout_template_exercises_order",
                        columnNames = {
                                "workout_template_id",
                                "order_index"
                        }
                )
        }
)
public class WorkoutTemplateExercise extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "workout_template_id",
            nullable = false
    )
    private WorkoutTemplate workoutTemplate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "exercise_id",
            nullable = false
    )
    private Exercise exercise;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(nullable = false)
    private Integer sets;

    @Column(nullable = false)
    private Integer repetitions;

    @Column(name = "rest_seconds", nullable = false)
    private Integer restSeconds;
}