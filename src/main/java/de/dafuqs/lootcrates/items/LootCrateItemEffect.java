package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.enums.*;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.server.world.*;
import net.minecraft.sound.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.*;
import net.minecraft.world.*;

public interface LootCrateItemEffect {
	
	default void doInventoryTick(World world, Entity entity, ScheduledTickEvent scheduledTickEvent) {
		if (world != null && entity != null) {
			// play fire sound, set player and surroundings on fire
			if (scheduledTickEvent == ScheduledTickEvent.FIRE && world instanceof ServerWorld serverWorld) {
				boolean doFireTicks = serverWorld.getGameRules().getBoolean(GameRules.DO_FIRE_TICK);
				
				Random random = serverWorld.getRandom();
				if (doFireTicks) {
					if (random.nextInt(50) == 0) {
						entity.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 0.4F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.2F);
					}
				}
				
				int r = world.getRandom().nextInt(120);
				if (r < 2) {
					entity.setFireTicks(25);
				} else if (r < 3) {
					if (doFireTicks) {
						int xOffset = 3 - random.nextInt(7);
						int yOffset = 1 - random.nextInt(3);
						int zOffset = 3 - random.nextInt(7);
						
						BlockPos targetPos = BlockPos.ofFloored(entity.getPos()).add(xOffset, yOffset, zOffset);
						if (world.getBlockState(targetPos).isAir() && world.getBlockState(targetPos.down()).isSolid()) {
							world.setBlockState(targetPos, Blocks.FIRE.getDefaultState());
						}
					}
				}
			}
		}
	}
	
}
