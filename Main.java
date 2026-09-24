import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Main {
	public static void main(String[] args) {
		Profile profile = new Profile();
		profile.setFitnessGoal("Strength");
		profile.setWorkoutDuration(60);
		profile.addEquipment("barbell");
		profile.addEquipment("dumbbells");

		WorkoutPlanner planner = new WorkoutPlanner();
		WorkoutRoutine routine = planner.generateWorkout(profile, "Chest");

		if (routine == null) {
			System.out.println("Please assign fitness goals.");
		} else {
			System.out.println(routine);
		}
	}

	static class Profile {
		private String fitnessGoal;
		private int workoutDuration;
		private final Set<String> limitations = new HashSet<>();
		private final Set<String> equipment = new HashSet<>();

		public void setFitnessGoal(String goal) {
			if (goal != null && !goal.trim().isEmpty()) {
				fitnessGoal = goal.trim();
			}
		}

		public void setWorkoutDuration(int minutes) {
			if (minutes > 0) {
				workoutDuration = minutes;
			}
		}

		public void addLimit(String limit) {
			if (limit != null && !limit.trim().isEmpty()) {
				limitations.add(limit.trim().toLowerCase());
			}
		}

		public void addEquipment(String item) {
			if (item != null && !item.trim().isEmpty()) {
				equipment.add(item.trim().toLowerCase());
			}
		}

		public String getFitnessGoal() {
			return fitnessGoal;
		}

		public int getWorkoutDuration() {
			return workoutDuration;
		}

		public boolean hasEquipment(String item) {
			if (item == null || item.trim().isEmpty()) {
				return false;
			}
			return equipment.contains(item.trim().toLowerCase());
		}

		public boolean hasLimitation(String limitation) {
			if (limitation == null || limitation.trim().isEmpty()) {
				return false;
			}
			return limitations.contains(limitation.trim().toLowerCase());
		}
	}

	static class Exercise {
		private final String name;
		private final String muscleGroup;
		private final String equipment;
		private final String limitation;

		Exercise(String name, String muscleGroup, String equipment,
				 String limitation) {
			this.name = name;
			this.muscleGroup = muscleGroup;
			this.equipment = equipment;
			this.limitation = limitation;
		}

		public String getName() {
			return name;
		}

		public String getMuscleGroup() {
			return muscleGroup;
		}

		public String getEquipment() {
			return equipment;
		}

		public String getLimitation() {
			return limitation;
		}
	}

	static class WorkoutRoutine {
		private final String name;
		private final List<Exercise> exercises = new ArrayList<>();

		WorkoutRoutine(String name) {
			this.name = name;
		}

		public boolean addExercise(Exercise exercise) {
			if (exercise == null || exercise.getName() == null
					|| exercise.getName().trim().isEmpty()
					|| containsExercise(exercise.getName())) {
				return false;
			}

			exercises.add(exercise);
			return true;
		}

		public boolean removeExercise(String exerciseName) {
			Exercise exercise = getExercise(exerciseName);
			return exercise != null && exercises.remove(exercise);
		}

		public boolean containsExercise(String exerciseName) {
			return getExercise(exerciseName) != null;
		}

		public Exercise getExercise(String exerciseName) {
			if (exerciseName == null || exerciseName.trim().isEmpty()) {
				return null;
			}

			for (Exercise exercise : exercises) {
				if (exercise.getName() != null
						&& exercise.getName().equalsIgnoreCase(exerciseName.trim())) {
					return exercise;
				}
			}

			return null;
		}

		public int getNumberOfExercises() {
			return exercises.size();
		}

		public List<Exercise> getExercises() {
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
						.append(exercise.getName())
						.append(" (")
						.append(exercise.getMuscleGroup())
						.append(")\n");
			}

			return result.toString();
		}
	}

	static class WorkoutSchedule {
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

	static class WorkoutPlanner {
		private final List<Exercise> exerciseDatabase = new ArrayList<>();

		WorkoutPlanner() {
			exerciseDatabase.add(new Exercise(
					"Barbell Bench Press", "Chest", "barbell", "shoulder"));
			exerciseDatabase.add(new Exercise(
					"Dumbbell Bench Press", "Chest", "dumbbells", "shoulder"));
			exerciseDatabase.add(new Exercise(
					"Push Ups", "Chest", "none", ""));
			exerciseDatabase.add(new Exercise(
					"Barbell Squat", "Legs", "barbell", "knee"));
			exerciseDatabase.add(new Exercise(
					"Dumbbell Lunges", "Legs", "dumbbells", "knee"));
			exerciseDatabase.add(new Exercise(
					"Pull Ups", "Back", "none", "shoulder"));
			exerciseDatabase.add(new Exercise(
					"Barbell Row", "Back", "barbell", "lower back"));
		}

		public WorkoutRoutine generateWorkout(Profile profile,
											   String muscleGroup) {
			if (profile == null
					|| profile.getFitnessGoal() == null
					|| muscleGroup == null
					|| muscleGroup.trim().isEmpty()) {
				return null;
			}

			WorkoutRoutine routine =
					new WorkoutRoutine(muscleGroup + " Workout");

			for (Exercise exercise : exerciseDatabase) {
				if (exercise.getMuscleGroup().equalsIgnoreCase(muscleGroup)
						&& isExerciseValid(exercise, profile)) {
					addExercise(routine, exercise, profile);

					if (routine.getNumberOfExercises()
							>= recommendedExerciseCount(profile)) {
						break;
					}
				}
			}

			return routine;
		}

		public boolean addExercise(WorkoutRoutine routine,
								   Exercise exercise,
								   Profile profile) {
			if (routine == null || exercise == null
					|| !isExerciseValid(exercise, profile)
					|| routine.getNumberOfExercises()
					>= recommendedExerciseCount(profile)) {
				return false;
			}

			return routine.addExercise(exercise);
		}

		public boolean removeExercise(WorkoutRoutine routine,
									  String exerciseName) {
			return routine != null && routine.removeExercise(exerciseName);
		}

		public boolean changeExercise(WorkoutRoutine routine,
									  String oldExercise,
									  Exercise newExercise,
									  Profile profile) {
			if (routine == null || oldExercise == null || newExercise == null
					|| !routine.containsExercise(oldExercise)
					|| routine.containsExercise(newExercise.getName())
					|| !isExerciseValid(newExercise, profile)) {
				return false;
			}

			routine.removeExercise(oldExercise);
			return routine.addExercise(newExercise);
		}

		public boolean isExerciseValid(Exercise exercise, Profile profile) {
			if (exercise == null || profile == null) {
				return false;
			}

			String equipment = exercise.getEquipment();
			String limitation = exercise.getLimitation();

			boolean equipmentAvailable =
					equipment != null
					&& (equipment.equalsIgnoreCase("none")
					|| profile.hasEquipment(equipment));
			boolean limitationConflict =
					limitation != null
					&& !limitation.trim().isEmpty()
					&& profile.hasLimitation(limitation);

			return equipmentAvailable && !limitationConflict;
		}

		private int recommendedExerciseCount(Profile profile) {
			if (profile == null || profile.getWorkoutDuration() <= 30) {
				return 3;
			}

			if (profile.getWorkoutDuration() <= 60) {
				return 5;
			}

			return 6;
		}
	}
}


