package com.imanol.gym.catalog.workout.session.entity;

import com.imanol.gym.catalog.workout.entity.WorkoutPlan;
import com.imanol.gym.common.entity.BaseEntity;
import com.imanol.gym.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "workout_sessions",
        uniqueConstraints = @UniqueConstraint(name = "uk_workout_sessions_active",
                columnNames = {"client_id", "workout_plan_id", "active_marker"}))
@Getter @Setter @NoArgsConstructor
public class WorkoutSession extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "workout_plan_id", nullable = false)
    private WorkoutPlan workoutPlan;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "client_id", nullable = false)
    private User client;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private WorkoutSessionStatus status;
    @Column(nullable = false) private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime cancelledAt;
    private Integer durationSeconds;
    @Column(columnDefinition = "TEXT") private String notes;
    @Version @Column(nullable = false) private Long version;
    @Column(name = "active_marker", insertable = false, updatable = false)
    private Integer activeMarker;
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC") @Fetch(FetchMode.SUBSELECT)
    private List<WorkoutSessionExercise> exercises = new ArrayList<>();
}
