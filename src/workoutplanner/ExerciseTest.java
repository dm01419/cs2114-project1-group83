package workoutplanner;

//Project 1 fall 2026
//Virginia Tech Honor Code Pledge:
//
//As a Hokie, I will conduct myself with honor and integrity at all times.
//I will not lie, cheat, or steal, nor will I
//accept the actions of those who do.
//-- Ishaan Srichitturi (Ishaan07)

//LLM Statement:
//
//During the preparation of this assignment, I, Ishaan Srichitturi used claude
//to help implement the Exercise class and its test cases.
//After using this tool, I reviewed and edited the content as needed
//to ensure its accuracy and take full responsibility for the content in
//relation to grading. I understand that I am responsible for being able to
//complete this work without the use of assistance.

/**
 * Tests for the Exercise class. Each method has a normal case and a
 * bad-input case from the test plan.
 *
 * @author Ishaan Srichitturi
 * @version 2026.09.23
 */
public class ExerciseTest extends student.TestCase {
    private Exercise squat;
    private Exercise bench;
    private Exercise pushUp;
    private Profile profile;

    /**
     * Sets up the exercises and profile for each test.
     */
    public void setUp() {
        squat = new Exercise("Squat", "Quadriceps", "Hamstrings", "Barbell",
            3, 10);
        squat.addConflictingLimitation("knee");
        bench = new Exercise("Bench Press", "Chest", "Triceps", "Barbell", 4,
            8);
        pushUp = new Exercise("Push Up", "Chest", null, "", 3, 15);
        profile = new Profile();
    }


    /**
     * Tests the constructor stores values and trims them.
     */
    public void testConstructor() {
        Exercise e = new Exercise("  Deadlift ", " Back ", " Glutes ",
            " Barbell ", 5, 5);
        assertEquals("Deadlift", e.getName());
        assertEquals("Back", e.getPrimaryMuscleGroup());
        assertEquals("Glutes", e.getSecondaryMuscleGroup());
        assertEquals("Barbell", e.getRequiredEquipment());
        assertEquals(5, e.getSets());
        assertEquals(5, e.getReps());
    }


    /**
     * Tests getName, and that an empty name is rejected.
     */
    public void testGetName() {
        assertEquals("Squat", squat.getName());

        Exception thrown = null;
        try {
            new Exercise("", "Chest", null, null, 3, 10);
        }
        catch (Exception e) {
            thrown = e;
        }
        assertTrue(thrown instanceof IllegalArgumentException);

        thrown = null;
        try {
            new Exercise(null, "Chest", null, null, 3, 10);
        }
        catch (Exception e) {
            thrown = e;
        }
        assertTrue(thrown instanceof IllegalArgumentException);
    }


    /**
     * Tests getPrimaryMuscleGroup, and that a missing one is rejected.
     */
    public void testGetPrimaryMuscleGroup() {
        assertEquals("Quadriceps", squat.getPrimaryMuscleGroup());

        Exception thrown = null;
        try {
            new Exercise("Squat", "   ", null, null, 3, 10);
        }
        catch (Exception e) {
            thrown = e;
        }
        assertTrue(thrown instanceof IllegalArgumentException);
    }


    /**
     * Tests getSecondaryMuscleGroup, and "None" when there is none.
     */
    public void testGetSecondaryMuscleGroup() {
        assertEquals("Hamstrings", squat.getSecondaryMuscleGroup());
        assertEquals(Exercise.NONE, pushUp.getSecondaryMuscleGroup());
    }


    /**
     * Tests getRequiredEquipment, and "None" when no equipment is needed.
     */
    public void testGetRequiredEquipment() {
        assertEquals("Barbell", bench.getRequiredEquipment());
        assertEquals(Exercise.NONE, pushUp.getRequiredEquipment());
    }


    /**
     * Tests sets and reps getters and setters, including bad values.
     */
    public void testSetsAndReps() {
        bench.setSets(5);
        bench.setReps(6);
        assertEquals(5, bench.getSets());
        assertEquals(6, bench.getReps());

        Exception thrown = null;
        try {
            bench.setSets(0);
        }
        catch (Exception e) {
            thrown = e;
        }
        assertTrue(thrown instanceof IllegalArgumentException);
        assertEquals(5, bench.getSets());

        thrown = null;
        try {
            bench.setReps(-3);
        }
        catch (Exception e) {
            thrown = e;
        }
        assertTrue(thrown instanceof IllegalArgumentException);
        assertEquals(6, bench.getReps());

        thrown = null;
        try {
            new Exercise("Row", "Back", null, null, 0, 10);
        }
        catch (Exception e) {
            thrown = e;
        }
        assertTrue(thrown instanceof IllegalArgumentException);
    }


    /**
     * Tests adding conflicting limitations.
     */
    public void testAddConflictingLimitation() {
        assertTrue(bench.addConflictingLimitation(" Shoulder "));
        assertFalse(bench.addConflictingLimitation("shoulder"));
        assertFalse(bench.addConflictingLimitation(""));
        assertFalse(bench.addConflictingLimitation(null));
        assertEquals(1, bench.getConflictingLimitations().size());
        assertTrue(bench.getConflictingLimitations().contains("shoulder"));
    }


    /**
     * Tests canPerformWith when the user has the equipment and no
     * conflicting limitation.
     */
    public void testCanPerformWith() {
        profile.addEquipment("barbell");
        assertTrue(squat.canPerformWith(profile));
        assertTrue(bench.canPerformWith(profile));
        // no equipment needed
        assertTrue(pushUp.canPerformWith(new Profile()));
    }


    /**
     * Tests canPerformWith when the user is missing equipment, has a
     * conflicting limitation, or the profile is null.
     */
    public void testCanPerformWithBad() {
        assertFalse(bench.canPerformWith(profile));

        profile.addEquipment("Dumbbells");
        profile.addEquipment(null);
        assertFalse(bench.canPerformWith(profile));

        profile.addEquipment("Barbell");
        profile.addLimit(null);
        profile.addLimit("Knee injury");
        assertFalse(squat.canPerformWith(profile));
        assertTrue(bench.canPerformWith(profile));

        assertFalse(squat.canPerformWith(null));
    }


    /**
     * Tests equals and hashCode.
     */
    public void testEquals() {
        Exercise sameSquat = new Exercise("SQUAT", "Legs", null, null, 1, 1);
        assertTrue(squat.equals(squat));
        assertTrue(squat.equals(sameSquat));
        assertEquals(squat.hashCode(), sameSquat.hashCode());
        assertFalse(squat.equals(bench));
        assertFalse(squat.equals(null));
        assertFalse(squat.equals("Squat"));
    }


    /**
     * Tests toString.
     */
    public void testToString() {
        assertEquals("Bench Press 4 x 8", bench.toString());
    }
}
