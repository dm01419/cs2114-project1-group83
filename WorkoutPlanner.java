import java.util.ArrayList;
import java.util.List;

/**
 * WorkoutPlanner: generates workout routines from the user's profile using
 * rules (equipment, injuries, time limit, fitness goal) and adds, removes,
 * and changes exercises in a routine.
 */
public class WorkoutPlanner {
    /** The muscle groups a workout day can target. */
    public static final List<String> MUSCLE_GROUPS = List.of(
            "Chest", "Back", "Legs", "Shoulders", "Arms", "Core");

    private final List<Exercise> exerciseLibrary = new ArrayList<>();

    /** Creates a planner with the built-in list of exercises. */
    public WorkoutPlanner() {
        addToLibrary("Barbell Bench Press", "Chest", "Triceps", "Barbell",
                "shoulder");
        addToLibrary("Dumbbell Bench Press", "Chest", "Triceps", "Dumbbells",
                "shoulder");
        addToLibrary("Incline Dumbbell Press", "Chest", "Shoulders",
                "Dumbbells", "shoulder");
        addToLibrary("Push Ups", "Chest", "Triceps", null, "wrist");
        addToLibrary("Barbell Squat", "Legs", "Glutes", "Barbell", "knee");
        addToLibrary("Dumbbell Lunges", "Legs", "Glutes", "Dumbbells", "knee");
        addToLibrary("Romanian Deadlift", "Legs", "Hamstrings", "Barbell",
                "back");
        addToLibrary("Glute Bridge", "Legs", "Glutes", null);
        addToLibrary("Bodyweight Squat", "Legs", "Glutes", null, "knee");
        addToLibrary("Pull Ups", "Back", "Biceps", "Pull-up Bar", "shoulder");
        addToLibrary("Barbell Row", "Back", "Biceps", "Barbell", "back");
        addToLibrary("Dumbbell Row", "Back", "Biceps", "Dumbbells", "back");
        addToLibrary("Superman", "Back", "Glutes", null);
        addToLibrary("Overhead Press", "Shoulders", "Triceps", "Barbell",
                "shoulder");
        addToLibrary("Dumbbell Lateral Raise", "Shoulders", null, "Dumbbells",
                "shoulder");
        addToLibrary("Pike Push Ups", "Shoulders", "Triceps", null,
                "shoulder", "wrist");
        addToLibrary("Dumbbell Curl", "Arms", "Forearms", "Dumbbells",
                "elbow");
        addToLibrary("Tricep Dips", "Arms", "Chest", null, "shoulder",
                "elbow");
        addToLibrary("Plank", "Core", "Shoulders", null);
        addToLibrary("Crunches", "Core", null, null, "back", "neck");
    }

    /**
     * Creates a workout routine based on the user's profile and target muscle
     * group. Exercises the user likes come first. Returns null if there is no
     * fitness goal or the muscle group is not one of MUSCLE_GROUPS.
     */
    public WorkoutRoutine generateWorkout(Profile profile, String muscleGroup) {
        muscleGroup = normalizeMuscleGroup(muscleGroup);
        if (profile == null
                || profile.getFitnessGoal() == null
                || muscleGroup == null) {
            return null;
        }

        WorkoutRoutine routine = new WorkoutRoutine(muscleGroup + " Workout");

        // Preferred exercises first, then everything else.
        for (Exercise exercise : exerciseLibrary) {
            if (isPreferred(exercise, profile)) {
                addIfFits(routine, exercise, muscleGroup, profile);
            }
        }
        for (Exercise exercise : exerciseLibrary) {
            addIfFits(routine, exercise, muscleGroup, profile);
        }

        return routine;
    }

    /**
     * Adds an exercise to a workout routine if it is valid for the user and
     * the routine is not already full for the user's workout duration.
     */
    public boolean addExercise(WorkoutRoutine routine,
                               Exercise exercise,
                               Profile profile) {
        if (routine == null || exercise == null
                || !isExerciseValid(exercise, profile)
                || isRoutineFull(routine, profile)) {
            return false;
        }

        return routine.addExercise(exercise);
    }

    /** Removes the specified exercise from a workout routine. */
    public boolean removeExercise(WorkoutRoutine routine, String exerciseName) {
        return routine != null && routine.removeExercise(exerciseName);
    }

    /**
     * Replaces an existing exercise with another valid exercise, keeping its
     * place in the routine.
     */
    public boolean changeExercise(WorkoutRoutine routine,
                                  String oldExercise,
                                  Exercise newExercise,
                                  Profile profile) {
        if (routine == null || newExercise == null
                || !isExerciseValid(newExercise, profile)) {
            return false;
        }

        return routine.replaceExercise(oldExercise, newExercise);
    }

    /**
     * Checks whether an exercise matches the user's available equipment and
     * limitations.
     */
    public boolean isExerciseValid(Exercise exercise, Profile profile) {
        return exercise != null && exercise.canPerformWith(profile);
    }

    /**
     * Returns the most exercises a routine should have for the user's workout
     * duration: 3 for 30 minutes or less, 5 for up to an hour, otherwise 6.
     */
    public int getMaxExercises(Profile profile) {
        if (profile == null || profile.getWorkoutDuration() <= 30) {
            return 3;
        }

        if (profile.getWorkoutDuration() <= 60) {
            return 5;
        }

        return 6;
    }

    /** Returns whether the routine already has the most exercises allowed. */
    public boolean isRoutineFull(WorkoutRoutine routine, Profile profile) {
        return routine != null
                && routine.getNumberOfExercises() >= getMaxExercises(profile);
    }

    /**
     * Returns {sets, reps} for the user's fitness goal: strength 5 x 5,
     * muscle gain 4 x 10, endurance 3 x 15, anything else 3 x 12.
     */
    public int[] getSetsAndReps(Profile profile) {
        String goal = profile == null || profile.getFitnessGoal() == null
                ? ""
                : profile.getFitnessGoal().toLowerCase();
        if (goal.contains("strength")) {
            return new int[] {5, 5};
        }
        if (goal.contains("muscle") || goal.contains("gain")
                || goal.contains("size")) {
            return new int[] {4, 10};
        }
        if (goal.contains("endurance")) {
            return new int[] {3, 15};
        }
        return new int[] {3, 12};
    }

    /**
     * Finds an exercise in the built-in list by name and returns a copy with
     * sets and reps for the user's goal, or null if it is not in the list.
     */
    public Exercise findExercise(String name, Profile profile) {
        if (name == null) {
            return null;
        }
        for (Exercise exercise : exerciseLibrary) {
            if (exercise.getName().equalsIgnoreCase(name.trim())) {
                int[] setsReps = getSetsAndReps(profile);
                return exercise.copy(setsReps[0], setsReps[1]);
            }
        }
        return null;
    }

    /**
     * Returns the properly capitalized muscle group ("Chest"), or null if the
     * input is not one of MUSCLE_GROUPS.
     */
    public static String normalizeMuscleGroup(String group) {
        if (group == null) {
            return null;
        }
        for (String option : MUSCLE_GROUPS) {
            if (option.equalsIgnoreCase(group.trim())) {
                return option;
            }
        }
        return null;
    }

    /** Returns every exercise in the built-in list. */
    public List<Exercise> getExerciseLibrary() {
        return new ArrayList<>(exerciseLibrary);
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private void addToLibrary(String name, String primary, String secondary,
                              String equipment, String... conflicts) {
        Exercise exercise = new Exercise(name, primary, secondary, equipment,
                3, 10);
        for (String conflict : conflicts) {
            exercise.addConflictingLimitation(conflict);
        }
        exerciseLibrary.add(exercise);
    }

    private void addIfFits(WorkoutRoutine routine, Exercise exercise,
                           String muscleGroup, Profile profile) {
        if (exercise.getPrimaryMuscleGroup().equalsIgnoreCase(muscleGroup.trim())
                && !routine.containsExercise(exercise.getName())) {
            int[] setsReps = getSetsAndReps(profile);
            addExercise(routine, exercise.copy(setsReps[0], setsReps[1]),
                    profile);
        }
    }

    private boolean isPreferred(Exercise exercise, Profile profile) {
        for (String liked : profile.getExercisePreferences()) {
            if (liked.equalsIgnoreCase(exercise.getName())) {
                return true;
            }
        }
        return false;
    }
}
