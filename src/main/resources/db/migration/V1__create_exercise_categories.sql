-- =====================================================
-- V1 - Create exercise categories
-- =====================================================

CREATE TABLE exercise_categories (

    -- Primary Key
    id BIGINT NOT NULL AUTO_INCREMENT,

    -- Category information
    name VARCHAR(100) NOT NULL,

    -- Soft Delete
    active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit fields
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    -- Constraints
    CONSTRAINT pk_exercise_categories
        PRIMARY KEY (id),

    CONSTRAINT uk_exercise_categories_name
        UNIQUE (name)
);