package de.dafuqs.lootcrates.blocks.shulker;

import de.dafuqs.lootcrates.*;
import de.dafuqs.lootcrates.blocks.*;
import de.dafuqs.lootcrates.enums.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.state.*;
import net.minecraft.state.property.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class ShulkerLootCrateBlock extends LootCrateBlock {
	
	public static final EnumProperty<Direction> FACING = FacingBlock.FACING;
	
	public ShulkerLootCrateBlock(LootCrateVariant variant, AbstractBlock.Settings settings) {
		super(variant, settings);
		this.setDefaultState((this.stateManager.getDefaultState()).with(FACING, Direction.UP));
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getSide());
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? validateTicker(type, LootCratesBlockEntityType.SHULKER_LOOT_CRATE_BLOCK_ENTITY, ShulkerLootCrateBlockEntity::tick) : null;
	}
	
	@Override
	protected BlockBreakAction getBlockBreakAction() {
		if (LootCrates.CONFIG.ShulkerCratesKeepTheirInventory) {
			return BlockBreakAction.KEEP_INVENTORY;
		} else {
			if (LootCrates.CONFIG.ShulkerCratesDropAsItems) {
				return BlockBreakAction.DROP_AND_SCATTER_INVENTORY;
			} else {
				return BlockBreakAction.DESTROY_AND_SCATTER_INVENTORY;
			}
		}
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		Optional<ShulkerLootCrateBlockEntity> e = world.getBlockEntity(pos, LootCratesBlockEntityType.SHULKER_LOOT_CRATE_BLOCK_ENTITY);
		if (e.isEmpty()) {
			return ActionResult.FAIL;
		}
		if (!canOpen(state, world, pos, e.get())) {
			return ActionResult.FAIL;
		}
		return super.onUse(state, world, pos, player, hit);
	}
	
	private static boolean canOpen(BlockState state, World world, BlockPos pos, ShulkerLootCrateBlockEntity entity) {
		if (entity.getAnimationStage() != ShulkerBoxBlockEntity.AnimationStage.CLOSED) {
			return true;
		} else {
			Box box = ShulkerEntity.calculateBoundingBox(1.0F, state.get(FACING), 0.0F, 0.5F, pos.toBottomCenterPos()).contract(1.0E-6);
			return world.isSpaceEmpty(box);
		}
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		return blockEntity instanceof ShulkerLootCrateBlockEntity ? VoxelShapes.cuboid(((ShulkerLootCrateBlockEntity) blockEntity).getBoundingBox(state)) : VoxelShapes.fullCube();
	}
	
	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}
	
	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new ShulkerLootCrateBlockEntity(pos, state);
	}
}