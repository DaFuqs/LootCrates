package de.dafuqs.lootcrates.blocks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
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
                    player.sendMessage(new TranslatableText("message.lootcrates.common_key_needed_to_unlock"), false); // TODO: localize
                    player.sendMessage(new TranslatableText("message.lootcrates.uncommon_key_needed_to_unlock"), false); // TODO: localize
                    player.sendMessage(new TranslatableText("message.lootcrates.rare_key_needed_to_unlock"), false); // TODO: localize
                    player.sendMessage(new TranslatableText("message.lootcrates.epic_key_needed_to_unlock"), false); // TODO: localize
                }
                return ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;
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
            if(itemStack.hasTag()) {
                CompoundTag tag = itemStack.getSubTag("BlockEntityTag");
                if(tag != null) {
                    ((LootCrateBlockEntity) blockEntity).setLootCrateBlockTags(tag);
                }
            }
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LootCrateBlockEntity) {
            LootCrateBlockEntity lootCrateBlockEntity = (LootCrateBlockEntity)blockEntity;
            if (!world.isClient && player.isCreative() && !lootCrateBlockEntity.isEmpty()) {
                ItemStack itemStack = new ItemStack(this);

                CompoundTag compoundTag = lootCrateBlockEntity.addLootCrateBlockTags(new CompoundTag());
                itemStack.putSubTag("BlockEntityTag", compoundTag);

                ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, itemStack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            } else {
                lootCrateBlockEntity.checkLootInteraction(player);
            }
        }

        super.onBreak(world, pos, state, player);
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, net.minecraft.loot.context.LootContext.Builder builder) {
        BlockEntity blockEntity = builder.getNullable(LootContextParameters.BLOCK_ENTITY);
        if (blockEntity instanceof LootCrateBlockEntity) {
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

        CompoundTag compoundTag = lootCrateBlockEntity.addLootCrateBlockTags(new CompoundTag());
        if (!compoundTag.isEmpty()) {
            itemStack.putSubTag("BlockEntityTag", compoundTag);
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
