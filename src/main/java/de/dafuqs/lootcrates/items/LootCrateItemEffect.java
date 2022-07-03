package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.enums.ScheduledTickEvent;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public interface LootCrateItemEffect {
	
	default void doInventoryTick(World world, Entity entity, ScheduledTickEvent scheduledTickEvent) {
		if (world != null && entity != null) {
			if (scheduledTickEvent == ScheduledTickEvent.FIRE) {
				// play fire sound, set player and surroundings on fire
				if (world.isClient) {
					if (world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
						Random random = world.getRandom();
						if (random.nextInt(50) == 0) {
							entity.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 0.4F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.2F);
						}
					}
				} else {
					int r = world.getRandom().nextInt(120);
					if (r < 2) {
						entity.setFireTicks(25);
					} else if (r < 3) {
						if (world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
							Random random = world.getRandom();
							int xOffset = 3 - random.nextInt(7);
							int yOffset = 1 - random.nextInt(3);
							int zOffset = 3 - random.nextInt(7);
							
							BlockPos targetPos = new BlockPos(entity.getPos()).add(xOffset, yOffset, zOffset);
							if (world.getBlockState(targetPos).isAir() && world.getBlockState(targetPos.down()).getMaterial().isSolid()) {
								world.setBlockState(targetPos, Blocks.FIRE.getDefaultState());
							}
						}
					}
				}
			}
		}
	}
	
	
}
