package de.dafuqs.lootcrates.blocks.shulker;

import de.dafuqs.lootcrates.blocks.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.block.piston.*;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.screen.*;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.stream.*;

public class ShulkerLootCrateBlockEntity extends LootCrateBlockEntity implements SidedInventory {

    private int viewerCount;
    private ShulkerBoxBlockEntity.AnimationStage animationStage;
    private float animationProgress;
    private float prevAnimationProgress;
    private static final int[] AVAILABLE_SLOTS = IntStream.range(0, 27).toArray();

    public ShulkerLootCrateBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        this.animationStage = ShulkerBoxBlockEntity.AnimationStage.CLOSED;
    }

    public ShulkerLootCrateBlockEntity(BlockPos pos, BlockState state) {
        this(LootCratesBlockEntityType.SHULKER_LOOT_CRATE_BLOCK_ENTITY, pos, state);
    }

    protected void onInvOpenOrClose(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
        super.onInvOpenOrClose(world, pos, state, oldViewerCount, newViewerCount);
    }

    @Override
    public int getCurrentLookingPlayers(BlockView world, BlockPos pos) {
        return this.viewerCount;
    }



    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new ShulkerBoxScreenHandler(syncId, playerInventory, this);
    }

    public boolean suffocates() {
        return this.animationStage == ShulkerBoxBlockEntity.AnimationStage.CLOSED;
    }

    public static void tick(World world, BlockPos pos, BlockState state, ShulkerLootCrateBlockEntity blockEntity) {
        blockEntity.updateAnimation(world, pos, state);
    }

    private void updateAnimation(World world, BlockPos pos, BlockState state) {
        this.prevAnimationProgress = this.animationProgress;
        switch (this.animationStage) {
            case CLOSED -> this.animationProgress = 0.0F;
            case OPENING -> {
                this.animationProgress += 0.1F;
                if (this.animationProgress >= 1.0F) {
                    this.animationStage = ShulkerBoxBlockEntity.AnimationStage.OPENED;
                    this.animationProgress = 1.0F;
                    updateNeighborStates(world, pos, state);
                }
                this.pushEntities(world, pos, state);
            }
            case CLOSING -> {
                this.animationProgress -= 0.1F;
                if (this.animationProgress <= 0.0F) {
                    this.animationStage = ShulkerBoxBlockEntity.AnimationStage.CLOSED;
                    this.animationProgress = 0.0F;
                    updateNeighborStates(world, pos, state);
                }
            }
            case OPENED -> this.animationProgress = 1.0F;
        }
    }

    private void pushEntities(World world, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof ShulkerLootCrateBlock) {
            Direction direction = state.get(ShulkerBoxBlock.FACING);
            Box box = ShulkerEntity.calculateBoundingBox(direction, this.prevAnimationProgress, this.animationProgress).offset(pos);
            List<Entity> list = world.getOtherEntities(null, box);
            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    if (entity.getPistonBehavior() != PistonBehavior.IGNORE) {
                        entity.move(MovementType.SHULKER_BOX, new Vec3d((box.getLengthX() + 0.01D) * (double) direction.getOffsetX(), (box.getLengthY() + 0.01D) * (double) direction.getOffsetY(), (box.getLengthZ() + 0.01D) * (double) direction.getOffsetZ()));
                    }
                }
            }
        }
    }

    private static void updateNeighborStates(World world, BlockPos pos, BlockState state) {
        state.updateNeighbors(world, pos, 3);
    }

    public ShulkerBoxBlockEntity.AnimationStage getAnimationStage() {
        return this.animationStage;
    }

    public Box getBoundingBox(BlockState state) {
        return this.getBoundingBox(state.get(ShulkerBoxBlock.FACING));
    }

    public Box getBoundingBox(Direction openDirection) {
        float f = this.getAnimationProgress(1.0F);
        return VoxelShapes.fullCube().getBoundingBox().stretch((0.5F * f * (float)openDirection.getOffsetX()), (0.5F * f * (float)openDirection.getOffsetY()), (0.5F * f * (float)openDirection.getOffsetZ()));
    }

    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        if (type == 1) {
            this.viewerCount = data;
            if (data == 0) {
                this.animationStage = ShulkerBoxBlockEntity.AnimationStage.CLOSING;
                this.updateNeighborStates();
            } else if (data == 1) {
                this.animationStage = ShulkerBoxBlockEntity.AnimationStage.OPENING;
                this.updateNeighborStates();
            }
            return true;
        } else {
            return super.onSyncedBlockEvent(type, data);
        }
    }

    private void updateNeighborStates() {
        this.getCachedState().updateNeighbors(this.getWorld(), this.getPos(), 3);
    }

    @Override
    public void onOpen(PlayerEntity player) {
        if (!player.isSpectator() && hasWorld()) {

            if (this.viewerCount < 0) {
                this.viewerCount = 0;
            }
            ++this.viewerCount;
            onInvOpenOrClose(world, pos, world.getBlockState(pos), this.viewerCount-1, this.viewerCount);

            this.world.addSyncedBlockEvent(this.pos, this.getCachedState().getBlock(), 1, this.viewerCount);
            if (this.viewerCount == 1) {
                playOpenSoundEffect();
            }
        }
    }

    @Override
    public void onClose(PlayerEntity player) {
        if (!player.isSpectator() && hasWorld()) {
            --this.viewerCount;
            onInvOpenOrClose(world, pos, world.getBlockState(pos), this.viewerCount+1, this.viewerCount);

            this.world.addSyncedBlockEvent(this.pos, this.getCachedState().getBlock(), 1, this.viewerCount);
            if (this.viewerCount <= 0) {
                playCloseSoundEffect();
            }
        }
    }

    public float getAnimationProgress(float f) {
        return MathHelper.lerp(f, this.prevAnimationProgress, this.animationProgress);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return AVAILABLE_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return !(Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

}
