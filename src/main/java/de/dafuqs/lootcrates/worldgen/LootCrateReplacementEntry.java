package de.dafuqs.lootcrates.worldgen;

import de.dafuqs.lootcrates.enums.LootCrateRarity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class LootCrateReplacementEntry {

    @Nullable
    LootCrateRarity lootCrateRarity;
    @Nullable
    Identifier lootTable;
    boolean oncePerPlayer;
    int replenishTimeTicks;
    LockType lockType;
    int weight;

    public LootCrateReplacementEntry(@Nullable LootCrateRarity lootCrateRarity, @Nullable Identifier lootTable, boolean oncePerPlayer, int replenishTimeTicks, LockType lockType, int weight) {
        this.lootCrateRarity = lootCrateRarity;
        this.lootTable = lootTable;
        this.oncePerPlayer = oncePerPlayer;
        this.replenishTimeTicks = replenishTimeTicks;
        this.lockType = lockType;
        this.weight = weight;
    }
    
}