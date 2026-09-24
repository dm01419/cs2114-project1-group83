import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for WorkoutSchedule: a normal case and a bad-input case for each
 * method.
 */
class WorkoutScheduleTest {
    private WorkoutSchedule schedule;
    private WorkoutRoutine routine;

    @BeforeEach
    void setUp() {
        schedule = new WorkoutSchedule();
        routine = new WorkoutRoutine("Chest Workout");
    }

    @Test
    void addWorkoutDayAddsDay() {
        assertTrue(schedule.addWorkoutDay("Monday", "Chest"));
        assertTrue(schedule.isWorkoutDay("monday"));
        assertEquals("Chest", schedule.getMuscleGroup("MONDAY"));
    }

    @Test
    void addWorkoutDayRejectsInvalidDuplicateOrEmpty() {
        assertFalse(schedule.addWorkoutDay("Funday", "Chest"));
        assertFalse(schedule.addWorkoutDay("Monday", " "));
        assertTrue(schedule.addWorkoutDay("Monday", "Chest"));
        assertFalse(schedule.addWorkoutDay("Monday", "Back"));
    }

    @Test
    void removeWorkoutDayRemovesDayAndRoutine() {
        schedule.addWorkoutDay("Monday", "Chest");
        schedule.assignRoutine("Monday", routine);
        assertTrue(schedule.removeWorkoutDay("Monday"));
        assertFalse(schedule.isWorkoutDay("Monday"));
        assertNull(schedule.getRoutine("Monday"));
    }

    @Test
    void removeWorkoutDayReturnsFalseWhenNotScheduled() {
        assertFalse(schedule.removeWorkoutDay("Sunday"));
        assertFalse(schedule.removeWorkoutDay("Funday"));
    }

    @Test
    void changeWorkoutDayMovesWorkoutAndRoutine() {
        schedule.addWorkoutDay("Monday", "Chest");
        schedule.assignRoutine("Monday", routine);
        assertTrue(schedule.changeWorkoutDay("Monday", "Tuesday"));
        assertFalse(schedule.isWorkoutDay("Monday"));
        assertEquals("Chest", schedule.getMuscleGroup("Tuesday"));
        assertSame(routine, schedule.getRoutine("Tuesday"));
    }

    @Test
    void changeWorkoutDayRejectsTakenDay() {
        schedule.addWorkoutDay("Monday", "Chest");
        schedule.addWorkoutDay("Tuesday", "Back");
        assertFalse(schedule.changeWorkoutDay("Monday", "Tuesday"));
        assertEquals("Chest", schedule.getMuscleGroup("Monday"));
        assertFalse(schedule.changeWorkoutDay("Sunday", "Friday"));
    }

    @Test
    void isWorkoutDayTrueForScheduledDay() {
        schedule.addWorkoutDay("Monday", "Chest");
        assertTrue(schedule.isWorkoutDay("Monday"));
    }

    @Test
    void isWorkoutDayFalseForRestDay() {
        assertFalse(schedule.isWorkoutDay("Sunday"));
        assertFalse(schedule.isWorkoutDay(null));
    }

    @Test
    void getMuscleGroupReturnsGroup() {
        schedule.addWorkoutDay("Monday", "Chest");
        assertEquals("Chest", schedule.getMuscleGroup("Monday"));
    }

    @Test
    void getMuscleGroupReturnsNullForRestDay() {
        assertNull(schedule.getMuscleGroup("Monday"));
        assertNull(schedule.getMuscleGroup("Funday"));
    }

    @Test
    void assignRoutineToWorkoutDay() {
        schedule.addWorkoutDay("Monday", "Chest");
        assertTrue(schedule.assignRoutine("Monday", routine));
    }

    @Test
    void assignRoutineRejectsRestDay() {
        assertFalse(schedule.assignRoutine("Sunday", routine));
        assertNull(schedule.getRoutine("Sunday"));
    }

    @Test
    void getRoutineReturnsAssignedRoutine() {
        schedule.addWorkoutDay("Monday", "Chest");
        schedule.assignRoutine("Monday", routine);
        assertSame(routine, schedule.getRoutine("Monday"));
    }

    @Test
    void getRoutineReturnsNullWhenNoneAssigned() {
        schedule.addWorkoutDay("Monday", "Chest");
        assertNull(schedule.getRoutine("Monday"));
    }
}
