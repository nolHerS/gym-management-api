-- =====================================================
-- V2 - Create exercises
-- =====================================================

CREATE TABLE exercises (

    -- Primary Key
    id BIGINT NOT NULL AUTO_INCREMENT,

    -- Exercise information
    name VARCHAR(150) NOT NULL,
    description TEXT,

    -- Category relationship
    category_id BIGINT NOT NULL,

    -- Soft Delete
    active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit fields
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    -- Constraints
    CONSTRAINT pk_exercises
        PRIMARY KEY (id),

    CONSTRAINT fk_exercises_category
        FOREIGN KEY (category_id)
        REFERENCES exercise_categories (id),

    CONSTRAINT uk_exercises_name
        UNIQUE (name)
);

-- =====================================================
-- Indexes
-- =====================================================

CREATE INDEX idx_exercises_category_id
    ON exercises (category_id);