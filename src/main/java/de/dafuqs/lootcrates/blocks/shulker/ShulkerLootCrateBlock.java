package de.dafuqs.lootcrates.blocks.shulker;

import de.dafuqs.lootcrates.blocks.LootCrateBlock;
import de.dafuqs.lootcrates.blocks.LootCratesBlockEntityType;
import de.dafuqs.lootcrates.blocks.chest.ChestLootCrateBlockEntity;
import de.dafuqs.lootcrates.enums.BlockBreakAction;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShulkerLootCrateBlock extends LootCrateBlock {

    public static final DirectionProperty FACING;
    public static final Identifier CONTENTS;
    public static final BooleanProperty LOCKED;

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
        return BlockBreakAction.KEEP_INVENTORY;
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
            Box box = ShulkerEntity.method_33347(state.get(FACING), 0.0F, 0.5F).offset(pos).contract(1.0E-6D);
            return world.isSpaceEmpty(box);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.DESTROY;
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
        LOCKED = Properties.LOCKED;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ShulkerLootCrateBlockEntity(pos, state);
    }
}