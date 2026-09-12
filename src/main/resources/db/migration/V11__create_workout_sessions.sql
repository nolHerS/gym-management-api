CREATE TABLE workout_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    workout_plan_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at DATETIME NOT NULL,
    finished_at DATETIME NULL,
    cancelled_at DATETIME NULL,
    duration_seconds INT NULL,
    notes TEXT NULL,
    version BIGINT NOT NULL,
    active_marker INT GENERATED ALWAYS AS (IF(status = 'IN_PROGRESS', 1, NULL)) STORED,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT pk_workout_sessions PRIMARY KEY (id),
    CONSTRAINT fk_workout_sessions_plan FOREIGN KEY (workout_plan_id) REFERENCES workout_plans(id),
    CONSTRAINT fk_workout_sessions_client FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT uk_workout_sessions_active UNIQUE (client_id, workout_plan_id, active_marker)
);
CREATE INDEX idx_workout_sessions_client_started ON workout_sessions(client_id, started_at);
CREATE INDEX idx_workout_sessions_plan ON workout_sessions(workout_plan_id);
CREATE INDEX idx_workout_sessions_started ON workout_sessions(started_at);
CREATE INDEX idx_workout_sessions_status ON workout_sessions(status);

CREATE TABLE workout_session_exercises (
    id BIGINT NOT NULL AUTO_INCREMENT, session_id BIGINT NOT NULL, exercise_id BIGINT NOT NULL,
    source_workout_plan_exercise_id BIGINT NULL, exercise_name_snapshot VARCHAR(150) NOT NULL,
    order_index INT NOT NULL, planned_sets INT NOT NULL, planned_repetitions INT NOT NULL,
    planned_rest_seconds INT NOT NULL, completed BOOLEAN NOT NULL, notes TEXT NULL,
    created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
    CONSTRAINT pk_workout_session_exercises PRIMARY KEY(id),
    CONSTRAINT fk_session_exercises_session FOREIGN KEY(session_id) REFERENCES workout_sessions(id),
    CONSTRAINT fk_session_exercises_exercise FOREIGN KEY(exercise_id) REFERENCES exercises(id),
    CONSTRAINT uk_session_exercises_order UNIQUE(session_id, order_index)
);
CREATE INDEX idx_session_exercises_session ON workout_session_exercises(session_id);
CREATE INDEX idx_session_exercises_exercise ON workout_session_exercises(exercise_id);
CREATE TABLE workout_session_sets (
    id BIGINT NOT NULL AUTO_INCREMENT, session_exercise_id BIGINT NOT NULL, set_number INT NOT NULL,
    planned_repetitions INT NOT NULL, actual_repetitions INT NULL, weight DECIMAL(10,2) NULL,
    rir INT NULL, rpe DECIMAL(4,2) NULL, completed BOOLEAN NOT NULL, performed_at DATETIME NULL,
    created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
    CONSTRAINT pk_workout_session_sets PRIMARY KEY(id),
    CONSTRAINT fk_session_sets_exercise FOREIGN KEY(session_exercise_id) REFERENCES workout_session_exercises(id),
    CONSTRAINT uk_workout_session_sets_number UNIQUE(session_exercise_id, set_number),
    CONSTRAINT chk_session_sets_values CHECK(
        (actual_repetitions IS NULL OR actual_repetitions >= 0)
        AND (weight IS NULL OR weight >= 0)
        AND (rir IS NULL OR rir >= 0)
        AND (rpe IS NULL OR (rpe >= 0 AND rpe <= 10))
    )
);
CREATE INDEX idx_session_sets_exercise ON workout_session_sets(session_exercise_id);
