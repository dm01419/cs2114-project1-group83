import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for Exercise: a normal case and a bad-input case for each method.
 */
class ExerciseTest {
    private Exercise squat;
    private Exercise bench;
    private Exercise pushUp;
    private Profile profile;

    @BeforeEach
    void setUp() {
        squat = new Exercise("Squat", "Quadriceps", "Hamstrings", "Barbell",
                3, 10);
        squat.addConflictingLimitation("knee");
        bench = new Exercise("Bench Press", "Chest", "Triceps", "Barbell", 4, 8);
        pushUp = new Exercise("Push Up", "Chest", null, "", 3, 15);
        profile = new Profile();
    }

    @Test
    void constructorStoresAndTrimsValues() {
        Exercise e = new Exercise("  Deadlift ", " Back ", " Glutes ",
                " Barbell ", 5, 5);
        assertEquals("Deadlift", e.getName());
        assertEquals("Back", e.getPrimaryMuscleGroup());
        assertEquals("Glutes", e.getSecondaryMuscleGroup());
        assertEquals("Barbell", e.getRequiredEquipment());
        assertEquals(5, e.getSets());
        assertEquals(5, e.getReps());
    }

    @Test
    void getNameReturnsName() {
        assertEquals("Squat", squat.getName());
    }

    @Test
    void emptyOrNullNameIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Exercise("", "Chest", null, null, 3, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new Exercise(null, "Chest", null, null, 3, 10));
    }

    @Test
    void getPrimaryMuscleGroupReturnsGroup() {
        assertEquals("Quadriceps", squat.getPrimaryMuscleGroup());
    }

    @Test
    void missingPrimaryMuscleGroupIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Exercise("Squat", "   ", null, null, 3, 10));
    }

    @Test
    void getSecondaryMuscleGroupReturnsGroupOrNone() {
        assertEquals("Hamstrings", squat.getSecondaryMuscleGroup());
        assertEquals(Exercise.NONE, pushUp.getSecondaryMuscleGroup());
    }

    @Test
    void getRequiredEquipmentReturnsEquipmentOrNone() {
        assertEquals("Barbell", bench.getRequiredEquipment());
        assertEquals(Exercise.NONE, pushUp.getRequiredEquipment());
    }

    @Test
    void setsAndRepsCanBeChanged() {
        bench.setSets(5);
        bench.setReps(6);
        assertEquals(5, bench.getSets());
        assertEquals(6, bench.getReps());
    }

    @Test
    void nonPositiveSetsAndRepsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> bench.setSets(0));
        assertThrows(IllegalArgumentException.class, () -> bench.setReps(-3));
        assertEquals(4, bench.getSets());
        assertEquals(8, bench.getReps());
        assertThrows(IllegalArgumentException.class,
                () -> new Exercise("Row", "Back", null, null, 0, 10));
    }

    @Test
    void addConflictingLimitationIgnoresBlanksAndDuplicates() {
        assertTrue(bench.addConflictingLimitation(" Shoulder "));
        assertFalse(bench.addConflictingLimitation("shoulder"));
        assertFalse(bench.addConflictingLimitation(""));
        assertFalse(bench.addConflictingLimitation(null));
        assertEquals(1, bench.getConflictingLimitations().size());
        assertTrue(bench.getConflictingLimitations().contains("shoulder"));
    }

    @Test
    void canPerformWithEquipmentAndNoConflicts() {
        profile.addEquipment("barbell");
        assertTrue(squat.canPerformWith(profile));
        assertTrue(bench.canPerformWith(profile));
        assertTrue(pushUp.canPerformWith(new Profile()));
    }

    @Test
    void equipmentMatchIgnoresCaseAndPlural() {
        Exercise curl = new Exercise("Curl", "Arms", null, "Dumbbell", 3, 10);
        profile.addEquipment("DUMBBELLS");
        assertTrue(curl.hasEquipmentFor(profile));
    }

    @Test
    void cannotPerformWithoutEquipmentOrWithConflict() {
        assertFalse(bench.canPerformWith(profile));
        assertFalse(bench.hasEquipmentFor(profile));

        profile.addEquipment("Dumbbells");
        assertFalse(bench.canPerformWith(profile));

        profile.addEquipment("Barbell");
        profile.addLimit("Knee injury");
        assertFalse(squat.canPerformWith(profile));
        assertFalse(squat.isSafeFor(profile));
        assertTrue(bench.canPerformWith(profile));

        assertFalse(squat.canPerformWith(null));
        assertFalse(squat.isSafeFor(null));
        assertFalse(bench.hasEquipmentFor(null));
    }

    @Test
    void unrelatedLimitationDoesNotBlock() {
        profile.addEquipment("Barbell");
        profile.addLimit("Shoulder injury");
        assertTrue(squat.canPerformWith(profile));
    }

    @Test
    void nullListsFromProfileAreHandled() {
        Profile nullProfile = new Profile() {
            @Override
            public List<String> getEquipment() {
                return null;
            }

            @Override
            public List<String> getLimitations() {
                return null;
            }
        };
        assertFalse(bench.canPerformWith(nullProfile));
        assertTrue(pushUp.canPerformWith(nullProfile));
    }

    @Test
    void copyHasOwnSetsAndRepsAndSameLimits() {
        Exercise copy = squat.copy(5, 5);
        assertEquals(5, copy.getSets());
        assertEquals(3, squat.getSets());
        assertEquals(squat.getConflictingLimitations(),
                copy.getConflictingLimitations());
        assertEquals(squat, copy);
    }

    @Test
    void equalsComparesNamesIgnoringCase() {
        Exercise sameSquat = new Exercise("SQUAT", "Legs", null, null, 1, 1);
        assertEquals(squat, squat);
        assertEquals(squat, sameSquat);
        assertEquals(squat.hashCode(), sameSquat.hashCode());
        assertNotEquals(squat, bench);
        assertNotEquals(squat, null);
        assertNotEquals(squat, "Squat");
    }

    @Test
    void toStringShowsSetsAndReps() {
        assertEquals("Bench Press 4 x 8", bench.toString());
    }
}
