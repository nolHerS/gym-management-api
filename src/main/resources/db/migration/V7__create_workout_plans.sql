-- =====================================================
-- V7 - Create client workout plans
-- =====================================================

CREATE TABLE workout_plans (
    id BIGINT NOT NULL AUTO_INCREMENT,
    client_id BIGINT NOT NULL,
    trainer_id BIGINT NOT NULL,
    source_template_id BIGINT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    CONSTRAINT pk_workout_plans PRIMARY KEY (id),
    CONSTRAINT fk_workout_plans_client
        FOREIGN KEY (client_id) REFERENCES users (id),
    CONSTRAINT fk_workout_plans_trainer
        FOREIGN KEY (trainer_id) REFERENCES users (id),
    CONSTRAINT fk_workout_plans_source_template
        FOREIGN KEY (source_template_id) REFERENCES workout_templates (id),
    CONSTRAINT chk_workout_plans_dates
        CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_workout_plans_client
    ON workout_plans (client_id);

CREATE INDEX idx_workout_plans_trainer
    ON workout_plans (trainer_id);

CREATE INDEX idx_workout_plans_source_template
    ON workout_plans (source_template_id);

CREATE INDEX idx_workout_plans_client_status
    ON workout_plans (client_id, status);

CREATE INDEX idx_workout_plans_client_dates
    ON workout_plans (client_id, start_date, end_date);

CREATE TABLE workout_plan_days (
    id BIGINT NOT NULL AUTO_INCREMENT,
    workout_plan_id BIGINT NOT NULL,
    day_of_week TINYINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    CONSTRAINT pk_workout_plan_days PRIMARY KEY (id),
    CONSTRAINT fk_workout_plan_days_plan
        FOREIGN KEY (workout_plan_id) REFERENCES workout_plans (id),
    CONSTRAINT uk_workout_plan_days_plan_day
        UNIQUE (workout_plan_id, day_of_week),
    CONSTRAINT chk_workout_plan_days_day
        CHECK (day_of_week BETWEEN 1 AND 7)
);

CREATE INDEX idx_workout_plan_days_plan_day
    ON workout_plan_days (workout_plan_id, day_of_week);

CREATE TABLE workout_plan_exercises (
    id BIGINT NOT NULL AUTO_INCREMENT,
    workout_plan_day_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    source_template_exercise_id BIGINT NULL,
    order_index INT NOT NULL,
    sets INT NOT NULL,
    repetitions INT NOT NULL,
    rest_seconds INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    CONSTRAINT pk_workout_plan_exercises PRIMARY KEY (id),
    CONSTRAINT fk_workout_plan_exercises_day
        FOREIGN KEY (workout_plan_day_id) REFERENCES workout_plan_days (id),
    CONSTRAINT fk_workout_plan_exercises_exercise
        FOREIGN KEY (exercise_id) REFERENCES exercises (id),
    CONSTRAINT uk_workout_plan_exercises_order
        UNIQUE (workout_plan_day_id, order_index),
    CONSTRAINT uk_workout_plan_exercises_exercise
        UNIQUE (workout_plan_day_id, exercise_id),
    CONSTRAINT chk_workout_plan_exercises_order
        CHECK (order_index >= 1),
    CONSTRAINT chk_workout_plan_exercises_sets
        CHECK (sets >= 1),
    CONSTRAINT chk_workout_plan_exercises_repetitions
        CHECK (repetitions >= 1),
    CONSTRAINT chk_workout_plan_exercises_rest
        CHECK (rest_seconds >= 0)
);

CREATE INDEX idx_workout_plan_exercises_day
    ON workout_plan_exercises (workout_plan_day_id, order_index);

CREATE INDEX idx_workout_plan_exercises_exercise
    ON workout_plan_exercises (exercise_id);
