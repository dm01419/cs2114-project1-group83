import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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
        profile.addExercisePreference("Resistance Band Press");
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

    @Test
    void preferredExerciseIsGeneratedFirst() {
        Main.Profile profile = validProfile();
        profile.addExercisePreference("Push Ups");
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();

        Main.WorkoutRoutine routine = planner.generateWorkout(profile, "Chest");

        assertEquals("Push Ups", routine.getExercises().get(0).getName());
    }

    @Test
    void consoleShowsInjuriesAndCurrentPlanAfterEditingAndViewing() {
        PrintStream originalOut = System.out;
        try {
            for (String injury : new String[] {"shoulder", ""}) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
                String input = "Strength\n45\n1\nMonday\nbarbell\n" + injury
                        + "\nPush Ups\n\nview\nedit\nMonday\nremove\nPush Ups\nview\n\n";
                new Main.ConsoleUI(new Scanner(input)).start();

                String[] views = output.toString(StandardCharsets.UTF_8).split("Current plan");
                assertEquals(5, views.length);
                for (int index = 1; index < views.length; index++) {
                    assertTrue(views[index].contains("Limitations or injuries: "
                            + (injury.isEmpty() ? "None" : injury)));
                    assertTrue(views[index].contains("Monday: Full Body"));
                    assertEquals(injury.isEmpty() && index <= 2, views[index].contains("- Push Ups"));
                    if (!injury.isEmpty()) {
                        assertFalse(views[index].contains("- Barbell Bench Press"));
                    }
                }
            }
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void goalsChangePrescriptionsOrderAndTimeBudgetWithoutChangingCustomExercises() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        String[] goals = {"Strength", "Muscle Gain", "Endurance", "Weight Loss"};
        int[] sets = {3, 3, 2, 3};
        int[] reps = {6, 10, 16, 12};
        int[] rest = {180, 120, 60, 90};
        String[] first = {"Barbell Bench Press", "Dumbbell Bench Press", "Push Ups", "Push Ups"};
        Main.Exercise custom = new Main.Exercise("Custom Press", "Chest", "none", "", 4, 8);
        planner.addCustomExercise(custom);
        for (int index = 0; index < goals.length; index++) {
            Main.Profile profile = validProfile();
            profile.setFitnessGoal(goals[index].toLowerCase());
            Main.WorkoutRoutine routine = planner.generateWorkout(profile, "Chest");
            assertEquals(first[index], routine.getExercises().get(0).getName());
            assertEquals(sets[index], routine.getExercises().get(0).getSets());
            assertEquals(reps[index], routine.getExercise("Push Ups").getReps());
            assertTrue(routine.toString().contains("rest " + rest[index] + " sec"));
            profile.addExercisePreference("Custom Press");
            Main.WorkoutRoutine customRoutine = planner.generateWorkout(profile, "Chest");
            assertEquals(4, customRoutine.getExercise("Custom Press").getSets());
            assertEquals(8, customRoutine.getExercise("Custom Press").getReps());
            profile.addLimit("shoulder");
            routine = planner.generateWorkout(profile, "Chest");
            assertFalse(routine.containsExercise("Barbell Bench Press"));
            profile.setWorkoutDuration(5);
            assertEquals(0, planner.generateWorkout(profile, "Chest").getNumberOfExercises());
        }
        Main.Profile profile = validProfile();
        profile.setWorkoutDuration(25);
        assertEquals(2, planner.generateWorkout(profile, "Chest").getNumberOfExercises());
        profile.setFitnessGoal("Endurance");
        assertEquals(3, planner.generateWorkout(profile, "Chest").getNumberOfExercises());
        profile.setFitnessGoal("unknown");
        assertNull(planner.generateWorkout(profile, "Chest"));
        assertEquals("Muscle Gain", new Main.ConsoleUI(
                new Scanner("unknown\n muscle gain \n")).readFitnessGoal());
    }

    @Test
    void expandedCatalogSupportsBodyweightAndFiltersEveryRecordedLimitation() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.Profile profile = new Main.Profile();
        profile.setFitnessGoal("Strength");
        profile.setWorkoutDuration(90);
        for (String group : new String[] {"Chest", "Legs", "Back"}) {
            Main.WorkoutRoutine routine = planner.generateWorkout(profile, group);
            assertTrue(routine.getNumberOfExercises() >= 4);
            for (Main.Exercise exercise : routine.getExercises()) {
                assertEquals("none", exercise.getEquipment());
            }
        }
        profile.addLimit("knee");
        Main.WorkoutRoutine legs = planner.generateWorkout(profile, "Legs");
        assertFalse(legs.containsExercise("Bodyweight Squat"));
        assertTrue(legs.containsExercise("Glute Bridge"));
        profile.addLimit("lower back");
        assertFalse(planner.generateWorkout(profile, "Legs").containsExercise("Glute Bridge"));
        Main.Exercise row = new Main.Exercise("Band Row", "Back", "resistance band", "shoulder, lower back");
        assertFalse(planner.isExerciseValid(row, profile));
        profile.addEquipment("resistance band");
        assertFalse(planner.isExerciseValid(row, profile));
        Main.Profile equipped = validProfile();
        equipped.setWorkoutDuration(90);
        assertEquals(6, planner.generateWorkout(equipped, "Legs").getNumberOfExercises());
    }

    @Test
    void shoulderAndArmRoutinesSupportAliasesEquipmentAndLimitations() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.Profile profile = validProfile();
        profile.setWorkoutDuration(90);
        for (String group : new String[] {" shoulder ", "SHOULDERS", "arm", "Arms"}) {
            Main.WorkoutRoutine routine = planner.generateWorkout(profile, group);
            assertEquals(6, routine.getNumberOfExercises());
            for (Main.Exercise exercise : routine.getExercises()) {
                assertEquals(Main.normalizeMuscleGroup(group), exercise.getMuscleGroup());
            }
        }
        profile.addLimit("shoulder");
        assertEquals(0, planner.generateWorkout(profile, "shoulder").getNumberOfExercises());
        Main.WorkoutRoutine arms = planner.generateWorkout(profile, "arm");
        assertTrue(arms.containsExercise("Dumbbell Biceps Curl"));
        assertFalse(arms.containsExercise("Dumbbell Overhead Triceps Extension"));
        profile.addLimit("elbow");
        assertEquals(0, planner.generateWorkout(profile, "arm").getNumberOfExercises());
        Main.Profile bodyweight = new Main.Profile();
        bodyweight.setFitnessGoal("Muscle Gain");
        bodyweight.setWorkoutDuration(30);
        assertTrue(planner.generateWorkout(bodyweight, "arm").containsExercise("Diamond Push Ups"));
        assertFalse(planner.generateWorkout(bodyweight, "arm").containsExercise("Dumbbell Biceps Curl"));
        assertEquals("Shoulders", new Main.Exercise("Custom", "shoulder", "none", "").getMuscleGroup());
        Main.WorkoutSchedule schedule = new Main.WorkoutSchedule();
        schedule.addWorkoutDay("Monday", "shoulder");
        assertEquals("Shoulders", schedule.getMuscleGroup("Monday"));
    }

    @Test
    void schedulesUseDayCountSplitsAndLongSessionsHaveMoreSets() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String[][] splits = {{"Full Body"}, {"Upper", "Legs"}, {"Push", "Pull", "Legs"},
                {"Upper", "Legs", "Upper", "Legs"}, {"Chest", "Back", "Legs", "Shoulders", "Arms"},
                {"Push", "Pull", "Legs", "Push", "Pull", "Legs"},
                {"Push", "Pull", "Legs", "Push", "Pull", "Legs", "Recovery"}};
        for (String[] split : splits) {
            Main.Profile profile = validProfile();
            profile.setWorkoutDuration(90);
            for (int index = 0; index < split.length; index++) {
                profile.addPreferredWorkoutDay(days[index]);
            }
            Main.WorkoutSchedule schedule = planner.generateSchedule(profile);
            for (int index = 0; index < split.length; index++) {
                assertEquals(split[index], schedule.getMuscleGroup(days[index]));
                Main.WorkoutRoutine routine = schedule.getRoutine(days[index]);
                if (split[index].equals("Recovery")) {
                    assertEquals(0, routine.getNumberOfExercises());
                } else {
                    assertEquals(6, routine.getNumberOfExercises());
                    assertEquals(22, routine.getExercises().stream().mapToInt(Main.Exercise::getSets).sum());
                }
            }
            if (split.length < 7) {
                assertNull(schedule.getRoutine(days[split.length]));
            }
        }
        Main.Profile profile = validProfile();
        Main.WorkoutRoutine push = planner.generateWorkout(profile, "Push");
        assertTrue(push.containsExercise("Dumbbell Triceps Kickback"));
        assertFalse(push.getExercises().stream().anyMatch(e -> e.getName().contains("Curl")));
        Main.WorkoutRoutine pull = planner.generateWorkout(profile, "Pull");
        assertTrue(pull.containsExercise("Barbell Biceps Curl"));
        assertFalse(pull.getExercises().stream().anyMatch(e -> e.getName().contains("Triceps")));
        profile.addLimit("shoulder");
        for (Main.Exercise exercise : planner.generateWorkout(profile, "Push").getExercises()) {
            assertTrue(planner.isExerciseValid(exercise, profile));
        }
    }

    @Test
    void oneAutomaticDayBuildsFullBodyWithinOneTimeBudget() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.Profile profile = validProfile();
        profile.addPreferredWorkoutDay("Wednesday");
        Main.WorkoutSchedule schedule = planner.generateSchedule(profile);
        assertEquals("Full Body", schedule.getMuscleGroup("Wednesday"));
        assertNull(schedule.getRoutine("Monday"));
        Main.WorkoutRoutine routine = schedule.getRoutine("Wednesday");
        assertEquals(5, routine.getNumberOfExercises());
        String[] groups = {"Legs", "Chest", "Back", "Shoulders", "Arms"};
        for (int index = 0; index < groups.length; index++) {
            assertEquals(groups[index], routine.getExercises().get(index).getMuscleGroup());
            assertEquals(6, routine.getExercises().get(index).getReps());
        }
        profile.setWorkoutDuration(25);
        assertEquals(2, planner.generateWorkout(profile, "full-body").getNumberOfExercises());
        profile.addLimit("shoulder");
        routine = planner.generateWorkout(profile, "Full Body");
        for (Main.Exercise exercise : routine.getExercises()) {
            assertTrue(planner.isExerciseValid(exercise, profile));
        }
        profile.setWorkoutDuration(5);
        assertEquals(0, planner.generateSchedule(profile).getRoutine("Wednesday").getNumberOfExercises());
    }

    @Test
    void ninetyMinuteHypertrophyUpperUsesTimeBudgetAndSeparateRestTimes() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.Profile profile = validProfile();
        profile.setFitnessGoal("Muscle Gain");
        profile.setWorkoutDuration(90);
        profile.addPreferredWorkoutDay("Monday");
        profile.addPreferredWorkoutDay("Thursday");
        profile.addEquipment("bench");
        profile.addEquipment("cable machine");
        Main.WorkoutRoutine routine = planner.generateSchedule(profile).getRoutine("Monday");
        assertEquals(9, routine.getNumberOfExercises());
        assertEquals(22, routine.getExercises().stream().mapToInt(Main.Exercise::getSets).sum());
        assertEquals("Barbell Bench Press", routine.getExercises().get(0).getName());
        assertEquals("Lat Pulldown", routine.getExercises().get(1).getName());
        assertTrue(routine.containsExercise("Triceps Pushdown"));
        assertTrue(routine.containsExercise("Incline Dumbbell Curl"));
        assertTrue(routine.toString().contains("6-10 reps, rest 150 sec"));
        assertTrue(routine.toString().contains("12-20 reps, rest 75 sec"));
        assertFalse(routine.toString().contains("RIR"));
        Main.Profile limited = validProfile();
        limited.setFitnessGoal("Muscle Gain");
        limited.setWorkoutDuration(90);
        limited.addPreferredWorkoutDay("Monday");
        limited.addPreferredWorkoutDay("Thursday");
        routine = planner.generateSchedule(limited).getRoutine("Monday");
        assertFalse(routine.containsExercise("Incline Dumbbell Press"));
        limited.addLimit("shoulder");
        routine = planner.generateSchedule(limited).getRoutine("Monday");
        for (Main.Exercise exercise : routine.getExercises()) {
            assertTrue(planner.isExerciseValid(exercise, limited));
        }
    }

    @Test
    void editsAreAtomicAndEnforceVolumeAndRecovery() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.Profile profile = validProfile();
        Main.WorkoutRoutine routine = new Main.WorkoutRoutine("Test");
        Main.Exercise original = new Main.Exercise("Press", "Chest", "none", "", 3, 10);
        routine.addExercise(original);
        assertFalse(planner.changeExercise(routine, "Press",
                new Main.Exercise(" ", "Chest", "none", ""), profile));
        assertEquals(original, routine.getExercise("Press"));
        assertTrue(planner.changeExercise(routine, "Press",
                new Main.Exercise("Press", "Chest", "none", "", 4, 12), profile));
        assertEquals(4, routine.getExercise("Press").getSets());
        assertFalse(planner.changeExercise(routine, "Press",
                new Main.Exercise("Press", "Chest", "none", "", 100, 12), profile));
        assertEquals(4, routine.getExercise("Press").getSets());
        assertFalse(planner.addExercise(routine,
                new Main.Exercise("Huge", "Chest", "none", "", 100, 10), profile));
        profile.setWorkoutDuration(30);
        assertFalse(planner.addExercise(routine,
                new Main.Exercise("Long", "Chest", "none", "", 5, 10), profile));
        profile.setWorkoutDuration(60);
        for (String day : new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"}) {
            profile.addPreferredWorkoutDay(day);
        }
        Main.WorkoutRoutine recovery = planner.generateSchedule(profile).getRoutine("Sunday");
        assertFalse(planner.addExercise(recovery, original, profile));
        assertFalse(recovery.addExercise(original));
    }

    @Test
    void injuryFilterAndHypertrophyPreferencesArePreserved() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.Profile profile = validProfile();
        profile.setFitnessGoal("Muscle Gain");
        profile.setWorkoutDuration(90);
        profile.addEquipment("bench");
        profile.addEquipment("cable machine");
        profile.addPreferredWorkoutDay("Monday");
        profile.addPreferredWorkoutDay("Thursday");
        profile.addExercisePreference("Push Ups");
        assertTrue(planner.generateSchedule(profile).getRoutine("Monday").containsExercise("Push Ups"));
        profile.addLimit("shoulder");
        assertFalse(planner.generateSchedule(profile).getRoutine("Monday").containsExercise("Push Ups"));
    }

    @Test
    void eofClosesConsoleCleanlyAtRequiredPrompts() {
        PrintStream original = System.out;
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            for (String input : new String[] {"", "Strength\n", "Strength\n60\n", "Strength\n60\n1\n"}) {
                new Main.ConsoleUI(new Scanner(input)).start();
            }
            assertEquals(4, output.toString(StandardCharsets.UTF_8).split("Input ended", -1).length - 1);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    void targetPercentagesSumToOneHundredAndSurviveGenerationAndEditing() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.Profile profile = validProfile();
        profile.setWorkoutDuration(90);
        for (String group : new String[] {"Chest", "Back", "Legs", "Shoulders", "Arms"}) {
            for (Main.Exercise exercise : planner.generateWorkout(profile, group).getExercises()) {
                assertEquals(100, exercise.getTargetMusclePercentages().values().stream().mapToInt(Integer::intValue).sum());
                assertTrue(exercise.getTargetMusclePercentages().values().stream().allMatch(value -> value > 0));
            }
        }
        Main.WorkoutRoutine routine = planner.generateWorkout(profile, "Chest");
        assertEquals(60, routine.getExercise("Barbell Bench Press").getTargetMusclePercentages().get("Chest"));
        assertTrue(planner.changeExercise(routine, "Barbell Bench Press",
                new Main.Exercise("Barbell Bench Press", "Chest", "barbell", "shoulder", 3, 10), profile));
        assertTrue(routine.toString().contains("Chest 60%"));
        assertEquals(100, new Main.Exercise("Custom", "Arms", "none", "").getTargetMusclePercentages().get("Arms"));
    }

    @Test
    void timeBudgetIncludesWarmupSetRestAndTransitions() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.Profile profile = validProfile();
        profile.setWorkoutDuration(30);
        Main.WorkoutRoutine routine = new Main.WorkoutRoutine("Time boundary");
        assertTrue(planner.addExercise(routine, new Main.Exercise("A", "Chest", "none", "", 4, 10), profile));
        assertTrue(planner.addExercise(routine, new Main.Exercise("B", "Chest", "none", "", 2, 10), profile));
        assertFalse(planner.addExercise(routine, new Main.Exercise("C", "Chest", "none", "", 1, 10), profile));
        assertFalse(planner.changeExercise(routine, "B", new Main.Exercise("B", "Chest", "none", "", 5, 10), profile));
        profile.addPreferredWorkoutDay("Monday");
        routine = planner.generateSchedule(profile).getRoutine("Monday");
        assertEquals(6, routine.getExercises().stream().mapToInt(Main.Exercise::getSets).sum());
    }

    @Test
    void malformedCustomExercisesCannotEnterCatalogOrRoutine() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.WorkoutRoutine routine = new Main.WorkoutRoutine("Test");
        Main.Profile profile = validProfile();
        Main.Exercise[] invalid = {
                new Main.Exercise("Custom", null, "none", ""),
                new Main.Exercise("Custom", "Chest", ",,,", ""),
                new Main.Exercise("Custom", "Chest", "barbell,", ""),
                new Main.Exercise("Custom", "Chest", "none, barbell", ""),
                new Main.Exercise("Custom", "Chest", "none", "", 0, 10),
                new Main.Exercise("Custom", "Chest", "none", "", 3, -1)};
        for (Main.Exercise exercise : invalid) {
            assertFalse(planner.addCustomExercise(exercise));
            assertFalse(routine.addExercise(exercise));
            assertFalse(planner.isExerciseValid(exercise, profile));
        }
        assertNotNull(planner.generateWorkout(profile, "Chest"));
    }

    @Test
    void normalizedNamesAndKnownRestrictionsSurviveManualEdits() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.Profile profile = validProfile();
        Main.WorkoutRoutine routine = new Main.WorkoutRoutine("Test");
        Main.Exercise exercise = new Main.Exercise(" push ups ", "Chest", " none ", "");
        assertTrue(planner.addExercise(routine, exercise, profile));
        assertEquals(60, exercise.getTargetMusclePercentages().get("Chest"));
        assertFalse(planner.addExercise(routine, new Main.Exercise("Push Ups", "Chest", "none", ""), profile));
        assertTrue(planner.removeExercise(routine, "Push Ups"));
        profile.addLimit("shoulder");
        assertFalse(planner.addExercise(routine, exercise, profile));
        assertFalse(planner.addExercise(routine, new Main.Exercise("Lat Pulldown", "Back", "none", ""), profile));
    }

    @Test
    void emptyRoutineMessageDisappearsAfterAddingExercise() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.Profile profile = validProfile();
        Main.WorkoutRoutine routine = planner.generateWorkout(profile, "Custom Group");
        assertTrue(routine.toString().contains("No matching exercises"));
        assertTrue(planner.addExercise(routine, new Main.Exercise("Custom", "Custom Group", "none", ""), profile));
        assertFalse(routine.toString().contains("No matching exercises"));
    }

    @Test
    void scheduleBoundaryMatrixStaysWithinTimeAndSetLimits() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String goal : new String[] {"Strength", "Muscle Gain", "Endurance", "Weight Loss"}) {
            for (int minutes : new int[] {1, 5, 9, 29, 30, 44, 45, 59, 60, 75, 89, 90, Integer.MAX_VALUE}) {
                for (boolean restricted : new boolean[] {false, true}) {
                    Main.Profile profile = validProfile();
                    profile.setFitnessGoal(goal);
                    profile.setWorkoutDuration(minutes);
                    if (restricted) {
                        profile.addLimit("shoulder");
                        profile.addLimit("knee");
                        profile.addLimit("lower back");
                        profile.addLimit("elbow");
                    }
                    for (String day : days) {
                        profile.addPreferredWorkoutDay(day);
                        Main.WorkoutSchedule schedule = planner.generateSchedule(profile);
                        for (String scheduled : days) {
                            Main.WorkoutRoutine routine = schedule.getRoutine(scheduled);
                            if (routine == null) continue;
                            long sets = routine.getExercises().stream().mapToLong(Main.Exercise::getSets).sum();
                            if (sets > 0) {
                                assertTrue(300 + sets * 210 + (routine.getNumberOfExercises() - 1) * 60L <= (long) minutes * 60);
                            }
                            for (Main.Exercise exercise : routine.getExercises()) {
                                assertTrue(planner.isExerciseValid(exercise, profile));
                                assertEquals(100, exercise.getTargetMusclePercentages().values().stream().mapToInt(Integer::intValue).sum());
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    void manualAdditionExceedsTimeButRetainsSafetyChecks() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.Profile profile = validProfile();
        profile.setWorkoutDuration(30);
        Main.WorkoutRoutine routine = new Main.WorkoutRoutine("Manual");
        Main.Exercise custom = new Main.Exercise("Custom", "Chest", "none", "", 10, 10);
        assertTrue(planner.addManualExercise(routine, custom, profile));
        assertTrue(routine.estimatedSeconds() > 30 * 60);
        assertFalse(planner.addManualExercise(routine, custom, profile));
        profile.addLimit("shoulder");
        assertFalse(planner.addManualExercise(routine, new Main.Exercise("Push Ups", "Chest", "none", ""), profile));
    }

    @Test
    void consoleWarnsAfterKeepingOverBudgetAdditionAndRespondsImmediatelyToDayCount() {
        PrintStream original = System.out;
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            new Main.ConsoleUI(new Scanner("Strength\n30\n1\nMonday\nnone\n\n\n\nedit\nMonday\nadd\nCustom\nChest\nnone\n\n10\n10\n\n")).start();
            String text = output.toString(StandardCharsets.UTF_8);
            assertTrue(text.indexOf("Only one day?") < text.indexOf("Workout day 1"));
            assertTrue(text.contains("Warning: exercise kept"));
            assertTrue(text.contains("- Custom"));
            assertFalse(text.contains("guideline"));
            output.reset();
            assertEquals(7, new Main.ConsoleUI(new Scanner("7\n")).readWorkoutDays());
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("You need some rest. I'm going to add a recovery day."));
        } finally {
            System.setOut(original);
        }
    }

    @Test
    void generateWorkoutRejectsNonPositiveDuration() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.Profile profile = new Main.Profile();
        profile.setFitnessGoal("Strength");
        assertNull(planner.generateWorkout(profile, "Chest"));
        for (int duration : new int[] {0, -10}) {
            profile.setWorkoutDuration(duration);
            assertNull(planner.generateWorkout(profile, "Chest"));
            profile.addPreferredWorkoutDay("Monday");
            assertNull(planner.generateSchedule(profile).getRoutine("Monday"));
        }
    }

    @Test
    void generatedWorkoutsRespectThirtySixtyAndNinetyMinuteBudgets() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        int[] durations = {30, 60, 90};
        int[] expectedExerciseCounts = {2, 5, 6};
        for (int index = 0; index < durations.length; index++) {
            Main.Profile profile = validProfile();
            profile.setWorkoutDuration(durations[index]);
            Main.WorkoutRoutine routine = planner.generateWorkout(profile, "Chest");
            assertNotNull(routine);
            assertEquals(expectedExerciseCounts[index], routine.getNumberOfExercises());
            assertTrue(routine.estimatedSeconds() <= durations[index] * 60L);
            assertFalse(planner.addExercise(routine,
                    new Main.Exercise("Extra Press", "Chest", "none", "", 3, 10), profile));
        }
    }

    @Test
    void invalidDurationClearsPreviouslyValidDuration() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        for (int duration : new int[] {0, -10}) {
            Main.Profile profile = validProfile();
            profile.setWorkoutDuration(duration);
            assertEquals(0, profile.getWorkoutDuration());
            assertNull(planner.generateWorkout(profile, "Chest"));
        }
    }

    @Test
    void splitNamesIgnoreCaseAndWhitespace() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        for (String group : new String[] {"Upper", "Push", "Pull", "Biceps", "Triceps"}) {
            Main.WorkoutRoutine expected = planner.generateWorkout(validProfile(), group);
            Main.WorkoutRoutine actual = planner.generateWorkout(validProfile(),
                    " " + group.toLowerCase(java.util.Locale.ROOT) + " ");
            assertTrue(expected.getNumberOfExercises() > 0);
            assertEquals(expected.toString(), actual.toString());
        }
    }

    @Test
    void customArmExercisesUseExplicitMuscleGroup() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.Profile profile = validProfile();
        profile.setWorkoutDuration(180);
        assertTrue(planner.addCustomExercise(new Main.Exercise(
                "Custom Biceps Exercise", "Arms", "none", "", 1, 10)));
        assertFalse(planner.generateWorkout(profile, "Triceps")
                .containsExercise("Custom Biceps Exercise"));
        assertTrue(planner.generateWorkout(profile, "Arms")
                .containsExercise("Custom Biceps Exercise"));
        assertTrue(planner.addCustomExercise(new Main.Exercise(
                "Custom Curl", "triceps", "none", "", 1, 10)));
        assertTrue(planner.generateWorkout(profile, "Triceps").containsExercise("Custom Curl"));
        assertTrue(planner.generateWorkout(profile, "Arms").containsExercise("Custom Curl"));
        assertFalse(planner.generateWorkout(profile, "Biceps").containsExercise("Custom Curl"));
    }

    @Test
    void shortFullBodySessionPrioritizesPreferredExercise() {
        Main.WorkoutPlanner planner = new Main.WorkoutPlanner();
        Main.Profile profile = validProfile();
        profile.setWorkoutDuration(10);
        profile.addPreferredWorkoutDay("Monday");
        profile.addExercisePreference("Push Ups");
        Main.WorkoutRoutine routine = planner.generateSchedule(profile).getRoutine("Monday");
        assertTrue(routine.containsExercise("Push Ups"));
        assertTrue(routine.estimatedSeconds() <= 600);
        profile.addLimit("shoulder");
        assertFalse(planner.generateSchedule(profile).getRoutine("Monday").containsExercise("Push Ups"));
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
