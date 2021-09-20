package de.dafuqs.lootcrates.worldgen;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class LootCrateReplacementPosition {

    public RegistryKey<World> worldKey;
    public BlockPos blockPos;
    public Identifier lootTable;
    public long lootTableSeed;

    public LootCrateReplacementPosition(RegistryKey<World> worldKey, BlockPos blockPos, Identifier lootTable, long lootTableSeed) {
        this.worldKey = worldKey;
        this.blockPos = blockPos;
        this.lootTable = lootTable;
        this.lootTableSeed = lootTableSeed;
    }

}
