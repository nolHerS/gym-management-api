-- =====================================================
-- V901 - Local workout plan development data
-- This migration is loaded only by the local Flyway profile.
-- =====================================================

SELECT id
INTO @workout_trainer_id
FROM users
WHERE email = 'android.trainer@gym.local'
  AND role = 'TRAINER'
  AND active = TRUE;

SELECT id
INTO @workout_client_id
FROM users
WHERE email = 'android.client@gym.local'
  AND role = 'CLIENT'
  AND active = TRUE;

SELECT id
INTO @workout_template_id
FROM workout_templates
WHERE id = 1
  AND active = TRUE;

SELECT id
INTO @squat_id
FROM exercises
WHERE name = 'Sentadilla'
  AND active = TRUE;

SELECT id
INTO @bench_press_id
FROM exercises
WHERE name = 'Press Banca'
  AND active = TRUE;

SELECT id
INTO @barbell_row_id
FROM exercises
WHERE name = 'Remo con Barra'
  AND active = TRUE;

SELECT id
INTO @plank_id
FROM exercises
WHERE name = 'Plancha'
  AND active = TRUE;

SELECT wte.id
INTO @template_squat_id
FROM workout_template_exercises wte
JOIN exercises e ON e.id = wte.exercise_id
WHERE wte.workout_template_id = @workout_template_id
  AND e.name = 'Sentadilla';

SELECT wte.id
INTO @template_bench_press_id
FROM workout_template_exercises wte
JOIN exercises e ON e.id = wte.exercise_id
WHERE wte.workout_template_id = @workout_template_id
  AND e.name = 'Press Banca';

SELECT wte.id
INTO @template_barbell_row_id
FROM workout_template_exercises wte
JOIN exercises e ON e.id = wte.exercise_id
WHERE wte.workout_template_id = @workout_template_id
  AND e.name = 'Remo con Barra';

SELECT wte.id
INTO @template_plank_id
FROM workout_template_exercises wte
JOIN exercises e ON e.id = wte.exercise_id
WHERE wte.workout_template_id = @workout_template_id
  AND e.name = 'Plancha';

SELECT COUNT(*)
INTO @trainer_client_relationships
FROM trainer_clients
WHERE trainer_id = @workout_trainer_id
  AND client_id = @workout_client_id;

SELECT wp.id
INTO @existing_workout_plan_id
FROM workout_plans wp
WHERE wp.client_id = @workout_client_id
  AND wp.trainer_id = @workout_trainer_id
  AND wp.source_template_id = @workout_template_id
  AND wp.start_date = '2026-09-07'
  AND wp.end_date IS NULL
  AND wp.status = 'ACTIVE';

-- Required references are deliberately used in NOT NULL/FK columns below.
-- Missing users, template, exercises, or relationship therefore fail migration.
INSERT INTO workout_plans (
    client_id, trainer_id, source_template_id,
    start_date, end_date, status, created_at, updated_at
)
SELECT
    @workout_client_id,
    IF(@trainer_client_relationships = 1, @workout_trainer_id, NULL),
    @workout_template_id,
    '2026-09-07', NULL, 'ACTIVE',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE @existing_workout_plan_id IS NULL;

SET @workout_plan_id = IF(
    @existing_workout_plan_id IS NULL,
    LAST_INSERT_ID(),
    @existing_workout_plan_id
);

SET @monday_id = NULL;
INSERT INTO workout_plan_days (
    workout_plan_id, day_of_week, created_at, updated_at
)
SELECT
    @workout_plan_id, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE @existing_workout_plan_id IS NULL;
SET @monday_id = IF(@existing_workout_plan_id IS NULL, LAST_INSERT_ID(), NULL);

SET @wednesday_id = NULL;
INSERT INTO workout_plan_days (
    workout_plan_id, day_of_week, created_at, updated_at
)
SELECT
    @workout_plan_id, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE @existing_workout_plan_id IS NULL;
SET @wednesday_id = IF(
    @existing_workout_plan_id IS NULL,
    LAST_INSERT_ID(),
    NULL
);

SET @friday_id = NULL;
INSERT INTO workout_plan_days (
    workout_plan_id, day_of_week, created_at, updated_at
)
SELECT
    @workout_plan_id, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE @existing_workout_plan_id IS NULL;
SET @friday_id = IF(@existing_workout_plan_id IS NULL, LAST_INSERT_ID(), NULL);

INSERT INTO workout_plan_exercises (
    workout_plan_day_id, exercise_id, source_template_exercise_id,
    order_index, sets, repetitions, rest_seconds,
    created_at, updated_at
)
SELECT
    @monday_id, @squat_id, @template_squat_id,
    1, 4, 8, 120,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE @existing_workout_plan_id IS NULL
UNION ALL
SELECT
    @monday_id, @bench_press_id, @template_bench_press_id,
    2, 4, 8, 120,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE @existing_workout_plan_id IS NULL
UNION ALL
SELECT
    @wednesday_id, @barbell_row_id, @template_barbell_row_id,
    1, 4, 10, 90,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE @existing_workout_plan_id IS NULL
UNION ALL
SELECT
    @wednesday_id, @plank_id, @template_plank_id,
    2, 3, 45, 60,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE @existing_workout_plan_id IS NULL
UNION ALL
SELECT
    @friday_id, @squat_id, @template_squat_id,
    1, 4, 8, 120,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE @existing_workout_plan_id IS NULL
UNION ALL
SELECT
    @friday_id, @bench_press_id, @template_bench_press_id,
    2, 4, 8, 120,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM dual
WHERE @existing_workout_plan_id IS NULL;
