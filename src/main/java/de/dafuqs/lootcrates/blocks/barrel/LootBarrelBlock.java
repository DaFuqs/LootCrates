package de.dafuqs.lootcrates.blocks.barrel;

import de.dafuqs.lootcrates.*;
import de.dafuqs.lootcrates.blocks.*;
import de.dafuqs.lootcrates.enums.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.screen.*;
import net.minecraft.server.world.*;
import net.minecraft.state.*;
import net.minecraft.state.property.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

public class LootBarrelBlock extends LootCrateBlock {

    public static final DirectionProperty FACING = Properties.FACING;
    public static final BooleanProperty OPEN = Properties.OPEN;

    public LootBarrelBlock(Settings settings) {
        super(settings);
        this.setDefaultState(((this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(OPEN, false));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            ActionResult actionResult = super.onUse(state, world, pos, player, hand, hit);
            if(actionResult == ActionResult.PASS) {
                NamedScreenHandlerFactory namedScreenHandlerFactory = this.createScreenHandlerFactory(state, world, pos);
                if (namedScreenHandlerFactory != null) {
                    player.openHandledScreen(namedScreenHandlerFactory);
                    PiglinBrain.onGuardedBlockInteracted(player, true);
                }
            }
            return ActionResult.CONSUME;
        }
    }
    
    @Override
    public void onStateReplaced(BlockState state, @NotNull World world, BlockPos pos, BlockState newState, boolean moved) {
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
        if(LootCrates.CONFIG.LootBarrelsKeepTheirInventory) {
            return BlockBreakAction.KEEP_INVENTORY;
        } else {
            if(LootCrates.CONFIG.LootBarrelsDropAsItems) {
                return BlockBreakAction.DROP_AND_SCATTER_INVENTORY;
            } else {
                return BlockBreakAction.DESTROY_AND_SCATTER_INVENTORY;
            }
        }
    }
    
    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LootBarrelBlockEntity) {
            ((LootBarrelBlockEntity)blockEntity).tick();
        }
    }

    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new LootBarrelBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
    }

}