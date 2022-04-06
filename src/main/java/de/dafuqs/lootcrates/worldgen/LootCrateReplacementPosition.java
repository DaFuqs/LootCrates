package de.dafuqs.lootcrates.worldgen;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class LootCrateReplacementPosition {

    public RegistryKey<World> worldKey;
    public BlockPos blockPos;

    public LootCrateReplacementPosition(RegistryKey<World> worldKey, BlockPos blockPos) {
        this.worldKey = worldKey;
        this.blockPos = blockPos;
    }

}
