package com.imanol.gym.catalog.workout.entity;

import com.imanol.gym.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "workout_plan_days",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_workout_plan_days_plan_day",
                columnNames = {"workout_plan_id", "day_of_week"}
        )
)
public class WorkoutPlanDay extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_plan_id", nullable = false)
    private WorkoutPlan workoutPlan;

    @Column(
            name = "day_of_week",
            nullable = false,
            columnDefinition = "TINYINT"
    )
    private Integer dayOfWeek;

    @OneToMany(mappedBy = "workoutPlanDay", fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<WorkoutPlanExercise> exercises = new ArrayList<>();
}
