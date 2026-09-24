import java.util.ArrayList;

/**
 * WorkoutRoutine: stores the exercises for one workout session, in the order
 * they are done. Each Exercise already holds its own sets and reps.
 */
public class WorkoutRoutine {
    private final String name;
    private final ArrayList<Exercise> exercises = new ArrayList<>();

    /** Creates an empty routine with the given session name (e.g. "Push Day"). */
    public WorkoutRoutine(String name) {
        this.name = name;
    }

    /** Returns the session name. */
    public String getName() {
        return name;
    }

    /** Adds an exercise to the workout routine if it is not already included. */
    public boolean addExercise(Exercise exercise) {
        if (exercise == null || containsExercise(exercise.getName())) {
            return false;
        }

        exercises.add(exercise);
        return true;
    }

    /** Removes the specified exercise from the workout routine. */
    public boolean removeExercise(String exerciseName) {
        Exercise exercise = getExercise(exerciseName);
        return exercise != null && exercises.remove(exercise);
    }

    /**
     * Replaces the exercise with the given name by a new one, keeping its
     * place in the routine. Returns false if the old one is missing or the
     * new one is already in the routine.
     */
    public boolean replaceExercise(String oldName, Exercise newExercise) {
        Exercise old = getExercise(oldName);
        if (old == null || newExercise == null
                || (containsExercise(newExercise.getName())
                    && !old.equals(newExercise))) {
            return false;
        }
        exercises.set(exercises.indexOf(old), newExercise);
        return true;
    }

    /** Returns whether the workout routine contains the specified exercise. */
    public boolean containsExercise(String exerciseName) {
        return getExercise(exerciseName) != null;
    }

    /** Returns the exercise with the specified name, or null if not found. */
    public Exercise getExercise(String exerciseName) {
        if (exerciseName == null || exerciseName.trim().isEmpty()) {
            return null;
        }

        for (Exercise exercise : exercises) {
            if (exercise.getName().equalsIgnoreCase(exerciseName.trim())) {
                return exercise;
            }
        }

        return null;
    }

    /** Returns the number of exercises currently in the workout routine. */
    public int getNumberOfExercises() {
        return exercises.size();
    }

    /** Returns all exercises currently stored in the workout routine. */
    public ArrayList<Exercise> getExercises() {
        return new ArrayList<>(exercises);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(name).append("\n");

        if (exercises.isEmpty()) {
            return result.append("No exercises in this routine.").toString();
        }

        for (Exercise exercise : exercises) {
            result.append("- ")
                    .append(exercise)
                    .append(" (")
                    .append(exercise.getPrimaryMuscleGroup())
                    .append(")\n");
        }

        return result.toString();
    }
}
