import java.time.LocalDate;
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
            System.out.println("  3) Start today's workout");
            System.out.println("  4) Add an exercise to a workout day");
            System.out.println("  5) Remove an exercise from a workout day");
            System.out.println("  6) Change an exercise");
            System.out.println("  7) Move a workout to another day");
            System.out.println("  8) Exit");
            String choice = prompt("Choose an option: ");

            switch (choice) {
                case "1":
                    generatePlan();
                    break;
                case "2":
                    displaySchedule(schedule);
                    break;
                case "3":
                    showWorkoutFor(today());
                    break;
                case "4":
                    addExerciseToDay();
                    break;
                case "5":
                    removeExerciseFromDay();
                    break;
                case "6":
                    changeExerciseOnDay();
                    break;
                case "7":
                    moveWorkoutDay();
                    break;
                case "8":
                    System.out.println("Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number from 1 to 8.");
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
    // Editing the plan
    // ------------------------------------------------------------------

    /**
     * Shows the workout for the given day, or tells the user it is a rest
     * day and when to come back.
     */
    public void showWorkoutFor(String day) {
        if (!schedule.isWorkoutDay(day)) {
            String next = nextWorkoutDay(day);
            System.out.println("Today is a rest day, you must let your body rest"
                    + " before starting again, please come back "
                    + (next == null ? "once you have a plan." : "on " + next + "."));
            return;
        }
        System.out.println(day + " - " + schedule.getMuscleGroup(day));
        displayRoutine(schedule.getRoutine(day));
    }

    /** Adds an exercise (from the list or a custom one) to a workout day. */
    private void addExerciseToDay() {
        String day = readScheduledDay();
        if (day == null) {
            return;
        }
        WorkoutRoutine routine = routineFor(day);
        if (planner.isRoutineFull(routine, profile)) {
            System.out.println("Chose too many exercises, please choose from the"
                    + " recommended amount (" + planner.getMaxExercises(profile)
                    + " for a " + profile.getWorkoutDuration() + " minute workout).");
            return;
        }
        Exercise exercise = readExercise(schedule.getMuscleGroup(day));
        if (explainIfInvalid(exercise)) {
            return;
        }
        if (planner.addExercise(routine, exercise, profile)) {
            System.out.println("Added " + exercise + " to " + day + ".");
        } else {
            System.out.println(exercise.getName() + " is already in " + day + "'s routine.");
        }
    }

    /** Removes an exercise from a workout day. */
    private void removeExerciseFromDay() {
        String day = readScheduledDay();
        if (day == null) {
            return;
        }
        String name = readNonEmpty("Exercise to remove: ");
        if (planner.removeExercise(schedule.getRoutine(day), name)) {
            System.out.println("Removed " + name + " from " + day + ".");
        } else {
            System.out.println(name + " is not in " + day + "'s routine.");
        }
    }

    /** Replaces one exercise on a workout day with another. */
    private void changeExerciseOnDay() {
        String day = readScheduledDay();
        if (day == null) {
            return;
        }
        WorkoutRoutine routine = schedule.getRoutine(day);
        String oldName = readNonEmpty("Exercise to replace: ");
        if (routine == null || !routine.containsExercise(oldName)) {
            System.out.println(oldName + " is not in " + day + "'s routine.");
            return;
        }
        Exercise replacement = readExercise(schedule.getMuscleGroup(day));
        if (explainIfInvalid(replacement)) {
            return;
        }
        if (planner.changeExercise(routine, oldName, replacement, profile)) {
            System.out.println("Replaced " + oldName + " with " + replacement + ".");
        } else {
            System.out.println(replacement.getName() + " is already in " + day + "'s routine.");
        }
    }

    /** Moves a workout (and its routine) from one day to another. */
    private void moveWorkoutDay() {
        String oldDay = readScheduledDay();
        if (oldDay == null) {
            return;
        }
        String newDay = readWorkoutDay();
        if (schedule.changeWorkoutDay(oldDay, newDay)) {
            System.out.println("Moved " + oldDay + "'s workout to " + newDay + ".");
        } else {
            System.out.println(newDay + " already has a workout. Pick a rest day.");
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
            System.out.println("  " + i + ". " + exercise
                    + " (" + exercise.getPrimaryMuscleGroup() + ")");
            i++;
        }
    }

    /**
     * Prints every day from Monday to Sunday: workout days with their group
     * and routine, and the other days as rest days.
     */
    public void displaySchedule(WorkoutSchedule schedule) {
        boolean anyScheduled = false;
        for (String day : Profile.DAYS_OF_WEEK) {
            if (schedule.isWorkoutDay(day)) {
                anyScheduled = true;
            }
        }
        if (!anyScheduled) {
            System.out.println("No workouts scheduled.");
            return;
        }
        for (String day : Profile.DAYS_OF_WEEK) {
            System.out.println();
            if (!schedule.isWorkoutDay(day)) {
                System.out.println(day + " - Rest day");
                continue;
            }
            System.out.println(day + " - " + schedule.getMuscleGroup(day));
            WorkoutRoutine routine = schedule.getRoutine(day);
            if (routine == null) {
                System.out.println("  (no routine assigned yet)");
            } else {
                displayRoutine(routine);
            }
        }
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /**
     * Asks for a day that is in the schedule. Returns null (after a message)
     * if there is no plan yet or the user picks a rest day.
     */
    private String readScheduledDay() {
        String day = readWorkoutDay();
        if (schedule.isWorkoutDay(day)) {
            return day;
        }
        System.out.println(day + " is a rest day. Generate a plan or pick one of your workout days.");
        return null;
    }

    /** Returns the day's routine, creating an empty one if it has none. */
    private WorkoutRoutine routineFor(String day) {
        WorkoutRoutine routine = schedule.getRoutine(day);
        if (routine == null) {
            routine = new WorkoutRoutine(schedule.getMuscleGroup(day) + " Workout");
            schedule.assignRoutine(day, routine);
        }
        return routine;
    }

    /**
     * Asks for an exercise name. If it is in the planner's list, that one is
     * used; otherwise the user describes a custom exercise.
     */
    private Exercise readExercise(String defaultGroup) {
        String name = readNonEmpty("Exercise name: ");
        Exercise exercise = planner.findExercise(name, profile);
        if (exercise != null) {
            return exercise;
        }
        System.out.println(name + " is not in our list, so let's add it as a custom exercise.");
        String group = prompt("Main muscle group (press Enter for " + defaultGroup + "): ");
        if (group.isEmpty()) {
            group = defaultGroup;
        }
        String equipmentNeeded = prompt("Equipment needed (press Enter for none): ");
        int[] setsReps = planner.getSetsAndReps(profile);
        return new Exercise(name, group, null, equipmentNeeded, setsReps[0], setsReps[1]);
    }

    /**
     * Prints why the user cannot do the exercise and returns true, or returns
     * false if the exercise is fine.
     */
    private boolean explainIfInvalid(Exercise exercise) {
        if (!exercise.isSafeFor(profile)) {
            System.out.println("Based on your past injuries, this workout will cause pain,"
                    + " please choose something else.");
            return true;
        }
        if (!exercise.hasEquipmentFor(profile)) {
            System.out.println("You don't have the equipment for " + exercise.getName()
                    + " (needs " + exercise.getRequiredEquipment() + ").");
            return true;
        }
        return false;
    }

    /** Returns today's day name, like "Monday". */
    private String today() {
        return Profile.normalizeDay(LocalDate.now().getDayOfWeek().toString());
    }

    /** Returns the next workout day after the given day, or null if none. */
    private String nextWorkoutDay(String day) {
        int start = Profile.DAYS_OF_WEEK.indexOf(Profile.normalizeDay(day));
        for (int i = 1; i <= 7; i++) {
            String next = Profile.DAYS_OF_WEEK.get((start + i) % 7);
            if (schedule.isWorkoutDay(next)) {
                return next;
            }
        }
        return null;
    }

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
