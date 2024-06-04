package de.dafuqs.lootcrates.blocks;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import de.dafuqs.lootcrates.*;
import de.dafuqs.lootcrates.blocks.barrel.*;
import de.dafuqs.lootcrates.enums.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.loot.context.*;
import net.minecraft.screen.*;
import net.minecraft.server.world.*;
import net.minecraft.sound.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

import java.util.*;

public abstract class LootCrateBlock extends BlockWithEntity {
	
	protected final LootCrateVariant variant;
	
	protected LootCrateBlock(LootCrateVariant variant, Settings settings) {
		super(settings);
		this.variant = variant;
	}
	
	public LootCrateVariant getVariant() {
		return variant;
	}
	
	public static final MapCodec<LootBarrelBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
			LootCrateVariant.CODEC.fieldOf("variant").forGetter((block) -> block.variant),
			createSettingsCodec()
	).apply(instance, LootBarrelBlock::new));
	
	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}
	
	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof LootCrateBlockEntity lootCrateBlockEntity) {
			lootCrateBlockEntity.relockIfNecessary(player);
			if (!lootCrateBlockEntity.isUnlocked(player)) {
				for (ItemStack itemStack : player.getHandItems()) {
					if (lootCrateBlockEntity.doesItemUnlock(itemStack.getItem())) {
						if (!player.isCreative()) {
							if (lootCrateBlockEntity.getLockType().consumesKey()) {
								itemStack.decrement(1);
							}
						}
						lootCrateBlockEntity.unlock(world, player);
						return ActionResult.CONSUME; // just consume the action and play unlock sound.
					}
				}
				if (!world.isClient()) {
					LootCrateVariant variant = getCrateVariant(world, pos);
					Text translatableText = LootCrateAtlas.getKeyNeededTooltip(variant);
					player.sendMessage(translatableText, false);
				}
				playSound(world, pos, SoundEvents.BLOCK_CHEST_LOCKED);
				return ActionResult.FAIL;
			}
		}
		return ActionResult.PASS;
	}
	
	protected static LootCrateVariant getCrateVariant(@NotNull World world, BlockPos blockPos) {
		Block block = world.getBlockState(blockPos).getBlock();
		return getCrateVariant(block);
	}
	
	protected static LootCrateVariant getCrateVariant(Block block) {
		return LootCrateAtlas.getCrateVariant(block);
	}
	
	@Override
	public boolean emitsRedstonePower(BlockState state) {
		return true;
	}
	
	@Override
	public int getWeakRedstonePower(BlockState state, @NotNull BlockView world, BlockPos pos, Direction direction) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof LootCrateBlockEntity lootCrateBlockEntity) {
			if (lootCrateBlockEntity.isTrapped()) {
				return MathHelper.clamp(getPlayersLookingInCrateCount(world, pos), 0, 15);
			}
		}
		return 0;
	}
	
	@Override
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

    /*@Override
    public void onPlaced(@NotNull World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LootCrateBlockEntity lootCrateBlockEntity) {
            if (itemStack.hasCustomName()) {
                ((LootCrateBlockEntity) blockEntity).setCustomName(itemStack.getName());
            }
        }
    }*/
	
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
					if (world.getBlockState(targetPos).isAir() && world.getBlockState(targetPos.down()).isSolid()) {
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
			
			if (scheduledTickEvent != ScheduledTickEvent.NONE) {
				world.scheduleBlockTick(pos, this, getRandomTickTime(world.random));
			}
		}
	}
	
	// faster than fire (30+ 0-10)
	private static int getRandomTickTime(@NotNull Random random) {
		return 20 + random.nextInt(10);
	}
	
	@Override
	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof LootCrateBlockEntity lootCrateBlockEntity) {
			if (!world.isClient && player.isCreative() && !lootCrateBlockEntity.isEmpty()) {
				ItemStack itemStack = new ItemStack(this);
				itemStack.applyComponentsFrom(blockEntity.createComponentMap());
				ItemEntity itemEntity = new ItemEntity(world, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, itemStack);
				itemEntity.setToDefaultPickupDelay();
				world.spawnEntity(itemEntity);
			} else {
				lootCrateBlockEntity.generateLoot(player);
			}
		}
		
		return super.onBreak(world, pos, state, player);
	}
	
	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		BlockBreakAction blockBreakAction = getBlockBreakAction();
		switch (blockBreakAction) {
			case KEEP_INVENTORY -> {
				if (!state.isOf(newState.getBlock())) {
					BlockEntity blockEntity = world.getBlockEntity(pos);
					if (blockEntity instanceof LootCrateBlockEntity) {
						world.updateComparators(pos, state.getBlock());
					}
					
					super.onStateReplaced(state, world, pos, newState, moved);
				}
			}
			case DROP_AND_SCATTER_INVENTORY, DESTROY_AND_SCATTER_INVENTORY -> {
				ItemScatterer.onStateReplaced(state, newState, world, pos);
			}
		}
	}
	
	public static final Identifier CONTENTS_DYNAMIC_DROP_ID = new Identifier("contents");
	
	@Override
	protected List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
		
		BlockBreakAction blockBreakAction = getBlockBreakAction();
		switch (blockBreakAction) {
			case KEEP_INVENTORY, DROP_AND_SCATTER_INVENTORY -> {
				BlockEntity blockEntity = builder.getOptional(LootContextParameters.BLOCK_ENTITY);
				if (blockEntity instanceof LootCrateBlockEntity lootCrateBlockEntity) {
					builder = builder.addDynamicDrop(CONTENTS_DYNAMIC_DROP_ID, (lootConsumer) -> {
						for (int i = 0; i < lootCrateBlockEntity.size(); ++i) {
							lootConsumer.accept(lootCrateBlockEntity.getStack(i));
						}
					});
				}
			}
			case DESTROY_AND_SCATTER_INVENTORY -> {
			
			}
		}
		
		return super.getDroppedStacks(state, builder);
	}
	
	@Override
	public boolean hasComparatorOutput(BlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorOutput(BlockState state, @NotNull World world, BlockPos pos) {
		return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
	}
	
	protected abstract BlockBreakAction getBlockBreakAction();
	
	protected void playSound(@NotNull World world, @NotNull BlockPos blockPos, SoundEvent soundEvent) {
		double d = blockPos.getX() + 0.5D;
		double e = blockPos.getY() + 0.5D;
		double f = blockPos.getZ() + 0.5D;
		world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
	}
	
}
