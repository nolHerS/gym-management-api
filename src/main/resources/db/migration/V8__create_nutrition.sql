-- =====================================================
-- V8 - Create food catalog and client nutrition plans
-- =====================================================

CREATE TABLE foods (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    calories DECIMAL(10,2) NULL,
    protein DECIMAL(10,2) NULL,
    carbohydrates DECIMAL(10,2) NULL,
    fats DECIMAL(10,2) NULL,
    serving_size DECIMAL(10,2) NULL,
    serving_unit VARCHAR(30) NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    CONSTRAINT pk_foods PRIMARY KEY (id),
    CONSTRAINT uk_foods_name UNIQUE (name),
    CONSTRAINT chk_foods_calories CHECK (calories IS NULL OR calories >= 0),
    CONSTRAINT chk_foods_protein CHECK (protein IS NULL OR protein >= 0),
    CONSTRAINT chk_foods_carbohydrates CHECK (carbohydrates IS NULL OR carbohydrates >= 0),
    CONSTRAINT chk_foods_fats CHECK (fats IS NULL OR fats >= 0),
    CONSTRAINT chk_foods_serving_size CHECK (serving_size IS NULL OR serving_size >= 0)
);

CREATE INDEX idx_foods_active_name ON foods (active, name);

CREATE TABLE nutrition_plans (
    id BIGINT NOT NULL AUTO_INCREMENT,
    client_id BIGINT NOT NULL,
    trainer_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    CONSTRAINT pk_nutrition_plans PRIMARY KEY (id),
    CONSTRAINT fk_nutrition_plans_client FOREIGN KEY (client_id) REFERENCES users (id),
    CONSTRAINT fk_nutrition_plans_trainer FOREIGN KEY (trainer_id) REFERENCES users (id),
    CONSTRAINT chk_nutrition_plans_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_nutrition_plans_client ON nutrition_plans (client_id);
CREATE INDEX idx_nutrition_plans_trainer ON nutrition_plans (trainer_id);
CREATE INDEX idx_nutrition_plans_client_status
    ON nutrition_plans (client_id, status);
CREATE INDEX idx_nutrition_plans_client_dates
    ON nutrition_plans (client_id, start_date, end_date);

CREATE TABLE nutrition_plan_meals (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nutrition_plan_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    order_index INT NOT NULL,
    description TEXT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    CONSTRAINT pk_nutrition_plan_meals PRIMARY KEY (id),
    CONSTRAINT fk_nutrition_plan_meals_plan
        FOREIGN KEY (nutrition_plan_id) REFERENCES nutrition_plans (id),
    CONSTRAINT uk_nutrition_plan_meals_order
        UNIQUE (nutrition_plan_id, order_index),
    CONSTRAINT uk_nutrition_plan_meals_name
        UNIQUE (nutrition_plan_id, name),
    CONSTRAINT chk_nutrition_plan_meals_order CHECK (order_index >= 1)
);

CREATE INDEX idx_nutrition_plan_meals_plan
    ON nutrition_plan_meals (nutrition_plan_id, order_index);

CREATE TABLE nutrition_plan_foods (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nutrition_plan_meal_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit VARCHAR(30) NULL,
    order_index INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    CONSTRAINT pk_nutrition_plan_foods PRIMARY KEY (id),
    CONSTRAINT fk_nutrition_plan_foods_meal
        FOREIGN KEY (nutrition_plan_meal_id) REFERENCES nutrition_plan_meals (id),
    CONSTRAINT fk_nutrition_plan_foods_food
        FOREIGN KEY (food_id) REFERENCES foods (id),
    CONSTRAINT uk_nutrition_plan_foods_meal_food
        UNIQUE (nutrition_plan_meal_id, food_id),
    CONSTRAINT chk_nutrition_plan_foods_quantity CHECK (quantity > 0),
    CONSTRAINT uk_nutrition_plan_foods_meal_order
        UNIQUE (nutrition_plan_meal_id, order_index),
    CONSTRAINT chk_nutrition_plan_foods_order CHECK (order_index >= 1)
);

CREATE INDEX idx_nutrition_plan_foods_meal
    ON nutrition_plan_foods (nutrition_plan_meal_id);
CREATE INDEX idx_nutrition_plan_foods_food
    ON nutrition_plan_foods (food_id);
