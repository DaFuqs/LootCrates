package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.LootCrateAtlas;
import de.dafuqs.lootcrates.blocks.LootCrateBlock;
import de.dafuqs.lootcrates.enums.LootCrateTagNames;
import de.dafuqs.lootcrates.enums.ScheduledTickEvent;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
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
                if(world.isClient) {
                    Random random = world.getRandom();
                    if(random.nextInt(50) == 0) {
                        entity.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 0.4F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.2F);
                    }
                } else {
                    int r = world.getRandom().nextInt(120);
                    if (r == 0) {
                        entity.setFireTicks(25);
                    } else if (r < 3) {
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
