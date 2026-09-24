import java.util.Scanner;

/**
 * ConsoleUI: reads the user's input, validates it (re-prompting until it is
 * valid), and stores the results in the model. It also displays routines and
 * schedules. It does no workout-generation logic; that belongs to WorkoutPlanner.
 */
public class ConsoleUI {

    private final Scanner in;
    private final Profile profile;
    private final WorkoutSchedule schedule;
    private final WorkoutPlanner planner;

    public ConsoleUI(Profile profile, WorkoutSchedule schedule, WorkoutPlanner planner) {
        this(profile, schedule, planner, new Scanner(System.in));
    }

    /** Lets tests pass in a Scanner backed by a String instead of System.in. */
    public ConsoleUI(Profile profile, WorkoutSchedule schedule, WorkoutPlanner planner, Scanner in) {
        this.profile = profile;
        this.schedule = schedule;
        this.planner = planner;
        this.in = in;
    }

    // ------------------------------------------------------------------
    // Main flow
    // ------------------------------------------------------------------

    /** Starts the program: profile setup, then the main menu. */
    public void start() {
        System.out.println("=== Workout Planner ===");
        setUpProfile();

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("Main menu");
            System.out.println("  1) Generate my workout plan");
            System.out.println("  2) View my schedule");
            System.out.println("  3) Exit");
            String choice = prompt("Choose an option: ");

            switch (choice) {
                case "1":
                    generatePlan();
                    break;
                case "2":
                    displaySchedule(schedule);
                    break;
                case "3":
                    System.out.println("Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please enter 1, 2, or 3.");
            }
        }
    }

    /** Collects everything the Profile needs and stores it. */
    private void setUpProfile() {
        System.out.println("Let's set up your profile.");

        profile.setFitnessGoal(readFitnessGoal());
        profile.setWorkoutDuration(readWorkoutDuration());

        int daysWanted = readWorkoutDays();
        while (profile.getPreferredWorkoutDays().size() < daysWanted) {
            String day = readWorkoutDay();
            if (profile.getPreferredWorkoutDays().contains(day)) {
                System.out.println("You already picked " + day + ". Please choose a different day.");
            } else {
                profile.addPreferredWorkoutDay(day);
            }
        }

        while (askYesNo("Add a piece of equipment you have access to?")) {
            profile.addEquipment(readEquipment());
        }
        while (askYesNo("Add an injury or physical limitation?")) {
            profile.addLimit(readNonEmpty("Describe the limitation (e.g. Knee injury): "));
        }
        while (askYesNo("Add an exercise you like?")) {
            profile.addExercisePreference(readNonEmpty("Exercise name: "));
        }

        System.out.println("Profile saved.");
    }

    /** Asks for a muscle group for each preferred day and builds the plan. */
    private void generatePlan() {
        if (profile.getFitnessGoal() == null) {
            System.out.println("Please assign fitness goals.");
            return;
        }

        for (String day : Profile.DAYS_OF_WEEK) {
            if (!profile.getPreferredWorkoutDays().contains(day)) {
                continue;
            }
            String group = readNonEmpty("Which muscle group on " + day + "? (e.g. Chest): ");

            if (!schedule.addWorkoutDay(day, group)) {
                System.out.println(day + " is already scheduled. Skipping.");
                continue;
            }

            WorkoutRoutine routine = planner.generateWorkout(profile, group);
            if (routine == null) {
                System.out.println("Could not generate a routine for " + day + ".");
                continue;
            }
            schedule.assignRoutine(day, routine);
            System.out.println();
            System.out.println(day + " - " + group);
            displayRoutine(routine);
        }
    }

    // ------------------------------------------------------------------
    // Input methods (each one loops until the input is valid)
    // ------------------------------------------------------------------

    /** Reads and returns the user's fitness goal. Empty input re-prompts. */
    public String readFitnessGoal() {
        return readNonEmpty("What is your fitness goal? (e.g. Strength, Muscle Gain): ");
    }

    /** Reads and returns the number of workout days per week (1-7). */
    public int readWorkoutDays() {
        while (true) {
            String line = prompt("How many days per week do you want to work out? ");
            try {
                int days = Integer.parseInt(line);
                if (days >= Profile.MIN_WORKOUT_DAYS && days <= Profile.MAX_WORKOUT_DAYS) {
                    return days;
                }
            } catch (NumberFormatException e) {
                // fall through to the error message below
            }
            System.out.println("Response should be a number between "
                    + Profile.MIN_WORKOUT_DAYS + " - " + Profile.MAX_WORKOUT_DAYS + ".");
        }
    }

    /** Reads and returns the preferred workout duration in minutes. */
    public int readWorkoutDuration() {
        while (true) {
            String line = prompt("How many minutes should each workout last? ");
            try {
                int minutes = Integer.parseInt(line);
                if (minutes > 0) {
                    return minutes;
                }
                System.out.println("Response should be a positive number of minutes.");
            } catch (NumberFormatException e) {
                System.out.println("Response should be a number.");
            }
        }
    }

    /** Reads and returns a valid day of the week, capitalized (e.g. "Monday"). */
    public String readWorkoutDay() {
        while (true) {
            String line = prompt("Enter a preferred workout day (e.g. Monday): ");
            String day = Profile.normalizeDay(line);
            if (day != null) {
                return day;
            }
            System.out.println("'" + line + "' is not a valid day. Please enter a day like Monday.");
        }
    }

    /** Reads and returns a piece of equipment. Empty input re-prompts. */
    public String readEquipment() {
        return readNonEmpty("Enter a piece of equipment (e.g. Dumbbells): ");
    }

    // ------------------------------------------------------------------
    // Display methods
    // ------------------------------------------------------------------

    /** Prints every exercise in the routine, or a message if it is empty. */
    public void displayRoutine(WorkoutRoutine routine) {
        if (routine == null || routine.getNumberOfExercises() == 0) {
            System.out.println("No exercises in this routine.");
            return;
        }
        int i = 1;
        for (Exercise exercise : routine.getExercises()) {
            System.out.println("  " + i + ". " + exercise.getName()
                    + " (" + exercise.getPrimaryMuscleGroup() + ")");
            i++;
        }
    }

    /** Prints each scheduled day (Monday to Sunday) with its group and routine. */
    public void displaySchedule(WorkoutSchedule schedule) {
        boolean anyScheduled = false;
        for (String day : Profile.DAYS_OF_WEEK) {
            if (!schedule.isWorkoutDay(day)) {
                continue;
            }
            anyScheduled = true;
            System.out.println();
            System.out.println(day + " - " + schedule.getMuscleGroup(day));
            WorkoutRoutine routine = schedule.getRoutine(day);
            if (routine == null) {
                System.out.println("  (no routine assigned yet)");
            } else {
                displayRoutine(routine);
            }
        }
        if (!anyScheduled) {
            System.out.println("No workouts scheduled.");
        }
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private String prompt(String message) {
        System.out.print(message);
        return in.nextLine().trim();
    }

    private String readNonEmpty(String message) {
        while (true) {
            String line = prompt(message);
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Response cannot be empty.");
        }
    }

    private boolean askYesNo(String question) {
        while (true) {
            String line = prompt(question + " (y/n): ").toLowerCase();
            if (line.equals("y") || line.equals("yes")) {
                return true;
            }
            if (line.equals("n") || line.equals("no")) {
                return false;
            }
            System.out.println("Please answer y or n.");
        }
    }
}
