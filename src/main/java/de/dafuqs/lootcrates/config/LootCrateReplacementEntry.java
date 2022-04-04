package de.dafuqs.lootcrates.config;

import de.dafuqs.lootcrates.blocks.modes.InventoryDeletionMode;
import de.dafuqs.lootcrates.blocks.modes.LockMode;
import de.dafuqs.lootcrates.blocks.modes.ReplenishMode;
import de.dafuqs.lootcrates.enums.LootCrateRarity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class LootCrateReplacementEntry {
    
    @Nullable
    public LootCrateRarity lootCrateRarity;
    @Nullable
    public Identifier lootTable;
    
    public LockMode lockMode;
    public ReplenishMode replenishMode;
    public InventoryDeletionMode inventoryDeletionMode;
    public boolean trackedPerPlayer;
    public int replenishTimeTicks;
    
    public int weight;

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