import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for WorkoutRoutine: a normal case and a bad-input case for each
 * method.
 */
class WorkoutRoutineTest {
    private WorkoutRoutine routine;
    private Exercise squat;
    private Exercise bench;

    @BeforeEach
    void setUp() {
        routine = new WorkoutRoutine("Leg Day");
        squat = new Exercise("Squat", "Legs", null, "Barbell", 3, 10);
        bench = new Exercise("Bench Press", "Chest", null, "Barbell", 4, 8);
    }

    @Test
    void addExerciseAddsNewExercise() {
        assertTrue(routine.addExercise(squat));
        assertTrue(routine.containsExercise("Squat"));
    }

    @Test
    void addExerciseRejectsDuplicateAndNull() {
        routine.addExercise(squat);
        assertFalse(routine.addExercise(
                new Exercise("squat", "Legs", null, null, 1, 1)));
        assertFalse(routine.addExercise(null));
        assertEquals(1, routine.getNumberOfExercises());
    }

    @Test
    void removeExerciseRemovesExisting() {
        routine.addExercise(squat);
        assertTrue(routine.removeExercise("Squat"));
        assertFalse(routine.containsExercise("Squat"));
    }

    @Test
    void removeExerciseReturnsFalseWhenMissing() {
        routine.addExercise(squat);
        assertFalse(routine.removeExercise("Deadlift"));
        assertEquals(1, routine.getNumberOfExercises());
    }

    @Test
    void containsExerciseFindsExercise() {
        routine.addExercise(squat);
        assertTrue(routine.containsExercise("squat"));
    }

    @Test
    void containsExerciseReturnsFalseWhenMissing() {
        assertFalse(routine.containsExercise("Deadlift"));
        assertFalse(routine.containsExercise(null));
    }

    @Test
    void getExerciseReturnsExercise() {
        routine.addExercise(squat);
        assertSame(squat, routine.getExercise("Squat"));
    }

    @Test
    void getExerciseReturnsNullWhenMissing() {
        assertNull(routine.getExercise("Deadlift"));
        assertNull(routine.getExercise(" "));
    }

    @Test
    void getNumberOfExercisesCountsExercises() {
        routine.addExercise(squat);
        routine.addExercise(bench);
        assertEquals(2, routine.getNumberOfExercises());
    }

    @Test
    void getNumberOfExercisesIsZeroWhenEmpty() {
        assertEquals(0, routine.getNumberOfExercises());
    }

    @Test
    void getExercisesReturnsAllInOrder() {
        routine.addExercise(squat);
        routine.addExercise(bench);
        ArrayList<Exercise> list = routine.getExercises();
        assertEquals(2, list.size());
        assertSame(squat, list.get(0));
        assertSame(bench, list.get(1));
    }

    @Test
    void getExercisesIsEmptyWhenRoutineEmpty() {
        assertTrue(routine.getExercises().isEmpty());
    }

    @Test
    void replaceExerciseKeepsPosition() {
        routine.addExercise(squat);
        routine.addExercise(bench);
        Exercise legPress = new Exercise("Leg Press", "Legs", null, null, 3, 10);
        assertTrue(routine.replaceExercise("Squat", legPress));
        assertSame(legPress, routine.getExercises().get(0));
        assertFalse(routine.containsExercise("Squat"));
    }

    @Test
    void replaceExerciseRejectsMissingOrDuplicate() {
        routine.addExercise(squat);
        routine.addExercise(bench);
        assertFalse(routine.replaceExercise("Deadlift", bench));
        assertFalse(routine.replaceExercise("Squat", bench));
        assertFalse(routine.replaceExercise("Squat", null));
    }

    @Test
    void toStringListsExercisesOrSaysEmpty() {
        assertTrue(routine.toString().contains("No exercises in this routine."));
        routine.addExercise(squat);
        assertTrue(routine.toString().contains("Squat 3 x 10 (Legs)"));
        assertEquals("Leg Day", routine.getName());
    }
}
