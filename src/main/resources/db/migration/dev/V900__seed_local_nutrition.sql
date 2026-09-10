-- =====================================================
-- V900 - Local nutrition development data
-- This migration is loaded only by the local Flyway profile.
-- =====================================================

INSERT INTO users (
    first_name, last_name, email, password, role, active,
    created_at, updated_at
)
SELECT
    'Android', 'Trainer', 'android.trainer@gym.local',
    '$2a$10$3BLbByT.Lss8qVUtFV6LR.s0kCoWlWh.QeRnjc6aO5n.oIu.TE7Um',
    'TRAINER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'android.trainer@gym.local'
);

INSERT INTO users (
    first_name, last_name, email, password, role, active,
    created_at, updated_at
)
SELECT
    'Android', 'Client', 'android.client@gym.local',
    '$2a$10$3BLbByT.Lss8qVUtFV6LR.s0kCoWlWh.QeRnjc6aO5n.oIu.TE7Um',
    'CLIENT', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'android.client@gym.local'
);

SELECT id
INTO @android_trainer_id
FROM users
WHERE email = 'android.trainer@gym.local'
  AND role = 'TRAINER'
  AND active = TRUE;

SELECT id
INTO @android_client_id
FROM users
WHERE email = 'android.client@gym.local'
  AND role = 'CLIENT'
  AND active = TRUE;

-- These inserts deliberately fail if either required development user is
-- missing, has another role, or is inactive.
INSERT INTO trainer_clients (
    trainer_id, client_id, created_at, updated_at
)
SELECT
    @android_trainer_id, @android_client_id,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM trainer_clients
    WHERE trainer_id = @android_trainer_id
      AND client_id = @android_client_id
);

INSERT INTO trainer_clients (
    trainer_id, client_id, created_at, updated_at
)
SELECT
    @android_trainer_id, u.id,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u
WHERE u.email IN (
    'laura.client@gym.local',
    'mario.client@gym.local'
)
  AND u.role = 'CLIENT'
  AND NOT EXISTS (
      SELECT 1
      FROM trainer_clients tc
      WHERE tc.trainer_id = @android_trainer_id
        AND tc.client_id = u.id
  );

INSERT INTO foods (
    name, description, calories, protein, carbohydrates, fats,
    serving_size, serving_unit, active, created_at, updated_at
)
SELECT
    'Avena', 'Avena integral', 389.00, 16.90, 66.30, 6.90,
    100.00, 'g', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE name = 'Avena');

INSERT INTO foods (
    name, description, calories, protein, carbohydrates, fats,
    serving_size, serving_unit, active, created_at, updated_at
)
SELECT
    'Arroz', 'Arroz blanco cocido', 360.00, 7.10, 79.00, 0.70,
    100.00, 'g', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE name = 'Arroz');

INSERT INTO foods (
    name, description, calories, protein, carbohydrates, fats,
    serving_size, serving_unit, active, created_at, updated_at
)
SELECT
    'Pechuga de pollo', 'Pechuga de pollo a la plancha',
    165.00, 31.00, 0.00, 3.60,
    100.00, 'g', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE name = 'Pechuga de pollo');

INSERT INTO foods (
    name, description, calories, protein, carbohydrates, fats,
    serving_size, serving_unit, active, created_at, updated_at
)
SELECT
    'Plátano', 'Plátano natural', 89.00, 1.10, 22.80, 0.30,
    100.00, 'g', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE name = 'Plátano');

INSERT INTO foods (
    name, description, calories, protein, carbohydrates, fats,
    serving_size, serving_unit, active, created_at, updated_at
)
SELECT
    'Huevo', 'Huevo entero', 155.00, 13.00, 1.10, 11.00,
    100.00, 'g', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE name = 'Huevo');

INSERT INTO foods (
    name, description, calories, protein, carbohydrates, fats,
    serving_size, serving_unit, active, created_at, updated_at
)
SELECT
    'Yogur natural', 'Yogur natural entero', 61.00, 3.50, 4.70, 3.30,
    100.00, 'g', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE name = 'Yogur natural');

SELECT id INTO @avena_id
FROM foods WHERE name = 'Avena' AND active = TRUE;
SELECT id INTO @arroz_id
FROM foods WHERE name = 'Arroz' AND active = TRUE;
SELECT id INTO @pollo_id
FROM foods WHERE name = 'Pechuga de pollo' AND active = TRUE;
SELECT id INTO @platano_id
FROM foods WHERE name = 'Plátano' AND active = TRUE;
SELECT id INTO @huevo_id
FROM foods WHERE name = 'Huevo' AND active = TRUE;
SELECT id INTO @yogur_id
FROM foods WHERE name = 'Yogur natural' AND active = TRUE;

INSERT INTO nutrition_plans (
    client_id, trainer_id, name, description,
    start_date, end_date, status, created_at, updated_at
)
VALUES (
    @android_client_id, @android_trainer_id,
    'Plan definición', 'Plan de definición para pruebas Android',
    '2026-09-07', NULL, 'ACTIVE',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);
SET @definition_plan_id = LAST_INSERT_ID();

INSERT INTO nutrition_plan_meals (
    nutrition_plan_id, name, order_index, description,
    created_at, updated_at
)
VALUES (
    @definition_plan_id, 'Desayuno', 1, 'Desayuno completo',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);
SET @definition_breakfast_id = LAST_INSERT_ID();

INSERT INTO nutrition_plan_foods (
    nutrition_plan_meal_id, food_id, quantity, unit, order_index,
    created_at, updated_at
)
VALUES
    (@definition_breakfast_id, @avena_id, 80.00, 'g', 1,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (@definition_breakfast_id, @platano_id, 120.00, 'g', 2,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (@definition_breakfast_id, @yogur_id, 200.00, 'g', 3,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO nutrition_plan_meals (
    nutrition_plan_id, name, order_index, description,
    created_at, updated_at
)
VALUES (
    @definition_plan_id, 'Comida', 2, 'Comida principal',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);
SET @definition_lunch_id = LAST_INSERT_ID();

INSERT INTO nutrition_plan_foods (
    nutrition_plan_meal_id, food_id, quantity, unit, order_index,
    created_at, updated_at
)
VALUES
    (@definition_lunch_id, @arroz_id, 100.00, 'g', 1,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (@definition_lunch_id, @pollo_id, 200.00, 'g', 2,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO nutrition_plan_meals (
    nutrition_plan_id, name, order_index, description,
    created_at, updated_at
)
VALUES (
    @definition_plan_id, 'Cena', 3, 'Cena ligera',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);
SET @definition_dinner_id = LAST_INSERT_ID();

INSERT INTO nutrition_plan_foods (
    nutrition_plan_meal_id, food_id, quantity, unit, order_index,
    created_at, updated_at
)
VALUES
    (@definition_dinner_id, @huevo_id, 150.00, 'g', 1,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (@definition_dinner_id, @yogur_id, 150.00, 'g', 2,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO nutrition_plans (
    client_id, trainer_id, name, description,
    start_date, end_date, status, created_at, updated_at
)
VALUES (
    @android_client_id, @android_trainer_id,
    'Plan mantenimiento', 'Plan completado para probar histórico',
    '2026-07-01', '2026-08-31', 'COMPLETED',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);
SET @maintenance_plan_id = LAST_INSERT_ID();

INSERT INTO nutrition_plan_meals (
    nutrition_plan_id, name, order_index, description,
    created_at, updated_at
)
VALUES (
    @maintenance_plan_id, 'Desayuno', 1, 'Desayuno de mantenimiento',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);
SET @maintenance_breakfast_id = LAST_INSERT_ID();

INSERT INTO nutrition_plan_foods (
    nutrition_plan_meal_id, food_id, quantity, unit, order_index,
    created_at, updated_at
)
VALUES
    (@maintenance_breakfast_id, @avena_id, 60.00, 'g', 1,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (@maintenance_breakfast_id, @yogur_id, 200.00, 'g', 2,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO nutrition_plans (
    client_id, trainer_id, name, description,
    start_date, end_date, status, created_at, updated_at
)
VALUES (
    @android_client_id, @android_trainer_id,
    'Plan anterior', 'Plan inactivo para pruebas',
    '2026-05-01', '2026-06-30', 'INACTIVE',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);
SET @previous_plan_id = LAST_INSERT_ID();

INSERT INTO nutrition_plan_meals (
    nutrition_plan_id, name, order_index, description,
    created_at, updated_at
)
VALUES (
    @previous_plan_id, 'Desayuno', 1, 'Desayuno anterior',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);
SET @previous_breakfast_id = LAST_INSERT_ID();

INSERT INTO nutrition_plan_foods (
    nutrition_plan_meal_id, food_id, quantity, unit, order_index,
    created_at, updated_at
)
VALUES (
    @previous_breakfast_id, @huevo_id, 100.00, 'g', 1,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);
