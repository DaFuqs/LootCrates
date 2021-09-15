package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.LootCrateAtlas;
import de.dafuqs.lootcrates.blocks.LootCrateBlock;
import de.dafuqs.lootcrates.enums.ScheduledTickEvent;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.Random;

public class TickingLootCrateItem extends LootCrateItem {

    public TickingLootCrateItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if(world != null && entity != null) {
            ScheduledTickEvent scheduledTickEvent = LootCrateAtlas.getRandomTickEvent((LootCrateBlock) ((BlockItem) stack.getItem()).getBlock());

            if (scheduledTickEvent == ScheduledTickEvent.FIRE) {
                // play fire sound, set player and surroundings on fire
                if (world.isClient) {
                    if(world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
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
