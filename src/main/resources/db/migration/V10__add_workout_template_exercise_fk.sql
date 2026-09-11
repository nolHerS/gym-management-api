ALTER TABLE workout_plan_exercises
    ADD CONSTRAINT fk_workout_plan_exercises_template_exercise
        FOREIGN KEY (source_template_exercise_id)
        REFERENCES workout_template_exercises (id);
