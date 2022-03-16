package de.dafuqs.lootcrates.config;

import de.dafuqs.lootcrates.blocks.modes.LockMode;
import de.dafuqs.lootcrates.enums.LootCrateRarity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class LootCrateReplacementEntry {

    @Nullable
    public LootCrateRarity lootCrateRarity;
    @Nullable
    public Identifier lootTable;
    public boolean oncePerPlayer;
    public int replenishTimeTicks;
    public LockMode lockMode;
    public int weight;

    public LootCrateReplacementEntry(@Nullable LootCrateRarity lootCrateRarity, @Nullable Identifier lootTable, boolean oncePerPlayer, int replenishTimeTicks, LockMode lockMode, int weight) {
        this.lootCrateRarity = lootCrateRarity;
        this.lootTable = lootTable;
        this.oncePerPlayer = oncePerPlayer;
        this.replenishTimeTicks = replenishTimeTicks;
        this.lockMode = lockMode;
        this.weight = weight;
    }
    
}