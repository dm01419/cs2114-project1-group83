<<<<<<< HEAD
# cs2114-project1-group83
 Workout_Planner
=======
# Workout Planner

A small Java MVP for generating workout routines from a user's fitness goal, workout duration, available equipment, and physical limitations.

## Requirements

- Java 17 or newer
- JUnit 5 for running the tests

## Compile and run

From this folder, run:

```bash
javac Main.java
java Main
```

The sample program creates a strength-focused Chest workout and prints the exercises that are valid for the profile.

## Run the JUnit tests

Maven downloads the JUnit 5 dependency automatically. Run:

```bash
../apache-maven-3.9.8/bin/mvn test
```

If Maven is already installed on your computer, use `mvn test` instead.

## MVP features

- Generates a workout for a selected muscle group
- Rejects plans when no fitness goal is selected
- Checks required equipment
- Rejects exercises that conflict with physical limitations
- Adds, removes, and replaces exercises
- Limits the recommended number of exercises based on workout duration

## Project structure

- `Main.java`: model classes, `WorkoutPlanner`, and a runnable demo
- `WorkoutPlannerTest.java`: JUnit tests for normal and bad-input cases
- `Npng_System_Diagram_Clear.drawio`: system diagram to upload with the repository
>>>>>>> f4e0c81 (Initial commit)
