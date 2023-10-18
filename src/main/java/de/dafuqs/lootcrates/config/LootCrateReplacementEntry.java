package de.dafuqs.lootcrates.config;

import de.dafuqs.lootcrates.blocks.modes.*;
import de.dafuqs.lootcrates.enums.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

public class LootCrateReplacementEntry {
    
    @Nullable
    public final LootCrateRarity lootCrateRarity;
    @Nullable
    public final Identifier lootTable;
    
    public final LockMode lockMode;
    public final ReplenishMode replenishMode;
    public final InventoryDeletionMode inventoryDeletionMode;
    public final boolean trackedPerPlayer;
    public final int replenishTimeTicks;
    
    public final int weight;

    public LootCrateReplacementEntry(@Nullable LootCrateRarity lootCrateRarity, @Nullable Identifier lootTable, ReplenishMode replenishMode, int replenishTimeTicks, LockMode lockMode, InventoryDeletionMode inventoryDeletionMode, boolean trackedPerPlayer, int weight) {
        this.lootCrateRarity = lootCrateRarity;
        this.lootTable = lootTable;
        
        this.lockMode = lockMode;
        this.replenishMode = replenishMode;
        this.inventoryDeletionMode = inventoryDeletionMode;
        this.trackedPerPlayer = trackedPerPlayer;
        this.replenishTimeTicks = replenishTimeTicks;
        
        this.weight = weight;
    }
    
}