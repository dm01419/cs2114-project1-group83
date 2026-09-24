import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for WorkoutPlanner: a normal case and a bad-input case for each
 * method.
 */
class WorkoutPlannerTest {
    private WorkoutPlanner planner;
    private Profile profile;

    @BeforeEach
    void setUp() {
        planner = new WorkoutPlanner();
        profile = new Profile();
        profile.setFitnessGoal("Strength");
        profile.setWorkoutDuration(60);
        profile.addEquipment("Barbell");
        profile.addEquipment("Dumbbells");
    }

    @Test
    void generateWorkoutCreatesRoutineForValidProfile() {
        WorkoutRoutine routine = planner.generateWorkout(profile, "Chest");

        assertNotNull(routine);
        assertTrue(routine.getNumberOfExercises() > 0);
        for (Exercise exercise : routine.getExercises()) {
            assertEquals("Chest", exercise.getPrimaryMuscleGroup());
            assertTrue(exercise.canPerformWith(profile));
        }
    }

    @Test
    void generateWorkoutRejectsProfileWithoutGoal() {
        Profile noGoal = new Profile();
        assertNull(planner.generateWorkout(noGoal, "Chest"));
        assertNull(planner.generateWorkout(profile, " "));
        assertNull(planner.generateWorkout(null, "Chest"));
    }

    @Test
    void generateWorkoutRejectsUnknownMuscleGroup() {
        assertNull(planner.generateWorkout(profile, "Banana"));
        assertEquals("Chest Workout",
                planner.generateWorkout(profile, " chest ").getName());
    }

    @Test
    void normalizeMuscleGroupMatchesKnownGroups() {
        assertEquals("Shoulders", WorkoutPlanner.normalizeMuscleGroup("SHOULDERS"));
        assertNull(WorkoutPlanner.normalizeMuscleGroup("Banana"));
        assertNull(WorkoutPlanner.normalizeMuscleGroup(null));
        for (Exercise exercise : planner.getExerciseLibrary()) {
            assertEquals(exercise.getPrimaryMuscleGroup(),
                    WorkoutPlanner.normalizeMuscleGroup(exercise.getPrimaryMuscleGroup()));
        }
    }

    @Test
    void generateWorkoutAssignsSetsAndRepsFromGoal() {
        WorkoutRoutine routine = planner.generateWorkout(profile, "Legs");
        Exercise first = routine.getExercises().get(0);
        assertEquals(5, first.getSets());
        assertEquals(5, first.getReps());
    }

    @Test
    void generateWorkoutSkipsExercisesThatConflictWithInjuries() {
        profile.addLimit("Knee injury");
        WorkoutRoutine routine = planner.generateWorkout(profile, "Legs");
        assertFalse(routine.containsExercise("Barbell Squat"));
        assertFalse(routine.containsExercise("Dumbbell Lunges"));
        assertTrue(routine.containsExercise("Romanian Deadlift"));
    }

    @Test
    void generateWorkoutPutsPreferredExercisesFirst() {
        profile.addExercisePreference("Push Ups");
        WorkoutRoutine routine = planner.generateWorkout(profile, "Chest");
        assertEquals("Push Ups", routine.getExercises().get(0).getName());
    }

    @Test
    void generateWorkoutRespectsTimeLimit() {
        profile.setWorkoutDuration(30);
        WorkoutRoutine routine = planner.generateWorkout(profile, "Legs");
        assertEquals(3, routine.getNumberOfExercises());
    }

    @Test
    void addExerciseAcceptsValidExercise() {
        WorkoutRoutine routine = new WorkoutRoutine("Chest Workout");
        Exercise exercise = new Exercise("Bench Press", "Chest", null,
                "Barbell", 5, 5);

        assertTrue(planner.addExercise(routine, exercise, profile));
        assertTrue(routine.containsExercise("Bench Press"));
    }

    @Test
    void addExerciseRejectsUnavailableEquipment() {
        WorkoutRoutine routine = new WorkoutRoutine("Chest Workout");
        Exercise exercise = new Exercise("Cable Fly", "Chest", null,
                "Cable Machine", 3, 12);

        assertFalse(planner.addExercise(routine, exercise, profile));
        assertFalse(planner.addExercise(null, exercise, profile));
        assertFalse(planner.addExercise(routine, null, profile));
    }

    @Test
    void addExerciseRejectsTooManyForTime() {
        profile.setWorkoutDuration(30);
        WorkoutRoutine routine = new WorkoutRoutine("Chest Workout");
        for (int i = 1; i <= 3; i++) {
            assertTrue(planner.addExercise(routine,
                    new Exercise("Move " + i, "Chest", null, null, 3, 10),
                    profile));
        }
        assertTrue(planner.isRoutineFull(routine, profile));
        assertFalse(planner.addExercise(routine,
                new Exercise("Move 4", "Chest", null, null, 3, 10), profile));
    }

    @Test
    void removeExerciseRemovesExistingExercise() {
        WorkoutRoutine routine = planner.generateWorkout(profile, "Chest");

        assertTrue(planner.removeExercise(routine, "Push Ups"));
        assertFalse(routine.containsExercise("Push Ups"));
    }

    @Test
    void removeExerciseReturnsFalseForMissingExercise() {
        WorkoutRoutine routine = new WorkoutRoutine("Chest Workout");

        assertFalse(planner.removeExercise(routine, "Deadlift"));
        assertFalse(planner.removeExercise(null, "Deadlift"));
    }

    @Test
    void changeExerciseReplacesValidExercise() {
        WorkoutRoutine routine = new WorkoutRoutine("Leg Workout");
        planner.addExercise(routine,
                new Exercise("Squat", "Legs", null, "Barbell", 5, 5), profile);
        Exercise legPress = new Exercise("Leg Press", "Legs", null, null, 5, 5);

        assertTrue(planner.changeExercise(routine, "Squat", legPress, profile));
        assertTrue(routine.containsExercise("Leg Press"));
        assertFalse(routine.containsExercise("Squat"));
    }

    @Test
    void changeExerciseRejectsReplacementThatConflictsWithLimitations() {
        profile.addLimit("Knee injury");
        WorkoutRoutine routine = new WorkoutRoutine("Leg Workout");
        planner.addExercise(routine,
                new Exercise("Glute Bridge", "Legs", null, null, 3, 10), profile);
        Exercise lunges = planner.findExercise("Dumbbell Lunges", profile);

        assertFalse(planner.changeExercise(
                routine, "Glute Bridge", lunges, profile));
        assertTrue(routine.containsExercise("Glute Bridge"));
        assertFalse(planner.changeExercise(null, "Glute Bridge", lunges, profile));
    }

    @Test
    void isExerciseValidTrueWhenEquipmentAndNoConflict() {
        assertTrue(planner.isExerciseValid(
                planner.findExercise("Barbell Squat", profile), profile));
    }

    @Test
    void isExerciseValidFalseForConflictOrMissingEquipment() {
        profile.addLimit("knee");
        assertFalse(planner.isExerciseValid(
                planner.findExercise("Barbell Squat", profile), profile));
        assertFalse(planner.isExerciseValid(
                planner.findExercise("Pull Ups", profile), profile));
        assertFalse(planner.isExerciseValid(null, profile));
    }

    @Test
    void getSetsAndRepsDependsOnGoal() {
        assertArrayEquals(new int[] {5, 5}, planner.getSetsAndReps(profile));
        profile.setFitnessGoal("Muscle Gain");
        assertArrayEquals(new int[] {4, 10}, planner.getSetsAndReps(profile));
        profile.setFitnessGoal("Endurance");
        assertArrayEquals(new int[] {3, 15}, planner.getSetsAndReps(profile));
        profile.setFitnessGoal("General Fitness");
        assertArrayEquals(new int[] {3, 12}, planner.getSetsAndReps(profile));
        assertArrayEquals(new int[] {3, 12}, planner.getSetsAndReps(null));
    }

    @Test
    void getMaxExercisesDependsOnDuration() {
        profile.setWorkoutDuration(30);
        assertEquals(3, planner.getMaxExercises(profile));
        profile.setWorkoutDuration(60);
        assertEquals(5, planner.getMaxExercises(profile));
        profile.setWorkoutDuration(90);
        assertEquals(6, planner.getMaxExercises(profile));
        assertEquals(3, planner.getMaxExercises(null));
    }

    @Test
    void findExerciseReturnsCopyOrNull() {
        Exercise found = planner.findExercise("barbell squat", profile);
        assertEquals("Barbell Squat", found.getName());
        found.setSets(9);
        assertEquals(5, planner.findExercise("Barbell Squat", profile).getSets());
        assertNull(planner.findExercise("Moonwalk", profile));
        assertNull(planner.findExercise(null, profile));
        assertFalse(planner.getExerciseLibrary().isEmpty());
    }
}
