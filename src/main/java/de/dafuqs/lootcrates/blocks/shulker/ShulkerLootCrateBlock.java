package de.dafuqs.lootcrates.blocks.shulker;

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

public class ShulkerLootCrateBlock extends BlockWithEntity {

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

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getSide());
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ShulkerLootCrateBlockEntity) {
            ShulkerLootCrateBlockEntity shulkerBoxBlockEntity = (ShulkerLootCrateBlockEntity)blockEntity;
            if (!world.isClient && player.isCreative() && !shulkerBoxBlockEntity.isEmpty()) {
                ItemStack itemStack = new ItemStack(this);
                CompoundTag compoundTag = shulkerBoxBlockEntity.serializeInventory(new CompoundTag());
                if (!compoundTag.isEmpty()) {
                    itemStack.putSubTag("BlockEntityTag", compoundTag);
                }

                if (shulkerBoxBlockEntity.hasCustomName()) {
                    itemStack.setCustomName(shulkerBoxBlockEntity.getCustomName());
                }

                ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, itemStack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            } else {
                shulkerBoxBlockEntity.checkLootInteraction(player);
            }
        }

        super.onBreak(world, pos, state, player);
    }

    public List<ItemStack> getDroppedStacks(BlockState state, net.minecraft.loot.context.LootContext.Builder builder) {
        BlockEntity blockEntity = builder.getNullable(LootContextParameters.BLOCK_ENTITY);
        if (blockEntity instanceof ShulkerLootCrateBlockEntity) {
            ShulkerLootCrateBlockEntity shulkerBoxBlockEntity = (ShulkerLootCrateBlockEntity)blockEntity;
            builder = builder.putDrop(CONTENTS, (lootContext, consumer) -> {
                for(int i = 0; i < shulkerBoxBlockEntity.size(); ++i) {
                    consumer.accept(shulkerBoxBlockEntity.getStack(i));
                }

            });
        }

        return super.getDroppedStacks(state, builder);
    }

    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (itemStack.hasCustomName()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ShulkerLootCrateBlockEntity) {
                ((ShulkerLootCrateBlockEntity)blockEntity).setCustomName(itemStack.getName());
            }
        }

    }

    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        super.appendTooltip(stack, world, tooltip, options);
        CompoundTag compoundTag = stack.getSubTag("BlockEntityTag");
        if (compoundTag != null) {
            if (compoundTag.contains("LootTable", 8)) {
                tooltip.add(new LiteralText("???????"));
            }

            if (compoundTag.contains("Items", 9)) {
                DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(27, ItemStack.EMPTY);
                Inventories.fromTag(compoundTag, defaultedList);
                int i = 0;
                int j = 0;
                Iterator var9 = defaultedList.iterator();

                while(var9.hasNext()) {
                    ItemStack itemStack = (ItemStack)var9.next();
                    if (!itemStack.isEmpty()) {
                        ++j;
                        if (i <= 4) {
                            ++i;
                            MutableText mutableText = itemStack.getName().shallowCopy();
                            mutableText.append(" x").append(String.valueOf(itemStack.getCount()));
                            tooltip.add(mutableText);
                        }
                    }
                }

                if (j - i > 0) {
                    tooltip.add((new TranslatableText("container.shulkerBox.more", new Object[]{j - i})).formatted(Formatting.ITALIC));
                }
            }
        }

    }

    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.DESTROY;
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof ShulkerLootCrateBlockEntity ? VoxelShapes.cuboid(((ShulkerLootCrateBlockEntity)blockEntity).getBoundingBox(state)) : VoxelShapes.fullCube();
    }

    @Environment(EnvType.CLIENT)
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        ItemStack itemStack = super.getPickStack(world, pos, state);
        ShulkerLootCrateBlockEntity shulkerBoxBlockEntity = (ShulkerLootCrateBlockEntity)world.getBlockEntity(pos);
        CompoundTag compoundTag = shulkerBoxBlockEntity.serializeInventory(new CompoundTag());
        if (!compoundTag.isEmpty()) {
            itemStack.putSubTag("BlockEntityTag", compoundTag);
        }

        return itemStack;
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