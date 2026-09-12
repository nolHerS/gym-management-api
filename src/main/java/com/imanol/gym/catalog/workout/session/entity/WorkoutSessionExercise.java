package com.imanol.gym.catalog.workout.session.entity;

import com.imanol.gym.catalog.exercise.entity.Exercise;
import com.imanol.gym.catalog.workout.entity.WorkoutPlanExercise;
import com.imanol.gym.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import java.util.ArrayList; import java.util.List;

@Entity @Table(name = "workout_session_exercises")
@Getter @Setter @NoArgsConstructor
public class WorkoutSessionExercise extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "session_id", nullable = false)
    private WorkoutSession session;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;
    @Column(name = "source_workout_plan_exercise_id")
    private Long sourceWorkoutPlanExerciseId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_workout_plan_exercise_id", insertable = false,
            updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private WorkoutPlanExercise sourceWorkoutPlanExercise;
    @Column(name = "exercise_name_snapshot", nullable = false, length = 150) private String exerciseNameSnapshot;
    @Column(name = "order_index", nullable = false) private Integer orderIndex;
    @Column(name = "planned_sets", nullable = false) private Integer plannedSets;
    @Column(name = "planned_repetitions", nullable = false) private Integer plannedRepetitions;
    @Column(name = "planned_rest_seconds", nullable = false) private Integer plannedRestSeconds;
    @Column(nullable = false) private Boolean completed = false;
    @Column(columnDefinition = "TEXT") private String notes;
    @OneToMany(mappedBy = "sessionExercise", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("setNumber ASC") @Fetch(FetchMode.SUBSELECT)
    private List<WorkoutSessionSet> sets = new ArrayList<>();
}
