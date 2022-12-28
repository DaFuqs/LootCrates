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
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LootCrateBlock extends BlockWithEntity {

    protected LootCrateBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, @NotNull World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LootCrateBlockEntity lootCrateBlockEntity) {
            lootCrateBlockEntity.relockIfNecessary(player);
            if(!lootCrateBlockEntity.isUnlocked(player)) {
                for(ItemStack itemStack : player.getHandItems()) {
                    if(lootCrateBlockEntity.doesItemUnlock(itemStack.getItem())) {
                        if(!player.isCreative()) {
                            if (lootCrateBlockEntity.getLockType().consumesKey()) {
                                itemStack.decrement(1);
                            }
                        }
                        lootCrateBlockEntity.unlock(world, player);
                        return ActionResult.CONSUME; // just consume the action and play unlock sound.
                    }
                }
                if(!world.isClient()) {
                    LootCrateRarity rarity = getCrateRarity(world, pos);
                    Text translatableText = LootCrateAtlas.getKeyNeededTooltip(rarity);
                    player.sendMessage(translatableText, false);
                }
                playSound(world, pos, SoundEvents.BLOCK_CHEST_LOCKED);
                return ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;
    }

    protected static LootCrateRarity getCrateRarity(@NotNull World world, BlockPos blockPos) {
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

    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    public int getWeakRedstonePower(BlockState state, @NotNull BlockView world, BlockPos pos, Direction direction) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LootCrateBlockEntity lootCrateBlockEntity) {
            if (lootCrateBlockEntity.isTrapped()) {
                return MathHelper.clamp(getPlayersLookingInCrateCount(world, pos), 0, 15);
            }
        }
        return 0;
    }

    public int getStrongRedstonePower(BlockState state, @NotNull BlockView world, BlockPos pos, Direction direction) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LootCrateBlockEntity lootCrateBlockEntity) {
            if (lootCrateBlockEntity.isTrapped()) {
                return direction == Direction.UP ? state.getWeakRedstonePower(world, pos, direction) : 0;
            }
        }
        return 0;
    }

    public static int getPlayersLookingInCrateCount(@NotNull BlockView world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.hasBlockEntity()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof LootCrateBlockEntity lootCrateBlockEntity) {
                return lootCrateBlockEntity.getCurrentLookingPlayers(world, pos);
            }
        }
        return 0;
    }

    @Override
    public void onPlaced(@NotNull World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LootCrateBlockEntity) {
            if (itemStack.hasCustomName()) {
                ((LootCrateBlockEntity) blockEntity).setCustomName(itemStack.getName());
            }
        }
    }

    @Override
    public void scheduledTick(BlockState state, @NotNull ServerWorld world, BlockPos pos, Random random) {
        world.scheduleBlockTick(pos, this, getRandomTickTime(world.random));

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
                world.scheduleBlockTick(pos, this, getRandomTickTime(world.random));
            }
        }
    }

    // faster than fire (30+ 0-10)
    private static int getRandomTickTime(@NotNull Random random) {
        return 20 + random.nextInt(10);
    }
    
    @Override
    public void onStateReplaced(BlockState state, @NotNull World world, BlockPos pos, BlockState newState, boolean moved) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LootCrateBlockEntity lootCrateBlockEntity) {
            // if creative: If there is block data add those and drop a block with all those tags
            // No tags = No drop. Just like vanilla shulker chests
            if (!world.isClient) {
                BlockBreakAction blockBreakAction = getBlockBreakAction();
                if (blockBreakAction == BlockBreakAction.DESTROY_AND_SCATTER_INVENTORY) {
                    ItemScatterer.spawn(world, pos, lootCrateBlockEntity);
                } else if (blockBreakAction == BlockBreakAction.DROP_AND_SCATTER_INVENTORY) {
                    ItemScatterer.spawn(world, pos, lootCrateBlockEntity);
                    dropAsItemWithTags(world, pos, false);
                } else if (blockBreakAction == BlockBreakAction.KEEP_INVENTORY) {
                    dropAsItemWithTags(world, pos, true);
                }
            }
        }
    
        world.updateComparators(pos, state.getBlock());
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    public int getComparatorOutput(BlockState state, @NotNull World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput((Inventory)world.getBlockEntity(pos));
    }

    public void dropAsItemWithTags(@NotNull World world, BlockPos pos, boolean keepInventory) {
        LootCrateBlockEntity lootCrateBlockEntity = (LootCrateBlockEntity) world.getBlockEntity(pos);
        if(lootCrateBlockEntity != null) {
            ItemStack itemStack = new ItemStack(this);

            if (lootCrateBlockEntity.hasCustomName()) {
                itemStack.setCustomName(lootCrateBlockEntity.getCustomName());
            }

            NbtCompound compoundTag = lootCrateBlockEntity.putLootCrateBlockTags(new NbtCompound());
            if (!compoundTag.isEmpty()) {
                itemStack.setSubNbt("BlockEntityTag", compoundTag);
            }

            if (keepInventory && !lootCrateBlockEntity.isEmpty()) {
                // drop all contents that are considered to be non-nestable to prevent book banning
                for(int i = 0; i < lootCrateBlockEntity.inventory.size(); i++) {
                    if(!lootCrateBlockEntity.inventory.get(i).getItem().canBeNested()) {
                        ItemScatterer.spawn(lootCrateBlockEntity.getWorld(),
                                lootCrateBlockEntity.getPos().getX(),
                                lootCrateBlockEntity.getPos().getY(),
                                lootCrateBlockEntity.getPos().getZ(),
                                lootCrateBlockEntity.inventory.get(i));
                        lootCrateBlockEntity.inventory.set(i, ItemStack.EMPTY);
                    }
                }

                lootCrateBlockEntity.serializeInventory(compoundTag);
            }

            ItemEntity itemEntity = new ItemEntity(world, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, itemStack);
            itemEntity.setToDefaultPickupDelay();
            world.spawnEntity(itemEntity);
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

            NbtCompound compoundTag = lootCrateBlockEntity.putLootCrateBlockTags(new NbtCompound());
            if (!compoundTag.isEmpty()) {
                itemStack.setSubNbt("BlockEntityTag", compoundTag);
            }
        }
        return itemStack;
    }

    protected void playSound(@NotNull World world, @NotNull BlockPos blockPos, SoundEvent soundEvent) {
        double d = blockPos.getX() + 0.5D;
        double e = blockPos.getY() + 0.5D;
        double f = blockPos.getZ() + 0.5D;
        world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
    }
    
}
