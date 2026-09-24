import java.util.LinkedHashMap;
import java.util.Map;

/**
 * WorkoutSchedule: which days are workout days, the muscle group for each,
 * and the routine assigned to each. Any day not in the schedule is a rest day.
 */
public class WorkoutSchedule {
    private final Map<String, String> muscleGroups = new LinkedHashMap<>();
    private final Map<String, WorkoutRoutine> routines = new LinkedHashMap<>();

    public boolean addWorkoutDay(String day, String muscleGroup) {
        String normalizedDay = normalizeDay(day);
        if (normalizedDay == null || muscleGroup == null
                || muscleGroup.trim().isEmpty()
                || muscleGroups.containsKey(normalizedDay)) {
            return false;
        }

        muscleGroups.put(normalizedDay, muscleGroup.trim());
        return true;
    }

    public boolean removeWorkoutDay(String day) {
        String normalizedDay = normalizeDay(day);
        if (normalizedDay == null || !muscleGroups.containsKey(normalizedDay)) {
            return false;
        }

        muscleGroups.remove(normalizedDay);
        routines.remove(normalizedDay);
        return true;
    }

    public boolean changeWorkoutDay(String oldDay, String newDay) {
        String oldKey = normalizeDay(oldDay);
        String newKey = normalizeDay(newDay);
        if (oldKey == null || newKey == null || !muscleGroups.containsKey(oldKey)
                || muscleGroups.containsKey(newKey)) {
            return false;
        }

        String muscleGroup = muscleGroups.remove(oldKey);
        muscleGroups.put(newKey, muscleGroup);
        WorkoutRoutine routine = routines.remove(oldKey);
        if (routine != null) {
            routines.put(newKey, routine);
        }
        return true;
    }

    public boolean isWorkoutDay(String day) {
        String normalizedDay = normalizeDay(day);
        return normalizedDay != null && muscleGroups.containsKey(normalizedDay);
    }

    public String getMuscleGroup(String day) {
        String normalizedDay = normalizeDay(day);
        return normalizedDay == null ? null : muscleGroups.get(normalizedDay);
    }

    public boolean assignRoutine(String day, WorkoutRoutine routine) {
        String normalizedDay = normalizeDay(day);
        if (normalizedDay == null || routine == null
                || !muscleGroups.containsKey(normalizedDay)) {
            return false;
        }

        routines.put(normalizedDay, routine);
        return true;
    }

    public WorkoutRoutine getRoutine(String day) {
        String normalizedDay = normalizeDay(day);
        return normalizedDay == null ? null : routines.get(normalizedDay);
    }

    private String normalizeDay(String day) {
        if (day == null || day.trim().isEmpty()) {
            return null;
        }

        String value = day.trim().toLowerCase();
        String[] validDays = {"monday", "tuesday", "wednesday", "thursday",
                "friday", "saturday", "sunday"};
        for (String validDay : validDays) {
            if (validDay.equals(value)) {
                return validDay;
            }
        }
        return null;
    }
}
