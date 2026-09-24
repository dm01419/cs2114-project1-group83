import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Profile: stores one user's fitness goal, limitations, workout duration,
 * equipment, preferred workout days, and exercise preferences.
 *
 * Validation lives here. Every setter/adder rejects bad input by throwing an
 * IllegalArgumentException and leaves the profile unchanged. ConsoleUI catches
 * nothing itself for these cases; it validates first and re-prompts, and the
 * exceptions are the safety net for any other caller.
 */
public class Profile {

    public static final int DEFAULT_DURATION_MINUTES = 60;
    public static final int MIN_WORKOUT_DAYS = 1;
    public static final int MAX_WORKOUT_DAYS = 7;
    public static final int MIN_DURATION_MINUTES = 10;
    public static final int MAX_DURATION_MINUTES = 180;
    /** Longest text allowed for a goal, limitation, equipment or exercise name. */
    public static final int MAX_TEXT_LENGTH = 50;
    /** Most limitations, equipment items or exercise preferences allowed. */
    public static final int MAX_LIST_SIZE = 20;

    public static final List<String> FITNESS_GOALS = List.of(
            "Strength", "Muscle Gain", "Endurance", "General Fitness");

    public static final List<String> DAYS_OF_WEEK = List.of(
            "Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday", "Saturday", "Sunday");

    // ---- Fields (types match the design doc) ----
    private final List<String> goals = new ArrayList<>();
    private final List<String> limitations = new ArrayList<>();
    private int duration = DEFAULT_DURATION_MINUTES;
    private final List<String> equipment = new ArrayList<>();
    private final Set<String> preferredDays = new LinkedHashSet<>(); // Set: no duplicate days
    private final List<String> preferences = new ArrayList<>();

    // ---- Setters / adders (from the design doc) ----

    /**
     * Sets the user's fitness goal, replacing any previous goal. The goal must
     * be one of FITNESS_GOALS (any capitalization).
     */
    public void setFitnessGoal(String goal) {
        requireText(goal, "Fitness goal");
        String normalized = normalizeGoal(goal);
        if (normalized == null) {
            throw new IllegalArgumentException("Fitness goal must be one of "
                    + String.join(", ", FITNESS_GOALS) + ".");
        }
        goals.clear();
        goals.add(normalized);
    }

    /** Sets the max amount of time (minutes) the user wants each workout to last. */
    public void setWorkoutDuration(int minutes) {
        if (minutes < MIN_DURATION_MINUTES || minutes > MAX_DURATION_MINUTES) {
            throw new IllegalArgumentException("Workout duration must be between "
                    + MIN_DURATION_MINUTES + " and " + MAX_DURATION_MINUTES + " minutes.");
        }
        duration = minutes;
    }

    /** Adds a physical limit or injury to the profile. */
    public void addLimit(String limit) {
        addUnique(limitations, requireText(limit, "Limitation"));
    }

    /** Adds a piece of equipment available to the user. */
    public void addEquipment(String equipmentName) {
        addUnique(equipment, requireText(equipmentName, "Equipment"));
    }

    /** Adds a preferred workout day (e.g. "monday" is stored as "Monday"). */
    public void addPreferredWorkoutDay(String day) {
        String normalized = normalizeDay(day);
        if (normalized == null) {
            throw new IllegalArgumentException("'" + day + "' is not a valid day of the week.");
        }
        preferredDays.add(normalized);
    }

    /** Adds an exercise the user likes. */
    public void addExercisePreference(String exercise) {
        addUnique(preferences, requireText(exercise, "Exercise preference"));
    }

    // ---- Getters ----

    /** Returns the current fitness goal, or null if none has been set. */
    public String getFitnessGoal() {
        return goals.isEmpty() ? null : goals.get(0);
    }

    /** Returns the preferred workout duration in minutes (default if never set). */
    public int getWorkoutDuration() {
        return duration;
    }

    // Extra getters: Exercise.canPerformWith and WorkoutPlanner.isExerciseValid
    // need these to check equipment and limitations.

    public List<String> getLimitations() {
        return Collections.unmodifiableList(limitations);
    }

    public List<String> getEquipment() {
        return Collections.unmodifiableList(equipment);
    }

    public Set<String> getPreferredWorkoutDays() {
        return Collections.unmodifiableSet(preferredDays);
    }

    public List<String> getExercisePreferences() {
        return Collections.unmodifiableList(preferences);
    }

    // ---- Shared helpers (ConsoleUI reuses the day check) ----

    /**
     * Returns the properly capitalized day name ("Monday"), or null if the
     * input is not a real day of the week.
     */
    public static String normalizeDay(String day) {
        return matchIgnoringCase(day, DAYS_OF_WEEK);
    }

    /**
     * Returns the properly capitalized goal ("Muscle Gain"), or null if the
     * input is not one of FITNESS_GOALS.
     */
    public static String normalizeGoal(String goal) {
        return matchIgnoringCase(goal, FITNESS_GOALS);
    }

    private static String matchIgnoringCase(String value, List<String> options) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        for (String option : options) {
            if (option.equalsIgnoreCase(trimmed)) {
                return option;
            }
        }
        return null;
    }

    private static String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
        if (value.trim().length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException(label + " must be "
                    + MAX_TEXT_LENGTH + " characters or fewer.");
        }
        return value.trim();
    }

    private static void addUnique(List<String> list, String value) {
        for (String existing : list) {
            if (existing.equalsIgnoreCase(value)) {
                return; // already there, ignore duplicate
            }
        }
        if (list.size() >= MAX_LIST_SIZE) {
            throw new IllegalArgumentException("You can add at most "
                    + MAX_LIST_SIZE + " items.");
        }
        list.add(value);
    }
}
