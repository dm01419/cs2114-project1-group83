/**
 * Starts the workout planner.
 */
public class Main {
    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI(new Profile(), new WorkoutSchedule(),
                new WorkoutPlanner());
        ui.start();
    }
}
