import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

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

    @Test
    void routineRejectsBlankExerciseNames() {
        Main.WorkoutRoutine routine = new Main.WorkoutRoutine("Chest Workout");

        assertFalse(routine.addExercise(
                new Main.Exercise(" ", "Chest", "none", "")));
        assertEquals(0, routine.getNumberOfExercises());
    }

    @Test
    void scheduleAddsAndFindsWorkoutDay() {
        Main.WorkoutSchedule schedule = new Main.WorkoutSchedule();

        assertTrue(schedule.addWorkoutDay("Monday", "Chest"));
        assertTrue(schedule.isWorkoutDay("monday"));
        assertEquals("Chest", schedule.getMuscleGroup("MONDAY"));
    }

    @Test
    void scheduleRejectsInvalidOrDuplicateDays() {
        Main.WorkoutSchedule schedule = new Main.WorkoutSchedule();

        assertFalse(schedule.addWorkoutDay("Funday", "Chest"));
        assertTrue(schedule.addWorkoutDay("Monday", "Chest"));
        assertFalse(schedule.addWorkoutDay("Monday", "Back"));
    }

    @Test
    void scheduleMovesWorkoutAndItsRoutine() {
        Main.WorkoutSchedule schedule = new Main.WorkoutSchedule();
        Main.WorkoutRoutine routine = new Main.WorkoutRoutine("Chest Workout");

        schedule.addWorkoutDay("Monday", "Chest");
        schedule.assignRoutine("Monday", routine);

        assertTrue(schedule.changeWorkoutDay("Monday", "Tuesday"));
        assertFalse(schedule.isWorkoutDay("Monday"));
        assertEquals("Chest", schedule.getMuscleGroup("Tuesday"));
        assertEquals(routine, schedule.getRoutine("Tuesday"));
    }

    @Test
    void scheduleDoesNotAssignRoutineToRestDay() {
        Main.WorkoutSchedule schedule = new Main.WorkoutSchedule();

        assertFalse(schedule.assignRoutine(
                "Sunday", new Main.WorkoutRoutine("Rest Day")));
        assertNull(schedule.getRoutine("Sunday"));
    }

    @Test
    void removingWorkoutDayAlsoRemovesItsRoutine() {
        Main.WorkoutSchedule schedule = new Main.WorkoutSchedule();
        Main.WorkoutRoutine routine = new Main.WorkoutRoutine("Chest Workout");

        schedule.addWorkoutDay("Monday", "Chest");
        schedule.assignRoutine("Monday", routine);

        assertTrue(schedule.removeWorkoutDay("Monday"));
        assertFalse(schedule.isWorkoutDay("Monday"));
        assertNull(schedule.getRoutine("Monday"));
    }

    @Test
    void exerciseStoresSetsAndReps() {
        Main.Exercise exercise = new Main.Exercise(
                "Bench Press", "Chest", "barbell", "shoulder", 4, 8);

        assertEquals(4, exercise.getSets());
        assertEquals(8, exercise.getReps());
    }

    @Test
    void consoleUiRepromptsForInvalidWorkoutDayCount() {
        Scanner input = new Scanner(new ByteArrayInputStream(
                "four\n0\n3\n".getBytes(StandardCharsets.UTF_8)));
        Main.ConsoleUI ui = new Main.ConsoleUI(input);

        assertEquals(3, ui.readWorkoutDays());
    }

    @Test
    void consoleUiRepromptsForInvalidDay() {
        Scanner input = new Scanner(new ByteArrayInputStream(
                "Funday\nMonday\n".getBytes(StandardCharsets.UTF_8)));
        Main.ConsoleUI ui = new Main.ConsoleUI(input);

        assertEquals("monday", ui.readWorkoutDay());
    }

    @Test
    void plannerCanUseAValidCustomExercise() {
        Main.Profile profile = validProfile();
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.Exercise customExercise = new Main.Exercise(
                "Resistance Band Press", "Chest", "resistance band", "",
                4, 12);

        assertTrue(planner.addCustomExercise(customExercise));
        profile.addEquipment("resistance band");
        Main.WorkoutRoutine routine = planner.generateWorkout(profile, "Chest");

        assertTrue(routine.containsExercise("Resistance Band Press"));
    }

    @Test
    void plannerRejectsDuplicateCustomExercise() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.Exercise customExercise = new Main.Exercise(
                "My Press", "Chest", "none", "");

        assertTrue(planner.addCustomExercise(customExercise));
        assertFalse(planner.addCustomExercise(customExercise));
    }

    @Test
    void profileStoresLimitationsAndExercisePreferences() {
        Main.Profile profile = new Main.Profile();

        profile.addLimit("Knee");
        profile.addExercisePreference("Push Ups");

        assertTrue(profile.hasLimitation("knee"));
        assertTrue(profile.hasExercisePreference("push ups"));
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
