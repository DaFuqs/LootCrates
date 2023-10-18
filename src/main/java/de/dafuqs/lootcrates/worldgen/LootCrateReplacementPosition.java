package de.dafuqs.lootcrates.worldgen;

import net.minecraft.server.world.*;
import net.minecraft.util.math.*;

public class LootCrateReplacementPosition {

    public final ServerWorld world;
    public final BlockPos blockPos;

    public LootCrateReplacementPosition(ServerWorld world, BlockPos blockPos) {
        this.world = world;
        this.blockPos = blockPos;
    }

}
