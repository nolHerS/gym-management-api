package com.imanol.gym.catalog.workout.session.entity;

import com.imanol.gym.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "workout_session_sets",
        uniqueConstraints = @UniqueConstraint(name = "uk_workout_session_sets_number",
                columnNames = {"session_exercise_id", "set_number"}))
@Getter @Setter @NoArgsConstructor
public class WorkoutSessionSet extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "session_exercise_id", nullable = false)
    private WorkoutSessionExercise sessionExercise;
    @Column(name = "set_number", nullable = false) private Integer setNumber;
    @Column(name = "planned_repetitions", nullable = false) private Integer plannedRepetitions;
    @Column(nullable = true) private Integer actualRepetitions;
    @Column(precision = 10, scale = 2) private BigDecimal weight;
    @Column private Integer rir;
    @Column(precision = 4, scale = 2) private BigDecimal rpe;
    @Column(nullable = false) private Boolean completed = false;
    @Column private LocalDateTime performedAt;
}
