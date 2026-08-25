-- =====================================================
-- V6 - Create trainer clients
-- =====================================================

CREATE TABLE trainer_clients (

    -- Primary Key
    id BIGINT NOT NULL AUTO_INCREMENT,

    -- Relationships
    trainer_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,

    -- Audit fields
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    -- Constraints
    CONSTRAINT pk_trainer_clients
        PRIMARY KEY (id),

    CONSTRAINT fk_trainer_clients_trainer
        FOREIGN KEY (trainer_id)
        REFERENCES users (id),

    CONSTRAINT fk_trainer_clients_client
        FOREIGN KEY (client_id)
        REFERENCES users (id),

    CONSTRAINT uk_trainer_clients
        UNIQUE (trainer_id, client_id)
);

-- Indexes
CREATE INDEX idx_trainer_clients_trainer
    ON trainer_clients (trainer_id);

CREATE INDEX idx_trainer_clients_client
    ON trainer_clients (client_id);