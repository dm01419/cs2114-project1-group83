import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for ConsoleUI. Input comes from a String and output is captured, so
 * each test can check what the user would see.
 */
class ConsoleUITest {
    /** Profile setup answers: goal, minutes, 1 day (Monday), no extras. */
    private static final String BASIC_SETUP = "Strength\n60\n1\nMonday\nn\nn\nn\n";

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private PrintStream originalOut;
    private Profile profile;
    private WorkoutSchedule schedule;
    private WorkoutPlanner planner;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        System.setOut(new PrintStream(output));
        profile = new Profile();
        schedule = new WorkoutSchedule();
        planner = new WorkoutPlanner();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private ConsoleUI uiWithInput(String input) {
        return new ConsoleUI(profile, schedule, planner, new Scanner(input));
    }

    private String out() {
        return output.toString();
    }

    @Test
    void startShowsPromptsAndExits() {
        uiWithInput(BASIC_SETUP + "8\n").start();
        assertTrue(out().contains("=== Workout Planner ==="));
        assertTrue(out().contains("Main menu"));
        assertTrue(out().contains("Goodbye!"));
        assertEquals("Strength", profile.getFitnessGoal());
        assertTrue(profile.getPreferredWorkoutDays().contains("Monday"));
    }

    @Test
    void startRepromptsOnInvalidMenuOption() {
        uiWithInput(BASIC_SETUP + "9\n8\n").start();
        assertTrue(out().contains("Invalid option. Please enter a number from 1 to 8."));
        assertTrue(out().contains("Goodbye!"));
    }

    @Test
    void readFitnessGoalReturnsGoal() {
        assertEquals("Strength", uiWithInput("Strength\n").readFitnessGoal());
    }

    @Test
    void readFitnessGoalRepromptsOnEmpty() {
        assertEquals("Strength", uiWithInput("\nStrength\n").readFitnessGoal());
        assertTrue(out().contains("Response cannot be empty."));
    }

    @Test
    void readWorkoutDaysReturnsNumber() {
        assertEquals(4, uiWithInput("4\n").readWorkoutDays());
    }

    @Test
    void readWorkoutDaysRepromptsOnLettersZeroAndEight() {
        assertEquals(4, uiWithInput("four\n0\n8\n4\n").readWorkoutDays());
        assertTrue(out().contains("Response should be a number between 1 - 7."));
    }

    @Test
    void readWorkoutDurationReturnsMinutes() {
        assertEquals(60, uiWithInput("60\n").readWorkoutDuration());
    }

    @Test
    void readWorkoutDurationRepromptsOnBadInput() {
        String bad = "hour\n-30\n0\n50000\n99999999999\n60.5\n9\n181\n";
        assertEquals(60, uiWithInput(bad + "60\n").readWorkoutDuration());
        String message = "Response should be a number between 10 - 180 minutes.";
        assertEquals(8, out().split(message, -1).length - 1);
    }

    @Test
    void readWorkoutDurationAcceptsLimits() {
        assertEquals(10, uiWithInput("10\n").readWorkoutDuration());
        assertEquals(180, uiWithInput("180\n").readWorkoutDuration());
    }

    @Test
    void readFitnessGoalRepromptsOnUnknownGoal() {
        assertEquals("Muscle Gain",
                uiWithInput("Get huge\nmuscle gain\n").readFitnessGoal());
        assertTrue(out().contains("Response should be one of: Strength, Muscle Gain,"
                + " Endurance, General Fitness."));
    }

    @Test
    void longTextIsRepromptedEverywhere() {
        String tooLong = "x".repeat(Profile.MAX_TEXT_LENGTH + 1);
        assertEquals("Dumbbells",
                uiWithInput(tooLong + "\nDumbbells\n").readEquipment());
        assertTrue(out().contains("Response must be 50 characters or fewer."));
    }

    @Test
    void unknownMuscleGroupIsReprompted() {
        uiWithInput(BASIC_SETUP + "1\nBanana\nchest\n8\n").start();
        assertTrue(out().contains("Response should be one of: Chest, Back, Legs,"
                + " Shoulders, Arms, Core."));
        assertEquals("Chest", schedule.getMuscleGroup("Monday"));
    }

    @Test
    void setupStopsAskingWhenListIsFull() {
        for (int i = 1; i <= Profile.MAX_LIST_SIZE; i++) {
            profile.addEquipment("Item " + i);
        }
        uiWithInput("Strength\n60\n1\nMonday\nn\nn\n8\n").start();
        assertTrue(out().contains("You have reached the limit of 20 equipment."));
        assertTrue(!out().contains("Add a piece of equipment"));
        assertTrue(out().contains("Goodbye!"));
    }

    @Test
    void customExerciseWithUnknownGroupIsReprompted() {
        String input = BASIC_SETUP
                + "1\nChest\n"
                + "5\nMonday\nPush Ups\n"
                + "4\nMonday\nCable Fly\nBanana\n\n\n"
                + "8\n";
        uiWithInput(input).start();
        assertTrue(out().contains("Response should be one of: Chest, Back, Legs,"
                + " Shoulders, Arms, Core."));
        assertEquals("Chest",
                schedule.getRoutine("Monday").getExercise("Cable Fly")
                        .getPrimaryMuscleGroup());
    }

    @Test
    void readWorkoutDayReturnsDay() {
        assertEquals("Monday", uiWithInput("monday\n").readWorkoutDay());
    }

    @Test
    void readWorkoutDayRepromptsOnInvalidDay() {
        assertEquals("Monday", uiWithInput("Funday\nMonday\n").readWorkoutDay());
        assertTrue(out().contains("'Funday' is not a valid day."));
    }

    @Test
    void readEquipmentReturnsEquipment() {
        assertEquals("Dumbbells", uiWithInput("Dumbbells\n").readEquipment());
    }

    @Test
    void readEquipmentRepromptsOnEmpty() {
        assertEquals("Dumbbells", uiWithInput("\nDumbbells\n").readEquipment());
        assertTrue(out().contains("Response cannot be empty."));
    }

    @Test
    void displayRoutineShowsExercisesWithSetsAndReps() {
        WorkoutRoutine routine = new WorkoutRoutine("Chest Workout");
        routine.addExercise(new Exercise("Bench Press", "Chest", null, "Barbell", 4, 8));
        routine.addExercise(new Exercise("Push Ups", "Chest", null, null, 3, 15));
        uiWithInput("").displayRoutine(routine);
        assertTrue(out().contains("1. Bench Press 4 x 8 (Chest)"));
        assertTrue(out().contains("2. Push Ups 3 x 15 (Chest)"));
    }

    @Test
    void displayRoutineSaysWhenEmpty() {
        uiWithInput("").displayRoutine(new WorkoutRoutine("Empty"));
        assertTrue(out().contains("No exercises in this routine."));
    }

    @Test
    void displayScheduleShowsWorkoutAndRestDays() {
        schedule.addWorkoutDay("Monday", "Chest");
        schedule.addWorkoutDay("Wednesday", "Back");
        schedule.addWorkoutDay("Friday", "Legs");
        uiWithInput("").displaySchedule(schedule);
        assertTrue(out().contains("Monday - Chest"));
        assertTrue(out().contains("Wednesday - Back"));
        assertTrue(out().contains("Friday - Legs"));
        assertTrue(out().contains("Tuesday - Rest day"));
    }

    @Test
    void displayScheduleSaysWhenEmpty() {
        uiWithInput("").displaySchedule(schedule);
        assertTrue(out().contains("No workouts scheduled."));
    }

    @Test
    void generatePlanBuildsScheduleFromMenu() {
        profile.addEquipment("Barbell");
        uiWithInput(BASIC_SETUP + "1\nChest\n2\n8\n").start();
        assertTrue(schedule.isWorkoutDay("Monday"));
        assertTrue(schedule.getRoutine("Monday").containsExercise("Barbell Bench Press"));
        assertTrue(out().contains("Barbell Bench Press 5 x 5"));
    }

    @Test
    void workoutOnRestDayIsRefused() {
        schedule.addWorkoutDay("Monday", "Chest");
        uiWithInput("").showWorkoutFor("Sunday");
        assertTrue(out().contains("Today is a rest day, you must let your body rest"
                + " before starting again, please come back on Monday."));
    }

    @Test
    void workoutOnWorkoutDayIsShown() {
        schedule.addWorkoutDay("Monday", "Chest");
        uiWithInput("").showWorkoutFor("Monday");
        assertTrue(out().contains("Monday - Chest"));
    }

    @Test
    void addingInjuryExerciseIsRefused() {
        profile.addEquipment("Barbell");
        profile.addLimit("Knee injury");
        uiWithInput(BASIC_SETUP + "1\nLegs\n4\nMonday\nBarbell Squat\n8\n").start();
        assertTrue(out().contains("Based on your past injuries, this workout will cause pain,"
                + " please choose something else."));
        assertTrue(!schedule.getRoutine("Monday").containsExercise("Barbell Squat"));
    }

    @Test
    void addingTooManyExercisesIsRefused() {
        profile.addEquipment("Barbell");
        String setup = "Strength\n30\n1\nMonday\nn\nn\nn\n";
        uiWithInput(setup + "1\nLegs\n4\nMonday\n8\n").start();
        assertTrue(out().contains("Chose too many exercises, please choose from the"
                + " recommended amount"));
    }

    @Test
    void addRemoveChangeAndMoveFromMenu() {
        String input = BASIC_SETUP
                + "1\nChest\n"
                + "5\nMonday\nPush Ups\n"                       // remove
                + "4\nMonday\nPush Ups\n"                       // add back
                + "6\nMonday\nPush Ups\nCable Fly\nChest\n\n"   // change to custom
                + "7\nMonday\nThursday\n"                       // move day
                + "8\n";
        uiWithInput(input).start();
        assertTrue(out().contains("Removed Push Ups from Monday."));
        assertTrue(out().contains("Added Push Ups 5 x 5 to Monday."));
        assertTrue(out().contains("Replaced Push Ups with Cable Fly 5 x 5."));
        assertTrue(out().contains("Moved Monday's workout to Thursday."));
        assertTrue(schedule.isWorkoutDay("Thursday"));
        assertTrue(schedule.getRoutine("Thursday").containsExercise("Cable Fly"));
    }

    @Test
    void editingARestDayIsRefused() {
        uiWithInput(BASIC_SETUP + "5\nSunday\n8\n").start();
        assertTrue(out().contains("Sunday is a rest day."));
    }

    @Test
    void missingEquipmentIsExplained() {
        uiWithInput(BASIC_SETUP + "1\nChest\n4\nMonday\nBarbell Bench Press\n8\n").start();
        assertTrue(out().contains("You don't have the equipment for Barbell Bench Press"
                + " (needs Barbell)."));
    }
}
