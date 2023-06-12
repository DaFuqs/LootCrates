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

public class ShulkerLootCrateBlock extends LootCrateBlock {

    public static final DirectionProperty FACING;
    public static final Identifier CONTENTS;

    public ShulkerLootCrateBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((this.stateManager.getDefaultState()).with(FACING, Direction.UP));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getSide());
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? checkType(type, LootCratesBlockEntityType.SHULKER_LOOT_CRATE_BLOCK_ENTITY, ShulkerLootCrateBlockEntity::tick) : null;
    }

    @Override
    protected BlockBreakAction getBlockBreakAction() {
        if(LootCrates.CONFIG.ShulkerCratesKeepTheirInventory) {
            return BlockBreakAction.KEEP_INVENTORY;
        } else {
            if(LootCrates.CONFIG.ShulkerCratesDropAsItems) {
                return BlockBreakAction.DROP_AND_SCATTER_INVENTORY;
            } else {
                return BlockBreakAction.DESTROY_AND_SCATTER_INVENTORY;
            }
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else if (player.isSpectator()) {
            return ActionResult.CONSUME;
        } else {
            ActionResult actionResult = super.onUse(state, world, pos, player, hand, hit);
            if(actionResult == ActionResult.PASS) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof ShulkerLootCrateBlockEntity) {
                    ShulkerLootCrateBlockEntity shulkerLootCrateBlockEntity = (ShulkerLootCrateBlockEntity) blockEntity;
                    if (canOpen(state, world, pos, shulkerLootCrateBlockEntity)) {
                        player.openHandledScreen(shulkerLootCrateBlockEntity);
                        PiglinBrain.onGuardedBlockInteracted(player, true);
                    }

                    return ActionResult.CONSUME;
                } else {
                    return ActionResult.PASS;
                }
            } else {
                return ActionResult.CONSUME;
            }
        }
    }

    private static boolean canOpen(BlockState state, World world, BlockPos pos, ShulkerLootCrateBlockEntity entity) {
        if (entity.getAnimationStage() != ShulkerBoxBlockEntity.AnimationStage.CLOSED) {
            return true;
        } else {
            Box box = ShulkerEntity.calculateBoundingBox(state.get(FACING), 0.0F, 0.5F).offset(pos).contract(1.0E-6D);
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
        return blockEntity instanceof ShulkerLootCrateBlockEntity ? VoxelShapes.cuboid(((ShulkerLootCrateBlockEntity)blockEntity).getBoundingBox(state)) : VoxelShapes.fullCube();
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    static {
        FACING = FacingBlock.FACING;
        CONTENTS = new Identifier("contents");
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ShulkerLootCrateBlockEntity(pos, state);
    }
}