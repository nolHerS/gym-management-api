-- =====================================================
-- V4 - Create workout template exercises
-- =====================================================

CREATE TABLE workout_template_exercises (

    -- Primary Key
    id BIGINT NOT NULL AUTO_INCREMENT,

    -- Relationships
    workout_template_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,

    -- Exercise configuration
    order_index INT NOT NULL,
    sets INT NOT NULL,
    repetitions INT NOT NULL,
    rest_seconds INT NOT NULL,

    -- Audit fields
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    -- Constraints
    CONSTRAINT pk_workout_template_exercises
        PRIMARY KEY (id),

    CONSTRAINT fk_workout_template_exercises_template
        FOREIGN KEY (workout_template_id)
        REFERENCES workout_templates (id),

    CONSTRAINT fk_workout_template_exercises_exercise
        FOREIGN KEY (exercise_id)
        REFERENCES exercises (id),

    CONSTRAINT uk_workout_template_exercises_order
        UNIQUE (workout_template_id, order_index)
);

CREATE INDEX idx_workout_template_exercises_template
    ON workout_template_exercises (workout_template_id);

CREATE INDEX idx_workout_template_exercises_exercise
    ON workout_template_exercises (exercise_id);