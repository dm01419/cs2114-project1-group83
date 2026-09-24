import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for Profile: a normal case and a bad-input case for each method.
 */
class ProfileTest {
    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = new Profile();
    }

    @Test
    void setFitnessGoalStoresGoal() {
        profile.setFitnessGoal("Strength");
        assertEquals("Strength", profile.getFitnessGoal());
    }

    @Test
    void emptyFitnessGoalIsRejectedAndGoalUnchanged() {
        profile.setFitnessGoal("Strength");
        assertThrows(IllegalArgumentException.class,
                () -> profile.setFitnessGoal(" "));
        assertEquals("Strength", profile.getFitnessGoal());
    }

    @Test
    void setWorkoutDurationStoresMinutes() {
        profile.setWorkoutDuration(60);
        assertEquals(60, profile.getWorkoutDuration());
    }

    @Test
    void negativeDurationIsRejectedAndDurationUnchanged() {
        profile.setWorkoutDuration(45);
        assertThrows(IllegalArgumentException.class,
                () -> profile.setWorkoutDuration(-30));
        assertEquals(45, profile.getWorkoutDuration());
    }

    @Test
    void durationLimitsAreEnforced() {
        profile.setWorkoutDuration(Profile.MIN_DURATION_MINUTES);
        assertEquals(10, profile.getWorkoutDuration());
        profile.setWorkoutDuration(Profile.MAX_DURATION_MINUTES);
        assertEquals(180, profile.getWorkoutDuration());
        for (int bad : new int[] {0, 9, 181, 50000, Integer.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class,
                    () -> profile.setWorkoutDuration(bad));
        }
        assertEquals(180, profile.getWorkoutDuration());
    }

    @Test
    void fitnessGoalMustBeKnownGoal() {
        profile.setFitnessGoal("muscle GAIN");
        assertEquals("Muscle Gain", profile.getFitnessGoal());
        assertThrows(IllegalArgumentException.class,
                () -> profile.setFitnessGoal("Get huge"));
        assertEquals("Muscle Gain", profile.getFitnessGoal());
        assertEquals("General Fitness", Profile.normalizeGoal(" general fitness "));
        assertNull(Profile.normalizeGoal(null));
    }

    @Test
    void textLongerThanLimitIsRejected() {
        String max = "x".repeat(Profile.MAX_TEXT_LENGTH);
        String tooLong = max + "x";
        profile.addEquipment(max);
        assertTrue(profile.getEquipment().contains(max));
        assertThrows(IllegalArgumentException.class, () -> profile.addEquipment(tooLong));
        assertThrows(IllegalArgumentException.class, () -> profile.addLimit(tooLong));
        assertThrows(IllegalArgumentException.class,
                () -> profile.addExercisePreference(tooLong));
        assertThrows(IllegalArgumentException.class,
                () -> profile.setFitnessGoal(tooLong));
        assertEquals(1, profile.getEquipment().size());
    }

    @Test
    void listsStopAtMaxSize() {
        for (int i = 1; i <= Profile.MAX_LIST_SIZE; i++) {
            profile.addLimit("Limit " + i);
        }
        assertEquals(20, profile.getLimitations().size());
        assertThrows(IllegalArgumentException.class, () -> profile.addLimit("One more"));
        profile.addLimit("limit 1"); // duplicate is still ignored, not an error
        assertEquals(20, profile.getLimitations().size());
    }

    @Test
    void addLimitAddsLimitation() {
        profile.addLimit("Knee injury");
        assertTrue(profile.getLimitations().contains("Knee injury"));
    }

    @Test
    void emptyLimitIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> profile.addLimit(""));
        assertEquals(0, profile.getLimitations().size());
    }

    @Test
    void addEquipmentAddsEquipment() {
        profile.addEquipment("Dumbbells");
        assertTrue(profile.getEquipment().contains("Dumbbells"));
    }

    @Test
    void emptyEquipmentIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> profile.addEquipment(""));
        assertEquals(0, profile.getEquipment().size());
    }

    @Test
    void addPreferredWorkoutDayAddsDay() {
        profile.addPreferredWorkoutDay("monday");
        assertTrue(profile.getPreferredWorkoutDays().contains("Monday"));
    }

    @Test
    void invalidDayIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> profile.addPreferredWorkoutDay("Funday"));
        assertEquals(0, profile.getPreferredWorkoutDays().size());
    }

    @Test
    void addExercisePreferenceAddsExercise() {
        profile.addExercisePreference("Bench Press");
        assertTrue(profile.getExercisePreferences().contains("Bench Press"));
    }

    @Test
    void emptyExercisePreferenceIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> profile.addExercisePreference(""));
        assertEquals(0, profile.getExercisePreferences().size());
    }

    @Test
    void getFitnessGoalReturnsStoredGoal() {
        profile.setFitnessGoal("Muscle Gain");
        assertEquals("Muscle Gain", profile.getFitnessGoal());
    }

    @Test
    void getFitnessGoalWithoutGoalReturnsNull() {
        assertNull(profile.getFitnessGoal());
    }

    @Test
    void getWorkoutDurationWithoutValueReturnsDefault() {
        assertEquals(Profile.DEFAULT_DURATION_MINUTES,
                profile.getWorkoutDuration());
    }
}
