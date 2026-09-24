import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WorkoutPlannerTest {
    @Test
    void generateWorkoutCreatesRoutineForValidProfile() {
        Main.Profile profile = validProfile();
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();

        Main.WorkoutRoutine routine = planner.generateWorkout(profile, "Chest");

        assertNotNull(routine);
        assertTrue(routine.getNumberOfExercises() > 0);
    }

    @Test
    void generateWorkoutRejectsProfileWithoutGoal() {
        Main.Profile profile = new Main.Profile();
        profile.setWorkoutDuration(60);
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();

        assertNull(planner.generateWorkout(profile, "Chest"));
    }

    @Test
    void addExerciseAcceptsExerciseWithAvailableEquipment() {
        Main.Profile profile = validProfile();
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.WorkoutRoutine routine = new Main.WorkoutRoutine("Chest Workout");
        Main.Exercise exercise = new Main.Exercise(
                "Bench Press", "Chest", "barbell", "shoulder");

        assertTrue(planner.addExercise(routine, exercise, profile));
        assertTrue(routine.containsExercise("Bench Press"));
    }

    @Test
    void addExerciseRejectsUnavailableEquipment() {
        Main.Profile profile = validProfile();
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.WorkoutRoutine routine = new Main.WorkoutRoutine("Chest Workout");
        Main.Exercise exercise = new Main.Exercise(
                "Cable Fly", "Chest", "cable machine", "shoulder");

        assertFalse(planner.addExercise(routine, exercise, profile));
    }

    @Test
    void removeExerciseRemovesExistingExercise() {
        Main.Profile profile = validProfile();
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.WorkoutRoutine routine = planner.generateWorkout(profile, "Chest");

        assertTrue(planner.removeExercise(routine, "Push Ups"));
        assertFalse(routine.containsExercise("Push Ups"));
    }

    @Test
    void removeExerciseReturnsFalseForMissingExercise() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.WorkoutRoutine routine = new Main.WorkoutRoutine("Chest Workout");

        assertFalse(planner.removeExercise(routine, "Deadlift"));
    }

    @Test
    void changeExerciseReplacesValidExercise() {
        Main.Profile profile = validProfile();
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.WorkoutRoutine routine = planner.generateWorkout(profile, "Chest");
        Main.Exercise replacement = new Main.Exercise(
                "Incline Press", "Chest", "barbell", "shoulder");

        assertTrue(planner.changeExercise(
                routine, "Push Ups", replacement, profile));
        assertTrue(routine.containsExercise("Incline Press"));
        assertFalse(routine.containsExercise("Push Ups"));
    }

    @Test
    void changeExerciseRejectsInvalidReplacement() {
        Main.Profile profile = validProfile();
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.WorkoutRoutine routine = planner.generateWorkout(profile, "Chest");
        Main.Exercise replacement = new Main.Exercise(
                "Cable Fly", "Chest", "cable machine", "shoulder");

        assertFalse(planner.changeExercise(
                routine, "Push Ups", replacement, profile));
    }

    @Test
    void isExerciseValidReturnsFalseForConflictingLimitation() {
        Main.Profile profile = validProfile();
        profile.addLimit("knee");
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.Exercise exercise = new Main.Exercise(
                "Barbell Squat", "Legs", "barbell", "knee");

        assertFalse(planner.isExerciseValid(exercise, profile));
    }

    @Test
    void nullInputsAreHandledSafely() {
        Main.Profile profile = validProfile();
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();

        assertFalse(profile.hasEquipment(null));
        assertFalse(profile.hasLimitation(null));
        assertFalse(planner.isExerciseValid(null, profile));
        assertFalse(planner.isExerciseValid(
                new Main.Exercise("Push Ups", "Chest", "none", ""), null));
    }

    private Main.Profile validProfile() {
        Main.Profile profile = new Main.Profile();
        profile.setFitnessGoal("Strength");
        profile.setWorkoutDuration(60);
        profile.addEquipment("barbell");
        profile.addEquipment("dumbbells");
        return profile;
    }
}
