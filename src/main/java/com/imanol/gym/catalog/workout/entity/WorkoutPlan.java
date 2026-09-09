package com.imanol.gym.catalog.workout.entity;

import com.imanol.gym.common.entity.BaseEntity;
import com.imanol.gym.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "workout_plans")
public class WorkoutPlan extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private User trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_template_id")
    private WorkoutTemplate sourceTemplate;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkoutPlanStatus status;

    @OneToMany(mappedBy = "workoutPlan", fetch = FetchType.LAZY)
    @OrderBy("dayOfWeek ASC")
    private List<WorkoutPlanDay> days = new ArrayList<>();
}
