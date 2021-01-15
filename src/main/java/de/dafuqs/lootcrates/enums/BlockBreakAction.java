package de.dafuqs.lootcrates.enums;

public enum BlockBreakAction {
        KEEP_INVENTORY, // shulker boxes
        DROP_AND_SCATTER_ITEMS, // chests in survival: block drops and items get spilled on the ground
        DO_NOT_DROP_AND_SCATTER_ITEMS // chests when in creative: block drops and items get spilled on the ground
}