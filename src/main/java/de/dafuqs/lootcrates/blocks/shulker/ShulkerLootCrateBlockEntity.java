package de.dafuqs.lootcrates.blocks.shulker;

import de.dafuqs.lootcrates.LootCrateAtlas;
import de.dafuqs.lootcrates.blocks.LootCrateBlockEntity;
import de.dafuqs.lootcrates.blocks.LootCratesBlockEntityType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;

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

    @Override
    protected Text getContainerName() {
        return new TranslatableText("container.lootcrates.shulker_crate");
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
        switch(this.animationStage) {
            case CLOSED:
                this.animationProgress = 0.0F;
                break;
            case OPENING:
                this.animationProgress += 0.1F;
                if (this.animationProgress >= 1.0F) {
                    this.animationStage = ShulkerBoxBlockEntity.AnimationStage.OPENED;
                    this.animationProgress = 1.0F;
                    updateNeighborStates(world, pos, state);
                }

                this.pushEntities(world, pos, state);
                break;
            case CLOSING:
                this.animationProgress -= 0.1F;
                if (this.animationProgress <= 0.0F) {
                    this.animationStage = ShulkerBoxBlockEntity.AnimationStage.CLOSED;
                    this.animationProgress = 0.0F;
                    updateNeighborStates(world, pos, state);
                }
                break;
            case OPENED:
                this.animationProgress = 1.0F;
        }
    }

    private void pushEntities(World world, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof ShulkerBoxBlock) {
            Direction direction = state.get(ShulkerBoxBlock.FACING);
            Box box = ShulkerEntity.method_33347(direction, this.prevAnimationProgress, this.animationProgress).offset(pos);
            List<Entity> list = world.getOtherEntities(null, box);
            if (!list.isEmpty()) {
                for(int i = 0; i < list.size(); ++i) {
                    Entity entity = list.get(i);
                    if (entity.getPistonBehavior() != PistonBehavior.IGNORE) {
                        entity.move(MovementType.SHULKER_BOX, new Vec3d((box.getXLength() + 0.01D) * (double)direction.getOffsetX(), (box.getYLength() + 0.01D) * (double)direction.getOffsetY(), (box.getZLength() + 0.01D) * (double)direction.getOffsetZ()));
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


    private Box getCollisionBox(Direction facing) {
        Direction direction = facing.getOpposite();
        return this.getBoundingBox(facing).shrink(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
    }

    private void pushEntities() {
        BlockState blockState = this.world.getBlockState(this.getPos());
        if (blockState.getBlock() instanceof ShulkerLootCrateBlock) {
            Direction direction = blockState.get(ShulkerBoxBlock.FACING);
            Box box = this.getCollisionBox(direction).offset(this.pos);
            List<Entity> list = this.world.getOtherEntities(null, box);
            for (Entity entity : list) {
                if (entity.getPistonBehavior() != PistonBehavior.IGNORE) {
                    double d = 0.0D;
                    double e = 0.0D;
                    double f = 0.0D;
                    Box box2 = entity.getBoundingBox();
                    switch (direction.getAxis()) {
                        case X:
                            if (direction.getDirection() == Direction.AxisDirection.POSITIVE) {
                                d = box.maxX - box2.minX;
                            } else {
                                d = box2.maxX - box.minX;
                            }

                            d += 0.01D;
                            break;
                        case Y:
                            if (direction.getDirection() == Direction.AxisDirection.POSITIVE) {
                                e = box.maxY - box2.minY;
                            } else {
                                e = box2.maxY - box.minY;
                            }

                            e += 0.01D;
                            break;
                        case Z:
                            if (direction.getDirection() == Direction.AxisDirection.POSITIVE) {
                                f = box.maxZ - box2.minZ;
                            } else {
                                f = box2.maxZ - box.minZ;
                            }

                            f += 0.01D;
                    }
                    entity.move(MovementType.SHULKER_BOX, new Vec3d(d * (double) direction.getOffsetX(), e * (double) direction.getOffsetY(), f * (double) direction.getOffsetZ()));
                }

            }
        }
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
            this.world.addSyncedBlockEvent(this.pos, this.getCachedState().getBlock(), 1, this.viewerCount);
            if (this.viewerCount <= 0) {
                playCloseSoundEffect();
            }
        }
    }

    public float getAnimationProgress(float f) {
        return MathHelper.lerp(f, this.prevAnimationProgress, this.animationProgress);
    }

    public SpriteIdentifier getTexture() {
        return LootCrateAtlas.getShulkerTexture(this);
    }

    public boolean hasTransparency() {
        return LootCrateAtlas.hasTransparency(this);
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
