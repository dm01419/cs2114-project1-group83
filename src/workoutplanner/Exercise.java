package workoutplanner;

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
     *            number of sets, must be positive
     * @param reps
     *            number of reps per set, must be positive
     * @throws IllegalArgumentException
     *             if the name or primary muscle group is empty, or sets/reps
     *             are not positive
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
     *            the new number of sets, must be positive
     * @throws IllegalArgumentException
     *             if sets is not positive
     */
    public void setSets(int sets) {
        if (sets <= 0) {
            throw new IllegalArgumentException("Sets must be positive");
        }
        this.sets = sets;
    }


    /**
     * Changes the number of reps per set.
     *
     * @param reps
     *            the new number of reps, must be positive
     * @throws IllegalArgumentException
     *             if reps is not positive
     */
    public void setReps(int reps) {
        if (reps <= 0) {
            throw new IllegalArgumentException("Reps must be positive");
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
        if (profile == null) {
            return false;
        }
        return hasEquipment(profile.getEquipment())
            && !hasConflict(profile.getLimitations());
    }


    /**
     * Checks whether the needed equipment is in the given list.
     */
    private boolean hasEquipment(Iterable<String> equipment) {
        if (requiredEquipment.equals(NONE)) {
            return true;
        }
        if (equipment == null) {
            return false;
        }
        for (String item : equipment) {
            if (item != null && item.trim().equalsIgnoreCase(
                requiredEquipment)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks whether any of the given limitations conflict with this
     * exercise.
     */
    private boolean hasConflict(Iterable<String> limitations) {
        if (limitations == null) {
            return false;
        }
        for (String limit : limitations) {
            if (limit == null) {
                continue;
            }
            String lower = limit.toLowerCase();
            for (String conflict : conflictingLimitations) {
                if (lower.contains(conflict)) {
                    return true;
                }
            }
        }
        return false;
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
