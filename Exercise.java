import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A single exercise (for example Bench Press, 4 x 8). Stores the muscle
 * groups it works, the equipment it needs, its sets and reps, and the
 * limitations/injuries that make it unsafe.
 */
public class Exercise {

    /**
     * Value returned when an exercise has no secondary muscle group or
     * needs no equipment.
     */
    public static final String NONE = "None";

    /** Most sets allowed for one exercise. */
    public static final int MAX_SETS = 10;

    /** Most reps allowed per set. */
    public static final int MAX_REPS = 50;

    /** Longest name, muscle group or equipment text allowed. */
    public static final int MAX_TEXT_LENGTH = 50;

    private String name;
    private String primaryMuscleGroup;
    private String secondaryMuscleGroup;
    private String requiredEquipment;
    private int sets;
    private int reps;
    private Set<String> conflictingLimitations;

    /**
     * Creates a new exercise.
     *
     * @param name
     *            name of the exercise, cannot be null or empty
     * @param primaryMuscleGroup
     *            main muscle group worked, cannot be null or empty
     * @param secondaryMuscleGroup
     *            secondary muscle group worked, null or empty means none
     * @param requiredEquipment
     *            equipment needed, null or empty means none (bodyweight)
     * @param sets
     *            number of sets, from 1 to MAX_SETS
     * @param reps
     *            number of reps per set, from 1 to MAX_REPS
     * @throws IllegalArgumentException
     *             if the name or primary muscle group is empty, any text is
     *             longer than MAX_TEXT_LENGTH, or sets/reps are out of range
     */
    public Exercise(
        String name,
        String primaryMuscleGroup,
        String secondaryMuscleGroup,
        String requiredEquipment,
        int sets,
        int reps) {
        if (isBlank(name)) {
            throw new IllegalArgumentException(
                "Exercise name cannot be empty");
        }
        if (isBlank(primaryMuscleGroup)) {
            throw new IllegalArgumentException(
                "Primary muscle group cannot be empty");
        }
        checkLength(name, "Exercise name");
        checkLength(primaryMuscleGroup, "Primary muscle group");
        checkLength(secondaryMuscleGroup, "Secondary muscle group");
        checkLength(requiredEquipment, "Equipment");
        this.name = name.trim();
        this.primaryMuscleGroup = primaryMuscleGroup.trim();
        this.secondaryMuscleGroup = isBlank(secondaryMuscleGroup)
            ? NONE
            : secondaryMuscleGroup.trim();
        this.requiredEquipment = isBlank(requiredEquipment)
            ? NONE
            : requiredEquipment.trim();
        setSets(sets);
        setReps(reps);
        conflictingLimitations = new LinkedHashSet<String>();
    }


    /**
     * Returns the name of the exercise.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }


    /**
     * Returns the main muscle group targeted by the exercise.
     *
     * @return the primary muscle group
     */
    public String getPrimaryMuscleGroup() {
        return primaryMuscleGroup;
    }


    /**
     * Returns the secondary muscle group targeted by the exercise.
     *
     * @return the secondary muscle group, or "None" if there is none
     */
    public String getSecondaryMuscleGroup() {
        return secondaryMuscleGroup;
    }


    /**
     * Returns the equipment required for the exercise.
     *
     * @return the equipment, or "None" if no equipment is needed
     */
    public String getRequiredEquipment() {
        return requiredEquipment;
    }


    /**
     * Returns the number of sets.
     *
     * @return the sets
     */
    public int getSets() {
        return sets;
    }


    /**
     * Returns the number of reps per set.
     *
     * @return the reps
     */
    public int getReps() {
        return reps;
    }


    /**
     * Changes the number of sets.
     *
     * @param sets
     *            the new number of sets, from 1 to MAX_SETS
     * @throws IllegalArgumentException
     *             if sets is out of range
     */
    public void setSets(int sets) {
        if (sets < 1 || sets > MAX_SETS) {
            throw new IllegalArgumentException(
                "Sets must be between 1 and " + MAX_SETS);
        }
        this.sets = sets;
    }


    /**
     * Changes the number of reps per set.
     *
     * @param reps
     *            the new number of reps, from 1 to MAX_REPS
     * @throws IllegalArgumentException
     *             if reps is out of range
     */
    public void setReps(int reps) {
        if (reps < 1 || reps > MAX_REPS) {
            throw new IllegalArgumentException(
                "Reps must be between 1 and " + MAX_REPS);
        }
        this.reps = reps;
    }


    /**
     * Marks a limitation or injury (for example "knee") that makes this
     * exercise unsafe. A user limitation conflicts if it contains this word,
     * ignoring case, so "knee" matches "Knee injury".
     *
     * @param limitation
     *            the limitation keyword
     * @return true if it was added, false if it was empty or already there
     */
    public boolean addConflictingLimitation(String limitation) {
        if (isBlank(limitation)) {
            return false;
        }
        return conflictingLimitations.add(limitation.trim().toLowerCase());
    }


    /**
     * Returns the limitations that make this exercise unsafe.
     *
     * @return a read-only set of limitation keywords (lower case)
     */
    public Set<String> getConflictingLimitations() {
        return Collections.unmodifiableSet(conflictingLimitations);
    }


    /**
     * Returns whether the user can perform the exercise based on their
     * equipment and limitations.
     *
     * @param profile
     *            the user's profile
     * @return true if the user has the equipment and no conflicting
     *         limitation, false otherwise (including a null profile)
     */
    public boolean canPerformWith(Profile profile) {
        return hasEquipmentFor(profile) && isSafeFor(profile);
    }


    /**
     * Returns whether the user has the equipment this exercise needs.
     * Matching ignores case and allows singular/plural, so "Dumbbell"
     * matches "Dumbbells".
     *
     * @param profile
     *            the user's profile
     * @return true if no equipment is needed or the user has it
     */
    public boolean hasEquipmentFor(Profile profile) {
        if (requiredEquipment.equals(NONE)) {
            return true;
        }
        if (profile == null || profile.getEquipment() == null) {
            return false;
        }
        String needed = requiredEquipment.toLowerCase();
        for (String item : profile.getEquipment()) {
            if (item == null || item.trim().isEmpty()) {
                continue;
            }
            String have = item.trim().toLowerCase();
            if (have.contains(needed) || needed.contains(have)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Returns whether none of the user's limitations make this exercise
     * unsafe. A limitation conflicts if it contains one of this exercise's
     * conflicting keywords, ignoring case ("Knee injury" contains "knee").
     *
     * @param profile
     *            the user's profile
     * @return true if the exercise is safe, false if a limitation conflicts
     *         or the profile is null
     */
    public boolean isSafeFor(Profile profile) {
        if (profile == null) {
            return false;
        }
        if (profile.getLimitations() == null) {
            return true;
        }
        for (String limit : profile.getLimitations()) {
            if (limit == null) {
                continue;
            }
            String lower = limit.toLowerCase();
            for (String conflict : conflictingLimitations) {
                if (lower.contains(conflict)) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Returns a new exercise with the same data but its own sets and reps,
     * so changing the copy does not change the original.
     *
     * @param newSets
     *            sets for the copy, from 1 to MAX_SETS
     * @param newReps
     *            reps for the copy, from 1 to MAX_REPS
     * @return the copy
     */
    public Exercise copy(int newSets, int newReps) {
        Exercise copy = new Exercise(name, primaryMuscleGroup,
            secondaryMuscleGroup, requiredEquipment, newSets, newReps);
        copy.conflictingLimitations.addAll(conflictingLimitations);
        return copy;
    }


    /**
     * Throws if the text is longer than MAX_TEXT_LENGTH once trimmed.
     */
    private static void checkLength(String s, String label) {
        if (s != null && s.trim().length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException(
                label + " must be " + MAX_TEXT_LENGTH + " characters or fewer");
        }
    }


    /**
     * Checks if a string is null or only whitespace.
     */
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }


    /**
     * Two exercises are equal if they have the same name, ignoring case.
     *
     * @param obj
     *            the object to compare to
     * @return true if equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Exercise other = (Exercise)obj;
        return name.equalsIgnoreCase(other.name);
    }


    /**
     * Returns a hash code based on the name, ignoring case.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }


    /**
     * Returns the exercise as text, for example "Bench Press 4 x 8".
     *
     * @return the text form
     */
    @Override
    public String toString() {
        return name + " " + sets + " x " + reps;
    }
}
