package workoutplanner;

import java.util.ArrayList;
import java.util.List;

/**
 * PLACEHOLDER so Exercise compiles. Pratyay owns the real Profile class;
 * when merging, keep Pratyay's version but make sure it still has
 * addEquipment, addLimit, getEquipment and getLimitations.
 */
public class Profile {
    private List<String> equipment;
    private List<String> limitations;

    /**
     * Creates an empty profile.
     */
    public Profile() {
        equipment = new ArrayList<String>();
        limitations = new ArrayList<String>();
    }


    /**
     * Adds a piece of equipment available to the user.
     *
     * @param item
     *            the equipment
     */
    public void addEquipment(String item) {
        equipment.add(item);
    }


    /**
     * Adds a physical limit or injury.
     *
     * @param limit
     *            the limitation
     */
    public void addLimit(String limit) {
        limitations.add(limit);
    }


    /**
     * Returns the user's equipment.
     *
     * @return the equipment list
     */
    public List<String> getEquipment() {
        return equipment;
    }


    /**
     * Returns the user's limitations.
     *
     * @return the limitations list
     */
    public List<String> getLimitations() {
        return limitations;
    }
}
