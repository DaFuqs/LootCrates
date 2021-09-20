package de.dafuqs.lootcrates.blocks.chest;

import de.dafuqs.lootcrates.LootCrates;
import de.dafuqs.lootcrates.blocks.LootCrateBlock;
import de.dafuqs.lootcrates.blocks.LootCrateBlockEntity;
import de.dafuqs.lootcrates.blocks.LootCratesBlockEntityType;
import de.dafuqs.lootcrates.enums.BlockBreakAction;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class ChestLootCrateBlock extends LootCrateBlock {

    public static final DirectionProperty FACING;
    protected static final VoxelShape SHAPE;

    public ChestLootCrateBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            ActionResult actionResult = super.onUse(state, world, pos, player, hand, hit);
            if(actionResult == ActionResult.PASS) {
                if (!hasBlockOnTop(world, pos)) {
                    NamedScreenHandlerFactory namedScreenHandlerFactory = this.createScreenHandlerFactory(state, world, pos);
                    if (namedScreenHandlerFactory != null) {
                        player.openHandledScreen(namedScreenHandlerFactory);
                        PiglinBrain.onGuardedBlockInteracted(player, true);
                    }
                }
            }
            return ActionResult.CONSUME;
        }
    }

    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof LootCrateBlockEntity) {
                world.updateComparators(pos, state.getBlock());
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    protected BlockBreakAction getBlockBreakAction() {
        if(LootCrates.CONFIG.ChestCratesKeepTheirInventory) {
            return BlockBreakAction.KEEP_INVENTORY;
        } else {
            return BlockBreakAction.DROP_AND_SCATTER_INVENTORY;
        }
    }

    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        if(LootCrates.CONFIG.ChestCrateHardness < 0) {
            return PistonBehavior.BLOCK;
        } else {
            return PistonBehavior.DESTROY;
        }
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? checkType(type, LootCratesBlockEntityType.CHEST_LOOT_CRATE_BLOCK_ENTITY, ChestLootCrateBlockEntity::clientTick) : null;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.scheduledTick(state, world, pos, random);

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ChestBlockEntity) {
            ((ChestBlockEntity)blockEntity).onScheduledTick();
        }
    }

    public static boolean hasBlockOnTop(BlockView world, BlockPos pos) {
        BlockPos blockPos = pos.up();
        return world.getBlockState(blockPos).isSolidBlock(world, blockPos);
    }

    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ChestLootCrateBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getPlayerFacing().getOpposite();
        return this.getDefaultState().with(FACING, direction);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    static {
        FACING = HorizontalFacingBlock.FACING;
        SHAPE = Block.createCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
    }

}