package de.dafuqs.lootcrates.blocks;

import de.dafuqs.lootcrates.LootCratesBlocks;
import de.dafuqs.lootcrates.enums.BlockBreakAction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Rarity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static de.dafuqs.lootcrates.blocks.shulker.ShulkerLootCrateBlock.CONTENTS;

public abstract class LootCrateBlock extends BlockWithEntity {

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
                        if (lootCrateBlockEntity.doesConsumeKeyOnUnlock()) {
                            itemStack.decrement(1);
                        }
                        lootCrateBlockEntity.unlock();
                        return ActionResult.PASS;
                    }
                }
                if(!world.isClient()) {
                    Rarity rarity = getCrateRarity(world, pos);
                    String translatableMessageString;
                    switch (rarity) {
                        case COMMON:
                            translatableMessageString = "message.lootcrates.common_key_needed_to_unlock";
                            break;
                        case UNCOMMON:
                            translatableMessageString = "message.lootcrates.uncommon_key_needed_to_unlock";
                            break;
                        case RARE:
                            translatableMessageString = "message.lootcrates.rare_key_needed_to_unlock";
                            break;
                        default:
                            translatableMessageString = "message.lootcrates.epic_key_needed_to_unlock";
                            break;
                    }
                    player.sendMessage(new TranslatableText(translatableMessageString), false);
                }
                return ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;
    }

    protected static Rarity getCrateRarity(World world, BlockPos blockPos) {
        Block block = world.getBlockState(blockPos).getBlock();
        return getCrateRarity(block);
    }

    protected static Rarity getCrateRarity(Block block) {
        if (block.equals(LootCratesBlocks.COMMON_CHEST_LOOT_CRATE) || block.equals(LootCratesBlocks.COMMON_SHULKER_LOOT_CRATE)) {
            return Rarity.COMMON;
        } else if (block.equals(LootCratesBlocks.UNCOMMON_CHEST_LOOT_CRATE) || block.equals(LootCratesBlocks.UNCOMMON_SHULKER_LOOT_CRATE)) {
            return Rarity.UNCOMMON;
        } else if (block.equals(LootCratesBlocks.RARE_CHEST_LOOT_CRATE) || block.equals(LootCratesBlocks.RARE_SHULKER_LOOT_CRATE)) {
            return Rarity.RARE;
        } else if (block.equals(LootCratesBlocks.EPIC_CHEST_LOOT_CRATE) || block.equals(LootCratesBlocks.EPIC_SHULKER_LOOT_CRATE)) {
            return Rarity.EPIC;
        } else {
            return Rarity.COMMON;
        }
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
            LootCrateBlockEntity lootCrateBlockEntity = (LootCrateBlockEntity)blockEntity;

            // if creative: If there is block data add those and drop a block with all those tags
            // No tags = No drop. Just like vanilla shulker chests
            if (!world.isClient && player.isCreative()) {
                ItemStack itemStack = new ItemStack(this);

                boolean shouldDropItem = false;

                if (lootCrateBlockEntity.hasCustomName()) {
                    itemStack.setCustomName(lootCrateBlockEntity.getCustomName());
                    shouldDropItem = true;
                }

                CompoundTag compoundTag = lootCrateBlockEntity.addLootCrateBlockTags(new CompoundTag());
                if(!compoundTag.isEmpty()) {
                    itemStack.putSubTag("BlockEntityTag", compoundTag);
                    shouldDropItem = true;
                }

                if(shouldDropItem) {
                    ItemEntity itemEntity = new ItemEntity(world, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, itemStack);
                    itemEntity.setToDefaultPickupDelay();
                    world.spawnEntity(itemEntity);
                }
            }
        }

        super.onBreak(world, pos, state, player);
    }


    protected abstract BlockBreakAction getBlockBreakAction();

    /**
     * Add inventory data to dropped item stack
     * @param state
     * @param builder
     * @return
     */
    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, net.minecraft.loot.context.LootContext.Builder builder) {
        BlockEntity blockEntity = builder.getNullable(LootContextParameters.BLOCK_ENTITY);
        if (blockEntity instanceof LootCrateBlockEntity) { // TODO: only shulker chests should drop as item
            LootCrateBlockEntity lootCrateBlockEntity = (LootCrateBlockEntity)blockEntity;
            builder = builder.putDrop(CONTENTS, (lootContext, consumer) -> {
                for(int i = 0; i < lootCrateBlockEntity.size(); ++i) {
                    consumer.accept(lootCrateBlockEntity.getStack(i));
                }

            });
        }
        return super.getDroppedStacks(state, builder);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        ItemStack itemStack = super.getPickStack(world, pos, state);
        LootCrateBlockEntity lootCrateBlockEntity = (LootCrateBlockEntity)world.getBlockEntity(pos);

        if(lootCrateBlockEntity != null) {
            if (lootCrateBlockEntity.hasCustomName()) {
                itemStack.setCustomName(lootCrateBlockEntity.getCustomName());
            }

            CompoundTag compoundTag = lootCrateBlockEntity.addLootCrateBlockTags(new CompoundTag());
            if (!compoundTag.isEmpty()) {
                itemStack.putSubTag("BlockEntityTag", compoundTag);
            }
        }
        return itemStack;
    }

    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof Inventory) {
                ItemScatterer.spawn(world, pos, (Inventory)blockEntity);
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    protected void playSound(World world, BlockPos blockPos, SoundEvent soundEvent) {
        double d = blockPos.getX() + 0.5D;
        double e = blockPos.getY() + 0.5D;
        double f = blockPos.getZ() + 0.5D;
        world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
    }


}
