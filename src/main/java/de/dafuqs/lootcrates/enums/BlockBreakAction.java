package de.dafuqs.lootcrates.enums;

public enum BlockBreakAction {
        KEEP_INVENTORY, // shulker boxes. Drop as single item with the inventory stored as tag
        DROP_AND_SCATTER_INVENTORY, // chests in survival: drop the block and it's inventory
        DESTROY_AND_SCATTER_INVENTORY // The crate gets destroyed, but it's inventory will get scattered on the ground
}