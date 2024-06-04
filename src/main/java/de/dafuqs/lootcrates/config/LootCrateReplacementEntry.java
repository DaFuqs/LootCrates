package de.dafuqs.lootcrates.config;

import de.dafuqs.lootcrates.components.*;
import de.dafuqs.lootcrates.enums.*;
import net.minecraft.loot.*;
import net.minecraft.registry.*;
import org.jetbrains.annotations.*;

public class LootCrateReplacementEntry {
    
    @Nullable
    public final LootCrateRarity lootCrateRarity;
    @Nullable
    public final RegistryKey<LootTable> lootTable;
    
    public final LootCrateLootComponent.LockMode lockMode;
    public final LootCrateLootComponent.ReplenishTimeScale replenishTimeScale;
    public final LootCrateLootComponent.InventoryDeletionMode inventoryDeletionMode;
    public final boolean trackedPerPlayer;
    public final int replenishTimeTicks;
    
    public final int weight;

    public LootCrateReplacementEntry(@Nullable LootCrateRarity lootCrateRarity, @Nullable RegistryKey<LootTable> lootTable, LootCrateLootComponent.ReplenishTimeScale replenishTimeScale, int replenishTimeTicks, LootCrateLootComponent.LockMode lockMode, LootCrateLootComponent.InventoryDeletionMode inventoryDeletionMode, boolean trackedPerPlayer, int weight) {
        this.lootCrateRarity = lootCrateRarity;
        this.lootTable = lootTable;
        
        this.lockMode = lockMode;
        this.replenishTimeScale = replenishTimeScale;
        this.inventoryDeletionMode = inventoryDeletionMode;
        this.trackedPerPlayer = trackedPerPlayer;
        this.replenishTimeTicks = replenishTimeTicks;
        
        this.weight = weight;
    }
    
}