package de.dafuqs.lootcrates.blocks.shulker;

import de.dafuqs.lootcrates.blocks.LootCrateBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.ShulkerLidCollisions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class ShulkerLootCrateBlock extends LootCrateBlock {

    public static final DirectionProperty FACING;
    public static final Identifier CONTENTS;
    public static final BooleanProperty LOCKED;

    public ShulkerLootCrateBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((this.stateManager.getDefaultState()).with(FACING, Direction.UP));
    }

    public BlockEntity createBlockEntity(BlockView world) {
        return new ShulkerLootCrateBlockEntity();
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getSide());
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else if (player.isSpectator()) {
            return ActionResult.CONSUME;
        } else {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ShulkerLootCrateBlockEntity) {
                ShulkerLootCrateBlockEntity ShulkerLootCrateBlockEntity = (ShulkerLootCrateBlockEntity)blockEntity;
                boolean bl2;
                if (ShulkerLootCrateBlockEntity.getAnimationStage() == ShulkerBoxBlockEntity.AnimationStage.CLOSED) {
                    Direction direction = state.get(FACING);
                    bl2 = world.isSpaceEmpty(ShulkerLidCollisions.getLidCollisionBox(pos, direction));
                } else {
                    bl2 = true;
                }

                if (bl2) {
                    player.openHandledScreen(ShulkerLootCrateBlockEntity);
                    player.incrementStat(Stats.OPEN_SHULKER_BOX);
                    PiglinBrain.onGuardedBlockInteracted(player, true);
                }

                return ActionResult.CONSUME;
            } else {
                return ActionResult.PASS;
            }
        }
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.DESTROY;
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof ShulkerLootCrateBlockEntity ? VoxelShapes.cuboid(((ShulkerLootCrateBlockEntity)blockEntity).getBoundingBox(state)) : VoxelShapes.fullCube();
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    static {
        FACING = FacingBlock.FACING;
        CONTENTS = new Identifier("contents");
        LOCKED = Properties.LOCKED;
    }

}