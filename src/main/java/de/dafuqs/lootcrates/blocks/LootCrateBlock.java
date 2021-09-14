package de.dafuqs.lootcrates.blocks;

import de.dafuqs.lootcrates.LootCrateAtlas;
import de.dafuqs.lootcrates.enums.BlockBreakAction;
import de.dafuqs.lootcrates.enums.LootCrateRarity;
import de.dafuqs.lootcrates.enums.ScheduledTickEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public abstract class LootCrateBlock extends BlockWithEntity {

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    protected LootCrateBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LootCrateBlockEntity) {
            LootCrateBlockEntity lootCrateBlockEntity = (LootCrateBlockEntity) blockEntity;
            if(lootCrateBlockEntity.isLocked()) {
                for(ItemStack itemStack : player.getItemsHand()) {
                    if(lootCrateBlockEntity.doesUnlock(itemStack.getItem())) {
                        if(!player.isCreative()) {
                            if (lootCrateBlockEntity.doesConsumeKeyOnUnlock()) {
                                itemStack.decrement(1);
                            }
                        }
                        lootCrateBlockEntity.unlock();
                        return ActionResult.CONSUME; // just consume the action and play unlock sound.
                    }
                }
                if(!world.isClient()) {
                    LootCrateRarity rarity = getCrateRarity(world, pos);
                    TranslatableText translatableText = LootCrateAtlas.getKeyNeededTooltip(rarity);
                    player.sendMessage(translatableText, false);
                }
                playSound(world, pos, SoundEvents.BLOCK_CHEST_LOCKED);
                return ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;
    }

    protected static LootCrateRarity getCrateRarity(World world, BlockPos blockPos) {
        Block block = world.getBlockState(blockPos).getBlock();
        return getCrateRarity(block);
    }

    protected static LootCrateRarity getCrateRarity(Block block) {
        return LootCrateAtlas.getCrateRarity(block);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LootCrateBlockEntity) {
            if (itemStack.hasCustomName()) {
                ((LootCrateBlockEntity) blockEntity).setCustomName(itemStack.getName());
            }
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.getBlockTickScheduler().schedule(pos, this, getRandomTickTime(world.random));

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LootCrateBlockEntity) {
            ScheduledTickEvent scheduledTickEvent = ((LootCrateBlockEntity) blockEntity).getRandomTickEvent();

            if (scheduledTickEvent == ScheduledTickEvent.FIRE) {
                if (world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
                    int xOffset = 2 - random.nextInt(5);
                    int yOffset = 1 - random.nextInt(3);
                    int zOffset = 2 - random.nextInt(5);

                    BlockPos targetPos = pos.add(xOffset, yOffset, zOffset);
                    if (world.getBlockState(targetPos).isAir() && world.getBlockState(targetPos.down()).getMaterial().isSolid()) {
                        world.setBlockState(targetPos, Blocks.FIRE.getDefaultState());
                    }
                }
            }
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LootCrateBlockEntity) {
            ScheduledTickEvent scheduledTickEvent = ((LootCrateBlockEntity) blockEntity).getRandomTickEvent();

            if(scheduledTickEvent != ScheduledTickEvent.NONE) {
                world.getBlockTickScheduler().schedule(pos, this, getRandomTickTime(world.random));
            }
        }
    }

    // faster than fire (30+ 0-10)
    private static int getRandomTickTime(Random random) {
        return 20 + random.nextInt(10);
    }

    /**
     * Called when the block is broken
     * Used to add Block NBT to the item stack
     * @param world The world, client and server
     * @param pos BlockPos where the block is
     * @param state The block state
     * @param player The player that has broken the block
     */
    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LootCrateBlockEntity) {

            // if creative: If there is block data add those and drop a block with all those tags
            // No tags = No drop. Just like vanilla shulker chests
            if (!world.isClient) {
                BlockBreakAction blockBreakAction = getBlockBreakAction();
                if (player.isCreative()) {
                    dropAsItemWithTags(world, pos, true);
                } else {
                    if (blockBreakAction == BlockBreakAction.DESTROY_AND_SCATTER_INVENTORY) {
                        ItemScatterer.spawn(world, pos, (Inventory) blockEntity);
                    } else if (blockBreakAction == BlockBreakAction.DROP_AND_SCATTER_INVENTORY) {
                        ItemScatterer.spawn(world, pos, (Inventory) blockEntity);
                        dropAsItemWithTags(world, pos, false);
                    } else if (blockBreakAction == BlockBreakAction.KEEP_INVENTORY) {
                        dropAsItemWithTags(world, pos, true);
                    }
                }
            }
        }

        super.onBreak(world, pos, state, player);
    }

    public void dropAsItemWithTags(World world, BlockPos pos, boolean keepInventory) {
        LootCrateBlockEntity lootCrateBlockEntity = (LootCrateBlockEntity) world.getBlockEntity(pos);
        if(lootCrateBlockEntity != null) {
            ItemStack itemStack = new ItemStack(this);

            boolean shouldDropItem = false;

            if (lootCrateBlockEntity.hasCustomName()) {
                itemStack.setCustomName(lootCrateBlockEntity.getCustomName());
                shouldDropItem = true;
            }

            NbtCompound compoundTag = lootCrateBlockEntity.addLootCrateBlockTags(new NbtCompound());
            if (!compoundTag.isEmpty()) {
                itemStack.setSubNbt("BlockEntityTag", compoundTag);
                shouldDropItem = true;
            }

            if (keepInventory && !lootCrateBlockEntity.isEmpty()) {
                lootCrateBlockEntity.serializeInventory(compoundTag);
                shouldDropItem = true;
            }

            if (shouldDropItem) {
                ItemEntity itemEntity = new ItemEntity(world, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, itemStack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            }
        }
    }

    protected abstract BlockBreakAction getBlockBreakAction();

    @Override
    @Environment(EnvType.CLIENT)
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        ItemStack itemStack = super.getPickStack(world, pos, state);
        LootCrateBlockEntity lootCrateBlockEntity = (LootCrateBlockEntity)world.getBlockEntity(pos);

        if(lootCrateBlockEntity != null) {
            if (lootCrateBlockEntity.hasCustomName()) {
                itemStack.setCustomName(lootCrateBlockEntity.getCustomName());
            }

            NbtCompound compoundTag = lootCrateBlockEntity.addLootCrateBlockTags(new NbtCompound());
            if (!compoundTag.isEmpty()) {
                itemStack.setSubNbt("BlockEntityTag", compoundTag);
            }
        }
        return itemStack;
    }

    protected void playSound(World world, BlockPos blockPos, SoundEvent soundEvent) {
        double d = blockPos.getX() + 0.5D;
        double e = blockPos.getY() + 0.5D;
        double f = blockPos.getZ() + 0.5D;
        world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
    }


}
