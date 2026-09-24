import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		new ConsoleUI().start();
	}

	static class Profile {
		private String fitnessGoal;
		private int workoutDuration;
		private final Set<String> limitations = new HashSet<>();
		private final Set<String> equipment = new HashSet<>();
		private final Set<String> preferredWorkoutDays = new HashSet<>();
		private final List<String> exercisePreferences = new ArrayList<>();

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

		public boolean addPreferredWorkoutDay(String day) {
			String normalizedDay = normalizeDay(day);
			if (normalizedDay == null) {
				return false;
			}
			return preferredWorkoutDays.add(normalizedDay);
		}

		public boolean hasPreferredWorkoutDay(String day) {
			String normalizedDay = normalizeDay(day);
			return normalizedDay != null && preferredWorkoutDays.contains(normalizedDay);
		}

		public boolean addExercisePreference(String exercise) {
			if (exercise == null || exercise.trim().isEmpty()) {
				return false;
			}
			exercisePreferences.add(exercise.trim());
			return true;
		}

		public boolean hasExercisePreference(String exercise) {
			if (exercise == null || exercise.trim().isEmpty()) {
				return false;
			}
			for (String preference : exercisePreferences) {
				if (preference.equalsIgnoreCase(exercise.trim())) {
					return true;
				}
			}
			return false;
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

	static class Exercise {
		private final String name;
		private final String muscleGroup;
		private final String equipment;
		private final String limitation;
		private final int sets;
		private final int reps;

		Exercise(String name, String muscleGroup, String equipment,
				 String limitation) {
			this(name, muscleGroup, equipment, limitation, 3, 10);
		}

		Exercise(String name, String muscleGroup, String equipment,
				 String limitation, int sets, int reps) {
			this.name = name;
			this.muscleGroup = muscleGroup;
			this.equipment = equipment;
			this.limitation = limitation;
			this.sets = sets;
			this.reps = reps;
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

		public int getSets() {
			return sets;
		}

		public int getReps() {
			return reps;
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
						.append(", ")
						.append(exercise.getSets())
						.append(" sets x ")
						.append(exercise.getReps())
						.append(" reps")
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

		public String toString() {
			if (muscleGroups.isEmpty()) {
				return "No workouts scheduled.";
			}

			StringBuilder result = new StringBuilder();
			String[] days = {"monday", "tuesday", "wednesday", "thursday",
					"friday", "saturday", "sunday"};
			StringBuilder restDays = new StringBuilder("Rest days: ");
			for (String day : days) {
				if (muscleGroups.containsKey(day)) {
					result.append(capitalize(day)).append(": ")
							.append(muscleGroups.get(day));
					WorkoutRoutine routine = routines.get(day);
					if (routine != null) {
						result.append("\n").append(routine);
					} else {
						result.append(" (routine not assigned)");
					}
					result.append("\n");
				} else {
					if (restDays.length() > "Rest days: ".length()) {
						restDays.append(", ");
					}
					restDays.append(capitalize(day));
				}
			}
			return result.append(restDays).toString().trim();
		}

		private String capitalize(String day) {
			return Character.toUpperCase(day.charAt(0)) + day.substring(1);
		}
	}

	static class ConsoleUI {
		private final Scanner scanner;

		ConsoleUI() {
			this(new Scanner(System.in));
		}

		ConsoleUI(Scanner scanner) {
			this.scanner = scanner;
		}

		public void start() {
			System.out.println("NPNG Workout Planner");
			Profile profile = new Profile();
			profile.setFitnessGoal(readNonEmpty("Fitness goal: "));
			profile.setWorkoutDuration(readPositiveInt("Workout duration in minutes: "));
			int workoutDays = readNumberInRange("Number of workout days (1-7): ", 1, 7);

			for (int index = 0; index < workoutDays; index++) {
				String day = readDay("Workout day " + (index + 1) + ": ");
				while (!profile.addPreferredWorkoutDay(day)) {
					day = readDay("Choose a different valid workout day: ");
				}
			}

			String equipment = readNonEmpty(
					"Available equipment, separated by commas (or none): ");
			for (String item : equipment.split(",")) {
				if (!item.trim().equalsIgnoreCase("none")) {
					profile.addEquipment(item);
				}
			}
			String limitations = readOptional(
					"Limitations or injuries, separated by commas (or Enter for none): ");
			for (String limitation : limitations.split(",")) {
				profile.addLimit(limitation);
			}
			String preference = readOptional(
					"Preferred exercise (or Enter to skip): ");
			if (!preference.isEmpty()) {
				profile.addExercisePreference(preference);
			}
			String muscleGroup = readNonEmpty("Muscle group for this plan: ");
			if (profile.getFitnessGoal() == null) {
				System.out.println("Please assign a fitness goal first.");
				return;
			}

			WorkoutPlanner planner = new WorkoutPlanner();
			String customName = readOptional(
					"Custom exercise name (press Enter to skip): ");
			if (!customName.isEmpty()) {
				String customGroup = readNonEmpty("Custom exercise muscle group: ");
				String customEquipment = readNonEmpty(
						"Custom exercise equipment (or none): ");
				String customLimitation = readOptional(
						"Custom exercise limitation (or press Enter for none): ");
				int customSets = readPositiveInt("Custom exercise sets: ");
				int customReps = readPositiveInt("Custom exercise reps: ");
				planner.addCustomExercise(new Exercise(customName, customGroup,
						customEquipment, customLimitation, customSets, customReps));
			}

			WorkoutSchedule schedule = new WorkoutSchedule();
			for (String day : new String[] {"monday", "tuesday", "wednesday", "thursday",
					"friday", "saturday", "sunday"}) {
				if (profile.hasPreferredWorkoutDay(day)) {
					WorkoutRoutine routine = planner.generateWorkout(profile, muscleGroup);
					schedule.addWorkoutDay(day, muscleGroup);
					if (routine != null) {
						schedule.assignRoutine(day, routine);
					}
				}
			}
			displaySchedule(schedule);
			editRoutineMenu(schedule, planner, profile);
		}

		public String readFitnessGoal() {
			return readNonEmpty("Fitness goal: ");
		}

		public int readWorkoutDays() {
			return readNumberInRange("Number of workout days (1-7): ", 1, 7);
		}

		public int readWorkoutDuration() {
			return readPositiveInt("Workout duration in minutes: ");
		}

		public String readWorkoutDay() {
			return readDay("Workout day: ");
		}

		public String readEquipment() {
			return readNonEmpty("Available equipment: ");
		}

		public void displayRoutine(WorkoutRoutine routine) {
			System.out.println(routine == null ? "No exercises in this routine." : routine);
		}

		public void displaySchedule(WorkoutSchedule schedule) {
			System.out.println(schedule == null ? "No workouts scheduled." : schedule);
		}

		private void editRoutineMenu(WorkoutSchedule schedule,
				WorkoutPlanner planner, Profile profile) {
			String answer = readOptional(
					"Edit a routine? Enter yes to continue, or press Enter to finish: ");
			while (answer.equalsIgnoreCase("yes")) {
				String day = readDay("Workout day to edit: ");
				WorkoutRoutine routine = schedule.getRoutine(day);
				if (routine == null) {
					System.out.println("That day is a rest day or has no routine.");
				} else {
					String action = readOptional(
							"Choose action: add, remove, or change: ");
					if (action.equalsIgnoreCase("add")) {
						Exercise exercise = readExercise();
						if (!planner.addExercise(routine, exercise, profile)) {
							System.out.println("Exercise cannot be added. Check equipment, "
									+ "limitations, or the recommended amount.");
						} else {
							System.out.println("Exercise added.");
						}
					} else if (action.equalsIgnoreCase("remove")) {
						String exerciseName = readNonEmpty("Exercise to remove: ");
						if (planner.removeExercise(routine, exerciseName)) {
							System.out.println("Exercise removed.");
						} else {
							System.out.println("That exercise is not in the routine.");
						}
					} else if (action.equalsIgnoreCase("change")) {
						String oldExercise = readNonEmpty("Exercise to replace: ");
						Exercise replacement = readExercise();
						if (planner.changeExercise(routine, oldExercise,
								replacement, profile)) {
							System.out.println("Exercise changed.");
						} else {
							System.out.println("Exercise cannot be changed. Check equipment, "
									+ "limitations, or duplicate names.");
						}
					} else {
						System.out.println("Choose add, remove, or change.");
					}
				}
				answer = readOptional("Edit another routine? Enter yes or press Enter: ");
			}
		}

		private Exercise readExercise() {
			String name = readNonEmpty("Exercise name: ");
			String muscleGroup = readNonEmpty("Exercise muscle group: ");
			String equipment = readNonEmpty("Exercise equipment (or none): ");
			String limitation = readOptional("Exercise limitation (or Enter for none): ");
			int sets = readPositiveInt("Exercise sets: ");
			int reps = readPositiveInt("Exercise reps: ");
			return new Exercise(name, muscleGroup, equipment, limitation, sets, reps);
		}

		private String readNonEmpty(String prompt) {
			while (true) {
				System.out.print(prompt);
				String value = scanner.nextLine().trim();
				if (!value.isEmpty()) {
					return value;
				}
				System.out.println("Please enter a value.");
			}
		}

		private String readOptional(String prompt) {
			System.out.print(prompt);
			return scanner.hasNextLine() ? scanner.nextLine().trim() : "";
		}

		private int readPositiveInt(String prompt) {
			while (true) {
				System.out.print(prompt);
				try {
					int value = Integer.parseInt(scanner.nextLine().trim());
					if (value > 0) {
						return value;
					}
				} catch (NumberFormatException exception) {
					// Ask again below.
				}
				System.out.println("Response should be a positive number.");
			}
		}

		private int readNumberInRange(String prompt, int minimum, int maximum) {
			while (true) {
				System.out.print(prompt);
				try {
					int value = Integer.parseInt(scanner.nextLine().trim());
					if (value >= minimum && value <= maximum) {
						return value;
					}
				} catch (NumberFormatException exception) {
					// Ask again below.
				}
				System.out.println("Response should be a number between "
						+ minimum + " and " + maximum + ".");
			}
		}

		private String readDay(String prompt) {
			while (true) {
				String day = readNonEmpty(prompt).toLowerCase();
				if (isDay(day)) {
					return day;
				}
				System.out.println("Please enter a valid day of the week.");
			}
		}

		private boolean isDay(String day) {
			return day.equals("monday") || day.equals("tuesday")
					|| day.equals("wednesday") || day.equals("thursday")
					|| day.equals("friday") || day.equals("saturday")
					|| day.equals("sunday");
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

		public boolean addCustomExercise(Exercise exercise) {
			if (exercise == null || exercise.getName() == null
					|| exercise.getName().trim().isEmpty()) {
				return false;
			}
			for (Exercise existing : exerciseDatabase) {
				if (existing.getName().equalsIgnoreCase(exercise.getName().trim())) {
					return false;
				}
			}
			return exerciseDatabase.add(exercise);
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

			for (int preferencePass = 0; preferencePass < 2; preferencePass++) {
				for (Exercise exercise : exerciseDatabase) {
					boolean preferred = profile.hasExercisePreference(exercise.getName());
					if (exercise.getMuscleGroup().equalsIgnoreCase(muscleGroup)
							&& preferred == (preferencePass == 0)
							&& isExerciseValid(exercise, profile)) {
						addExercise(routine, exercise, profile);

						if (routine.getNumberOfExercises()
								>= recommendedExerciseCount(profile)) {
							return routine;
						}
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


