import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Scanner;
import java.util.NoSuchElementException;

public class Main {
	public static void main(String[] args) {
		new ConsoleUI().start();
	}

	// ponytail: fixed demo presets; add experience/load progression when those inputs exist.
	enum FitnessGoal {
		STRENGTH("Strength", 3, 6, 180, "barbell"),
		MUSCLE_GAIN("Muscle Gain", 3, 10, 120, "dumbbells"),
		ENDURANCE("Endurance", 2, 16, 60, "none"),
		WEIGHT_LOSS("Weight Loss", 3, 12, 90, "none");

		final String label;
		final int sets, reps, restSeconds;
		final String preferredEquipment;

		FitnessGoal(String label, int sets, int reps, int restSeconds,
				String preferredEquipment) {
			this.label = label;
			this.sets = sets;
			this.reps = reps;
			this.restSeconds = restSeconds;
			this.preferredEquipment = preferredEquipment;
		}

		static FitnessGoal parse(String value) {
			for (FitnessGoal goal : values()) {
				if (value != null && goal.label.equalsIgnoreCase(value.trim())) {
					return goal;
				}
			}
			return null;
		}
	}

	static String normalizeMuscleGroup(String value) {
		if (value == null) {
			return null;
		}
		return switch (value.trim().toLowerCase(Locale.ROOT)) {
			case "shoulder", "shoulders" -> "Shoulders";
			case "arm", "arms" -> "Arms";
			case "leg", "legs" -> "Legs";
			case "back" -> "Back";
			case "chest" -> "Chest";
			case "full body", "full-body" -> "Full Body";
			default -> value.trim();
		};
	}

	static class Profile {
		private String fitnessGoal;
		private int workoutDuration;
		private final Set<String> limitations = new HashSet<>();
		private final Set<String> equipment = new HashSet<>();
		private final Set<String> preferredWorkoutDays = new HashSet<>();
		private final List<String> exercisePreferences = new ArrayList<>();

		public void setFitnessGoal(String goal) {
			FitnessGoal parsed = FitnessGoal.parse(goal);
			fitnessGoal = parsed == null ? null : parsed.label;
		}

		public void setWorkoutDuration(int minutes) {
			if (minutes > 0) {
				workoutDuration = minutes;
			}
		}

		public void addLimit(String limit) {
			if (limit != null && !limit.trim().isEmpty()) {
				limitations.add(limit.trim().toLowerCase(Locale.ROOT));
			}
		}

		public void addEquipment(String item) {
			if (item != null && !item.trim().isEmpty()) {
				equipment.add(item.trim().toLowerCase(Locale.ROOT));
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
			return equipment.contains(item.trim().toLowerCase(Locale.ROOT));
		}

		public boolean hasLimitation(String limitation) {
			if (limitation == null || limitation.trim().isEmpty()) {
				return false;
			}
			return limitations.contains(limitation.trim().toLowerCase(Locale.ROOT));
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
			String value = day.trim().toLowerCase(Locale.ROOT);
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
		private int maxReps;
		private int restSeconds;
		private boolean goalBased;

		Exercise(String name, String muscleGroup, String equipment,
				 String limitation) {
			this(name, muscleGroup, equipment, limitation, 3, 10);
			goalBased = true;
		}

		Exercise(String name, String muscleGroup, String equipment,
				 String limitation, int sets, int reps) {
			this.name = name == null ? null : name.trim();
			this.muscleGroup = normalizeMuscleGroup(muscleGroup);
			this.equipment = equipment == null ? null : equipment.trim();
			this.limitation = limitation;
			this.sets = sets;
			this.reps = reps;
			this.maxReps = reps;
		}

		boolean hasValidDefinition() {
			if (name == null || name.isBlank() || muscleGroup == null || muscleGroup.isBlank()
					|| equipment == null || equipment.isBlank() || sets <= 0 || reps <= 0 || maxReps < reps) {
				return false;
			}
			String[] items = equipment.split(",", -1);
			for (String item : items) {
				if (item.isBlank() || (items.length > 1 && item.trim().equalsIgnoreCase("none"))) return false;
			}
			return true;
		}

		public Map<String, Integer> getTargetMusclePercentages() {
			// App classification weights, not measured muscle activation or medical guidance.
			if (name == null) {
				return Map.of();
			}
			return switch (name.toLowerCase(Locale.ROOT)) {
				case "barbell bench press", "dumbbell bench press", "push ups", "wide push ups",
						"dumbbell floor press", "barbell floor press", "standing band chest press" ->
						Map.of("Chest", 60, "Triceps", 25, "Shoulders", 15);
				case "wall push ups", "knee push ups" -> Map.of("Chest", 65, "Triceps", 20, "Shoulders", 15);
				case "incline dumbbell press" -> Map.of("Chest", 55, "Shoulders", 25, "Triceps", 20);
				case "dumbbell squeeze press" -> Map.of("Chest", 70, "Triceps", 20, "Shoulders", 10);
				case "close grip push ups", "diamond push ups" -> Map.of("Triceps", 50, "Chest", 35, "Shoulders", 15);
				case "barbell squat", "bodyweight squat", "dumbbell goblet squat" ->
						Map.of("Quads", 50, "Glutes", 30, "Hamstrings", 10, "Core", 10);
				case "dumbbell lunges", "reverse lunges", "split squat" ->
						Map.of("Quads", 45, "Glutes", 40, "Hamstrings", 10, "Core", 5);
				case "glute bridge" -> Map.of("Glutes", 75, "Hamstrings", 20, "Core", 5);
				case "standing calf raises" -> Map.of("Calves", 100);
				case "side lying leg raises" -> Map.of("Hip Abductors", 100);
				case "dumbbell romanian deadlift" -> Map.of("Hamstrings", 50, "Glutes", 35, "Lower Back", 15);
				case "pull ups", "lat pulldown" -> Map.of("Lats", 65, "Biceps", 25, "Upper Back", 10);
				case "barbell row", "dumbbell bent over row", "seated band row", "chest supported row" ->
						Map.of("Lats", 40, "Upper Back", 35, "Biceps", 25);
				case "prone w raises", "prone y raises", "reverse snow angels", "band pull apart" ->
						Map.of("Upper Back", 60, "Rear Delts", 40);
				case "bird dog" -> Map.of("Core", 50, "Lower Back", 25, "Glutes", 25);
				case "dumbbell reverse fly", "dumbbell rear delt raise" -> Map.of("Rear Delts", 70, "Upper Back", 30);
				case "dumbbell shoulder press", "barbell overhead press", "band overhead press", "pike push ups" ->
						Map.of("Shoulders", 70, "Triceps", 25, "Upper Chest", 5);
				case "dumbbell lateral raise", "band lateral raise" -> Map.of("Side Delts", 90, "Upper Traps", 10);
				case "dumbbell front raise" -> Map.of("Front Delts", 90, "Upper Chest", 10);
				case "dumbbell biceps curl", "barbell biceps curl", "band biceps curl", "incline dumbbell curl" ->
						Map.of("Biceps", 85, "Forearms", 15);
				case "dumbbell hammer curl" -> Map.of("Biceps", 50, "Brachialis", 30, "Forearms", 20);
				case "dumbbell triceps kickback", "dumbbell overhead triceps extension",
						"band overhead triceps extension", "triceps pushdown" -> Map.of("Triceps", 100);
				default -> muscleGroup == null || muscleGroup.isBlank() ? Map.of() : Map.of(muscleGroup, 100);
			};
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
		private final boolean recovery;

		WorkoutRoutine(String name) {
			this(name, false);
		}

		WorkoutRoutine(String name, boolean recovery) {
			this.name = name;
			this.recovery = recovery;
		}

		public boolean addExercise(Exercise exercise) {
			if (recovery || exercise == null || !exercise.hasValidDefinition()
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

		long estimatedSeconds() {
			if (exercises.isEmpty()) return 0;
			long sets = exercises.stream().mapToLong(Exercise::getSets).sum();
			return 300 + sets * 210 + Math.max(0, exercises.size() - 1) * 60L;
		}

		@Override
		public String toString() {
			StringBuilder result = new StringBuilder(name).append("\n");

			if (exercises.isEmpty()) {
				return recovery ? name : result.append("No matching exercises fit the equipment, limitations, and time budget.").toString();
			}

			for (Exercise exercise : exercises) {
				result.append("- ")
						.append(exercise.getName())
						.append(" (")
						.append(exercise.getMuscleGroup())
						.append(", ")
						.append(exercise.getSets())
						.append(" sets x ")
						.append(exercise.getReps());
				if (exercise.maxReps != exercise.reps) {
					result.append("-").append(exercise.maxReps);
				}
				result.append(" reps");
				if (exercise.restSeconds > 0) {
					result.append(", rest ").append(exercise.restSeconds).append(" sec");
				}
				result.append(")\n  Target mix (estimated): ");
				List<String> targets = new ArrayList<>();
				exercise.getTargetMusclePercentages().entrySet().stream()
						.sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
								.thenComparing(Map.Entry.comparingByKey()))
						.forEach(entry -> targets.add(entry.getKey() + " " + entry.getValue() + "%"));
				result.append(String.join(", ", targets)).append("\n");
			}

			long totalSets = exercises.stream().mapToLong(Exercise::getSets).sum();
			return result.append("Total: ").append(exercises.size()).append(" exercises, ")
					.append(totalSets).append(" working sets\n")
					.append("Estimated session time: ")
					.append(estimatedSeconds() / 60.0)
					.append(" min (warm-up and transitions included)\n").toString();
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

			muscleGroups.put(normalizedDay, normalizeMuscleGroup(muscleGroup));
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

			String value = day.trim().toLowerCase(Locale.ROOT);
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
			try {
				run();
			} catch (NoSuchElementException exception) {
				System.out.println("\nInput ended. Workout planner closed.");
			}
		}

		private void run() {
			System.out.println("NPNG Workout Planner");
			Profile profile = new Profile();
			profile.setFitnessGoal(readFitnessGoal());
			profile.setWorkoutDuration(readWorkoutDuration());
			int workoutDays = readWorkoutDays();

			for (int index = 0; index < workoutDays; index++) {
				String day = readDay("Workout day " + (index + 1) + " (choose a different day each time): ");
				while (!profile.addPreferredWorkoutDay(day)) {
					day = readDay("Choose a different valid workout day: ");
				}
			}

			String equipment = readEquipment();
			for (String item : equipment.split(",")) {
				if (!item.trim().equalsIgnoreCase("none")) {
					profile.addEquipment(item);
				}
			}
			String limitations = readOptional(
					"Limitations or injuries (shoulder, knee, lower back, elbow, wrist; separate with commas; Enter for none): ");
			for (String limitation : limitations.split(",")) {
				profile.addLimit(limitation);
			}
			String preference = readOptional(
					"Preferred exercise (exact name, e.g. Push Ups or Barbell Bench Press; Enter to skip): ");
			if (!preference.isEmpty()) {
				profile.addExercisePreference(preference);
			}
			System.out.println("Split is automatic: 1 day Full Body; 2 Upper/Lower; 3 Push/Pull/Legs;");
			System.out.println("4 Upper/Lower twice; 5 body-part split; 6 Push/Pull/Legs twice; 7 adds recovery.");

			if (profile.getFitnessGoal() == null) {
				System.out.println("Please assign a fitness goal first.");
				return;
			}

			WorkoutPlanner planner = new WorkoutPlanner();
			String customName = readOptional(
					"Custom exercise name (press Enter to skip): ");
			if (!customName.isEmpty()) {
				String customGroup = readNonEmpty("Custom exercise muscle group (Shoulders, Back, Chest, Legs, Arms, or custom): ");
				String customEquipment = readNonEmpty(
						"Custom exercise equipment (or none): ");
				String customLimitation = readOptional(
						"Custom exercise limitation (or press Enter for none): ");
				int customSets = readPositiveInt("Custom exercise sets: ");
				int customReps = readPositiveInt("Custom exercise reps: ");
				planner.addCustomExercise(new Exercise(customName, customGroup,
						customEquipment, customLimitation, customSets, customReps));
			}

			WorkoutSchedule schedule = planner.generateSchedule(profile);
			displayPlan(schedule, profile);
			editRoutineMenu(schedule, planner, profile);
		}

		public String readFitnessGoal() {
			while (true) {
				FitnessGoal goal = FitnessGoal.parse(readNonEmpty(
						"Fitness goal (Strength, Muscle Gain, Endurance, Weight Loss): "));
				if (goal != null) {
					return goal.label;
				}
				System.out.println("Please choose one of the four listed goals.");
			}
		}

		public int readWorkoutDays() {
			int days = readNumberInRange("Number of workout days per week (1-7, e.g. 3): ", 1, 7);
			if (days == 1) System.out.println("Only one day? Is that all you've got?");
			if (days == 7) System.out.println("You need some rest. I'm going to add a recovery day.");
			return days;
		}

		public int readWorkoutDuration() {
			System.out.println("Duration is a per-session time budget, not a promise to fill the entire time.");
			System.out.println("Each exercise has its own sets and reps. Equipment, injuries, and available exercises may reduce the count.");
			return readPositiveInt("Time available per workout in minutes (e.g. 30, 60, 90): ");
		}

		public String readWorkoutDay() {
			return readDay("Workout day: ");
		}

		public String readEquipment() {
			return readNonEmpty("Available equipment (barbell, dumbbells, bench, cable machine, resistance band, pull up bar, or none; separate with commas): ");
		}

		public void displayRoutine(WorkoutRoutine routine) {
			System.out.println(routine == null ? "No exercises in this routine." : routine);
		}

		public void displaySchedule(WorkoutSchedule schedule) {
			System.out.println(schedule == null ? "No workouts scheduled." : schedule);
		}

		private void displayPlan(WorkoutSchedule schedule, Profile profile) {
			System.out.println("\nCurrent plan");
			System.out.println("Fitness goal: " + profile.getFitnessGoal());
			System.out.println("Estimated time: 5 min warm-up + 3.5 min per working set (including rest) + 1 min between exercises.");
			System.out.println("Duration budget: " + profile.getWorkoutDuration()
					+ " min (includes 5 min warm-up allowance; exercise times are estimates)");
			if (FitnessGoal.parse(profile.getFitnessGoal()) == FitnessGoal.WEIGHT_LOSS) {
				System.out.println("Resistance-training component only; this plan does not estimate calorie burn.");
			}
			System.out.println("Limitations or injuries: " + (profile.limitations.isEmpty()
					? "None" : String.join(", ", profile.limitations)));
			displaySchedule(schedule);
		}

		private void editRoutineMenu(WorkoutSchedule schedule,
				WorkoutPlanner planner, Profile profile) {
			while (true) {
				String answer = readOptional(
						"Choose view or edit (or press Enter to finish): ");
				if (answer.isEmpty()) {
					return;
				}
				if (answer.equalsIgnoreCase("view")) {
					displayPlan(schedule, profile);
					continue;
				}
				if (!answer.equalsIgnoreCase("edit") && !answer.equalsIgnoreCase("yes")) {
					System.out.println("Choose view or edit.");
					continue;
				}
				String day = readDay("Workout day to edit: ");
				WorkoutRoutine routine = schedule.getRoutine(day);
				if (routine == null || routine.recovery) {
					System.out.println("That day is a rest/recovery day and cannot be edited.");
				} else {
					String action = readOptional(
							"Choose action: add, remove, or change: ");
					if (action.equalsIgnoreCase("add")) {
						Exercise exercise = readExercise();
						if (!planner.addManualExercise(routine, exercise, profile)) {
							System.out.println("Exercise cannot be added. Check equipment, "
									+ "limitations, or duplicate/invalid exercise details.");
						} else {
							System.out.println("Exercise added.");
							if (routine.estimatedSeconds() > (long) profile.getWorkoutDuration() * 60) {
								System.out.println("Warning: exercise kept, but the routine now takes about "
										+ routine.estimatedSeconds() / 60.0 + " min, exceeding your "
										+ profile.getWorkoutDuration() + " min budget.");
							}
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
									+ "limitations, duplicate names, or the time budget.");
						}
					} else {
						System.out.println("Choose add, remove, or change.");
					}
				}
				displayPlan(schedule, profile);
			}
		}

		private Exercise readExercise() {
			String name = readNonEmpty("Exercise name: ");
			String muscleGroup = readNonEmpty("Exercise muscle group (Shoulders, Back, Chest, Legs, Arms, or custom): ");
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
			System.out.println("Days: Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday (use full names).");
			while (true) {
				String day = readNonEmpty(prompt).toLowerCase(Locale.ROOT);
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
					"Push Ups", "Chest", "none", "shoulder, elbow, wrist"));
			exerciseDatabase.add(new Exercise(
					"Barbell Squat", "Legs", "barbell", "knee"));
			exerciseDatabase.add(new Exercise(
					"Dumbbell Lunges", "Legs", "dumbbells", "knee"));
			exerciseDatabase.add(new Exercise(
					"Pull Ups", "Back", "pull up bar", "shoulder"));
			exerciseDatabase.add(new Exercise(
					"Barbell Row", "Back", "barbell", "lower back"));
			exerciseDatabase.add(new Exercise(
					"Wall Push Ups", "Chest", "none", "shoulder"));
			exerciseDatabase.add(new Exercise(
					"Knee Push Ups", "Chest", "none", "shoulder, knee"));
			exerciseDatabase.add(new Exercise(
					"Wide Push Ups", "Chest", "none", "shoulder"));
			exerciseDatabase.add(new Exercise(
					"Close Grip Push Ups", "Chest", "none", "shoulder"));
			exerciseDatabase.add(new Exercise(
					"Dumbbell Floor Press", "Chest", "dumbbells", "shoulder"));
			exerciseDatabase.add(new Exercise(
					"Dumbbell Squeeze Press", "Chest", "dumbbells", "shoulder"));
			exerciseDatabase.add(new Exercise(
					"Barbell Floor Press", "Chest", "barbell", "shoulder"));
			exerciseDatabase.add(new Exercise(
					"Standing Band Chest Press", "Chest", "resistance band", "shoulder"));
			exerciseDatabase.add(new Exercise(
					"Bodyweight Squat", "Legs", "none", "knee, lower back"));
			exerciseDatabase.add(new Exercise(
					"Reverse Lunges", "Legs", "none", "knee"));
			exerciseDatabase.add(new Exercise(
					"Split Squat", "Legs", "none", "knee"));
			exerciseDatabase.add(new Exercise(
					"Glute Bridge", "Legs", "none", "lower back"));
			exerciseDatabase.add(new Exercise(
					"Standing Calf Raises", "Legs", "none", "knee"));
			exerciseDatabase.add(new Exercise(
					"Side Lying Leg Raises", "Legs", "none", "lower back"));
			exerciseDatabase.add(new Exercise(
					"Dumbbell Goblet Squat", "Legs", "dumbbells", "knee, lower back"));
			exerciseDatabase.add(new Exercise(
					"Dumbbell Romanian Deadlift", "Legs", "dumbbells", "lower back"));
			exerciseDatabase.add(new Exercise(
					"Prone W Raises", "Back", "none", "shoulder, lower back"));
			exerciseDatabase.add(new Exercise(
					"Prone Y Raises", "Back", "none", "shoulder, lower back"));
			exerciseDatabase.add(new Exercise(
					"Reverse Snow Angels", "Back", "none", "shoulder, lower back"));
			exerciseDatabase.add(new Exercise(
					"Bird Dog", "Back", "none", "shoulder, knee, lower back"));
			exerciseDatabase.add(new Exercise(
					"Dumbbell Bent Over Row", "Back", "dumbbells", "shoulder, lower back"));
			exerciseDatabase.add(new Exercise(
					"Dumbbell Reverse Fly", "Back", "dumbbells", "shoulder, lower back"));
			exerciseDatabase.add(new Exercise(
					"Seated Band Row", "Back", "resistance band", "shoulder, lower back"));
			exerciseDatabase.add(new Exercise(
					"Band Pull Apart", "Back", "resistance band", "shoulder"));
			exerciseDatabase.add(new Exercise(
					"Dumbbell Shoulder Press", "Shoulders", "dumbbells", "shoulder, elbow, lower back"));
			exerciseDatabase.add(new Exercise(
					"Dumbbell Lateral Raise", "Shoulders", "dumbbells", "shoulder"));
			exerciseDatabase.add(new Exercise(
					"Dumbbell Front Raise", "Shoulders", "dumbbells", "shoulder"));
			exerciseDatabase.add(new Exercise(
					"Dumbbell Rear Delt Raise", "Shoulders", "dumbbells", "shoulder, lower back"));
			exerciseDatabase.add(new Exercise(
					"Barbell Overhead Press", "Shoulders", "barbell", "shoulder, elbow, lower back"));
			exerciseDatabase.add(new Exercise(
					"Band Lateral Raise", "Shoulders", "resistance band", "shoulder"));
			exerciseDatabase.add(new Exercise(
					"Band Overhead Press", "Shoulders", "resistance band", "shoulder, elbow"));
			exerciseDatabase.add(new Exercise(
					"Pike Push Ups", "Shoulders", "none", "shoulder, elbow, wrist"));
			exerciseDatabase.add(new Exercise(
					"Dumbbell Biceps Curl", "Arms", "dumbbells", "elbow, wrist"));
			exerciseDatabase.add(new Exercise(
					"Dumbbell Hammer Curl", "Arms", "dumbbells", "elbow, wrist"));
			exerciseDatabase.add(new Exercise(
					"Dumbbell Triceps Kickback", "Arms", "dumbbells", "shoulder, elbow, lower back"));
			exerciseDatabase.add(new Exercise(
					"Dumbbell Overhead Triceps Extension", "Arms", "dumbbells", "shoulder, elbow"));
			exerciseDatabase.add(new Exercise(
					"Barbell Biceps Curl", "Arms", "barbell", "elbow, wrist"));
			exerciseDatabase.add(new Exercise(
					"Band Biceps Curl", "Arms", "resistance band", "elbow, wrist"));
			exerciseDatabase.add(new Exercise(
					"Band Overhead Triceps Extension", "Arms", "resistance band", "shoulder, elbow"));
			exerciseDatabase.add(new Exercise(
					"Diamond Push Ups", "Arms", "none", "shoulder, elbow, wrist"));
			exerciseDatabase.add(new Exercise(
					"Incline Dumbbell Press", "Chest", "dumbbells, bench", "shoulder"));
			exerciseDatabase.add(new Exercise(
					"Chest Supported Row", "Back", "dumbbells, bench", "shoulder"));
			exerciseDatabase.add(new Exercise(
					"Lat Pulldown", "Back", "cable machine", "shoulder, elbow"));
			exerciseDatabase.add(new Exercise(
					"Triceps Pushdown", "Arms", "cable machine", "elbow"));
			exerciseDatabase.add(new Exercise(
					"Incline Dumbbell Curl", "Arms", "dumbbells, bench", "shoulder, elbow, wrist"));
		}

		public boolean addCustomExercise(Exercise exercise) {
			if (exercise == null || !exercise.hasValidDefinition()) {
				return false;
			}
			for (Exercise existing : exerciseDatabase) {
				if (existing.getName().equalsIgnoreCase(exercise.getName().trim())) {
					return false;
				}
			}
			return exerciseDatabase.add(exercise);
		}

		public WorkoutSchedule generateSchedule(Profile profile) {
			WorkoutSchedule schedule = new WorkoutSchedule();
			if (profile == null || profile.getFitnessGoal() == null) {
				return schedule;
			}
			String[] split = switch (profile.preferredWorkoutDays.size()) {
				case 1 -> new String[] {"Full Body"};
				case 2 -> new String[] {"Upper", "Legs"};
				case 3 -> new String[] {"Push", "Pull", "Legs"};
				case 4 -> new String[] {"Upper", "Legs", "Upper", "Legs"};
				case 5 -> new String[] {"Chest", "Back", "Legs", "Shoulders", "Arms"};
				case 6 -> new String[] {"Push", "Pull", "Legs", "Push", "Pull", "Legs"};
				case 7 -> new String[] {"Push", "Pull", "Legs", "Push", "Pull", "Legs", "Recovery"};
				default -> new String[0];
			};
			int dayIndex = 0;
			for (String day : new String[] {"monday", "tuesday", "wednesday", "thursday",
					"friday", "saturday", "sunday"}) {
				if (profile.hasPreferredWorkoutDay(day)) {
					String group = split[dayIndex++];
					schedule.addWorkoutDay(day, group);
					schedule.assignRoutine(day, group.equals("Recovery")
							? new WorkoutRoutine("Recovery day - no strength workout scheduled.", true)
							: buildSession(profile, group));
				}
			}
			return schedule;
		}

		private WorkoutRoutine buildSession(Profile profile, String split) {
			WorkoutRoutine base = generateWorkout(profile, split);
			if (FitnessGoal.parse(profile.getFitnessGoal()) != FitnessGoal.MUSCLE_GAIN
					|| profile.getWorkoutDuration() < 90) {
				return base;
			}
			String[] preferred = switch (split) {
				case "Upper" -> new String[] {"Barbell Bench Press", "Pull Ups", "Lat Pulldown",
						"Incline Dumbbell Press", "Chest Supported Row", "Dumbbell Lateral Raise",
						"Dumbbell Rear Delt Raise", "Triceps Pushdown", "Incline Dumbbell Curl"};
				case "Push" -> new String[] {"Barbell Bench Press", "Dumbbell Shoulder Press",
						"Incline Dumbbell Press", "Dumbbell Floor Press", "Dumbbell Lateral Raise",
						"Triceps Pushdown", "Dumbbell Overhead Triceps Extension"};
				case "Pull" -> new String[] {"Pull Ups", "Lat Pulldown", "Barbell Row", "Chest Supported Row",
						"Dumbbell Reverse Fly", "Band Pull Apart", "Barbell Biceps Curl", "Dumbbell Hammer Curl"};
				default -> new String[0];
			};
			List<Exercise> candidates = new ArrayList<>();
			for (String name : preferred) {
				for (Exercise exercise : exerciseDatabase) {
					if (exercise.name.equals(name) && isExerciseValid(exercise, profile)) {
						// Pull-ups and pulldowns are alternatives for the same main lift.
						if (!name.equals("Lat Pulldown") || candidates.stream().noneMatch(e -> e.name.equals("Pull Ups"))) {
							candidates.add(exercise);
						}
					}
				}
			}
			candidates.addAll(base.getExercises());
			Map<String, Exercise> unique = new LinkedHashMap<>();
			for (Exercise exercise : candidates) {
				unique.putIfAbsent(exercise.name, exercise);
			}
			candidates = new ArrayList<>(unique.values());
			candidates.sort(Comparator.comparing(e -> !profile.hasExercisePreference(e.name)));
			// ponytail: catalog-name heuristic; explicit movement metadata when the catalog grows.
			candidates.subList(0, Math.min(8, candidates.size())).sort(Comparator.comparing((Exercise e) -> isIsolation(e))
					.thenComparing(e -> !profile.hasExercisePreference(e.name)));
			WorkoutRoutine result = new WorkoutRoutine(split + " Workout - Muscle Gain (90-minute template;"
					+ " fewer exercises if equipment or limitations restrict the plan)");
			for (Exercise exercise : candidates) {
				if (result.containsExercise(exercise.name) || result.getNumberOfExercises() >= recommendedExerciseCount(profile)) {
					continue;
				}
				boolean custom = exerciseDatabase.stream().anyMatch(e -> e.name.equals(exercise.name) && !e.goalBased);
				int index = result.getNumberOfExercises();
				boolean isolation = isIsolation(exercise);
				Exercise prescribed = exercise;
				if (!custom) {
					prescribed = new Exercise(exercise.name, exercise.muscleGroup, exercise.equipment,
							exercise.limitation, index < 4 ? 3 : 2, isolation ? 12 : index < 2 ? 6 : 8);
					prescribed.maxReps = isolation ? 20 : index < 2 ? 10 : 12;
					prescribed.restSeconds = isolation ? 75 : 150;
				}
				addGeneratedExercise(result, prescribed, profile);
			}
			return result.getNumberOfExercises() == 0 ? base : result;
		}

		private boolean isIsolation(Exercise exercise) {
			return exercise.name.matches(".*(Raise|Fly|Curl|Extension|Kickback|Pushdown|Pull Apart).*");
		}

		public WorkoutRoutine generateWorkout(Profile profile,
											   String muscleGroup) {
			if (profile == null
					|| profile.getFitnessGoal() == null
					|| muscleGroup == null
					|| muscleGroup.trim().isEmpty()) {
				return null;
			}

			muscleGroup = normalizeMuscleGroup(muscleGroup);
			FitnessGoal goal = FitnessGoal.parse(profile.getFitnessGoal());
			WorkoutRoutine routine = new WorkoutRoutine(muscleGroup.trim() + " Workout - "
					+ goal.label + " (rest " + goal.restSeconds + " sec between sets)");
			String[] splitGroups = switch (muscleGroup) {
				case "Full Body" -> new String[] {"Legs", "Chest", "Back", "Shoulders", "Arms"};
				case "Upper" -> new String[] {"Chest", "Back", "Shoulders", "Arms"};
				case "Push" -> new String[] {"Chest", "Shoulders", "Triceps"};
				case "Pull" -> new String[] {"Back", "Biceps"};
				default -> new String[0];
			};
			if (splitGroups.length > 0) {
				List<List<Exercise>> groups = new ArrayList<>();
				for (String group : splitGroups) {
					groups.add(generateWorkout(profile, group).getExercises());
				}
				// Take one exercise per group before repeating a group, within the shared time budget.
				for (int index = 0; index < recommendedExerciseCount(profile); index++) {
					for (List<Exercise> group : groups) {
						if (index < group.size()) {
							addGeneratedExercise(routine, group.get(index), profile);
						}
					}
				}
				return routine;
			}
			List<Exercise> candidates = new ArrayList<>(exerciseDatabase);
			// ponytail: equipment-based ordering; add exercise-specific priorities as the catalog grows.
			candidates.sort(Comparator
					.comparing((Exercise e) -> !profile.hasExercisePreference(e.getName()))
					.thenComparing(e -> !goal.preferredEquipment.equalsIgnoreCase(e.getEquipment())));
			for (Exercise exercise : candidates) {
				if ((exercise.getMuscleGroup().equalsIgnoreCase(muscleGroup)
						|| (exercise.getMuscleGroup().equals("Arms")
								&& (muscleGroup.equals("Biceps") && exercise.name.contains("Curl")
								|| muscleGroup.equals("Triceps") && !exercise.name.contains("Curl"))))
						&& isExerciseValid(exercise, profile)) {
					Exercise prescribed = exercise.goalBased
							? new Exercise(exercise.name, exercise.muscleGroup, exercise.equipment,
									exercise.limitation, recommendedSets(profile), goal.reps)
							: exercise;
					addGeneratedExercise(routine, prescribed, profile);
					if (routine.getNumberOfExercises() >= recommendedExerciseCount(profile)) {
						break;
					}
				}
			}

			return routine;
		}

		private void addGeneratedExercise(WorkoutRoutine routine, Exercise exercise, Profile profile) {
			if (addExercise(routine, exercise, profile)) return;
			boolean automatic = exerciseDatabase.stream().anyMatch(e -> e.name.equals(exercise.name) && e.goalBased);
			if (!automatic || routine.containsExercise(exercise.name)) return;
			for (int sets = exercise.sets - 1; sets > 0; sets--) {
				Exercise reduced = new Exercise(exercise.name, exercise.muscleGroup, exercise.equipment,
						exercise.limitation, sets, exercise.reps);
				reduced.maxReps = exercise.maxReps;
				reduced.restSeconds = exercise.restSeconds;
				if (addExercise(routine, reduced, profile)) return;
			}
		}

		public boolean addManualExercise(WorkoutRoutine routine, Exercise exercise, Profile profile) {
			return routine != null && !routine.recovery && isExerciseValid(exercise, profile)
					&& routine.addExercise(exercise);
		}

		public boolean addExercise(WorkoutRoutine routine, Exercise exercise, Profile profile) {
			return routine != null && !routine.recovery && isExerciseValid(exercise, profile)
					&& fitsBudget(routine, null, exercise, profile) && routine.addExercise(exercise);
		}

		private boolean fitsBudget(WorkoutRoutine routine, Exercise replaced, Exercise added, Profile profile) {
			List<Exercise> proposed = routine.getExercises();
			proposed.remove(replaced);
			proposed.add(added);

			long sets = 0;
			for (Exercise exercise : proposed) {
				sets += exercise.sets;
			}
			// ponytail: fixed set/rest and transition estimates; use live timing if needed.
			return 300 + sets * 210 + Math.max(0, proposed.size() - 1) * 60L
					<= (long) profile.getWorkoutDuration() * 60;
		}

		public boolean removeExercise(WorkoutRoutine routine,
									  String exerciseName) {
			return routine != null && routine.removeExercise(exerciseName);
		}

		public boolean changeExercise(WorkoutRoutine routine, String oldExercise,
				Exercise newExercise, Profile profile) {
			if (routine == null || routine.recovery || !isExerciseValid(newExercise, profile)) {
				return false;
			}
			Exercise original = routine.getExercise(oldExercise);
			Exercise duplicate = routine.getExercise(newExercise.name);
			if (original == null || (duplicate != null && duplicate != original)
					|| !fitsBudget(routine, original, newExercise, profile)) {
				return false;
			}
			routine.exercises.set(routine.exercises.indexOf(original), newExercise);
			return true;
		}

		public boolean isExerciseValid(Exercise exercise, Profile profile) {
			if (exercise == null || profile == null || !exercise.hasValidDefinition()
					|| !matchesProfile(exercise, profile)) return false;
			for (Exercise existing : exerciseDatabase) {
				if (existing.name.equalsIgnoreCase(exercise.name) && !matchesProfile(existing, profile)) return false;
			}
			return true;
		}

		private boolean matchesProfile(Exercise exercise, Profile profile) {
			String equipment = exercise.getEquipment();
			String limitation = exercise.getLimitation();

			boolean equipmentAvailable = equipment != null;
			if (equipment != null && !equipment.equalsIgnoreCase("none")) {
				for (String item : equipment.split(",")) {
					equipmentAvailable &= profile.hasEquipment(item);
				}
			}

			if (limitation != null) {
				for (String restrictedArea : limitation.split(",")) {
					if (profile.hasLimitation(restrictedArea)) {
						return false;
					}
				}
			}
			return equipmentAvailable;
		}

		private int recommendedSets(Profile profile) {
			FitnessGoal goal = FitnessGoal.parse(profile.getFitnessGoal());
			if (goal == FitnessGoal.MUSCLE_GAIN && profile.getWorkoutDuration() >= 90) {
				return goal.sets;
			}
			return goal.sets + (profile.getWorkoutDuration() >= 75 ? 1 : 0);
		}

		private int recommendedExerciseCount(Profile profile) {
			if (profile == null || profile.getFitnessGoal() == null) return 0;
			return (int) Math.min(exerciseDatabase.size(), Math.max(0L,
					((long) profile.getWorkoutDuration() * 60 - 300) / 210));
		}
	}
}
