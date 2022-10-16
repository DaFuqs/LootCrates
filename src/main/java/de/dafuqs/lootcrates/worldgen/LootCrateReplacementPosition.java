package de.dafuqs.lootcrates.worldgen;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class LootCrateReplacementPosition {

    public ServerWorld world;
    public BlockPos blockPos;

    public LootCrateReplacementPosition(ServerWorld world, BlockPos blockPos) {
        this.world = world;
        this.blockPos = blockPos;
    }

}
