-- =====================================================
-- V3 - Create workout templates
-- =====================================================

CREATE TABLE workout_templates (

    -- Primary Key
    id BIGINT NOT NULL AUTO_INCREMENT,

    -- Workout template information
    name VARCHAR(150) NOT NULL,
    description TEXT,

    -- Soft Delete
    active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit fields
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    -- Constraints
    CONSTRAINT pk_workout_templates
        PRIMARY KEY (id),

    CONSTRAINT uk_workout_templates_name
        UNIQUE (name)
);