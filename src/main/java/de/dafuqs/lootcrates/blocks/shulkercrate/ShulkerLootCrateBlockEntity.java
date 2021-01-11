package de.dafuqs.lootcrates.blocks.shulkercrate;

import de.dafuqs.lootcrates.blocks.LootCratesBlockEntityType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.data.server.LootTablesProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;

public class ShulkerLootCrateBlockEntity extends LootableContainerBlockEntity implements Tickable {

    private static final int[] AVAILABLE_SLOTS = IntStream.range(0, 27).toArray();
    private DefaultedList<ItemStack> inventory;
    private int viewerCount;
    private net.minecraft.block.entity.ShulkerBoxBlockEntity.AnimationStage animationStage;
    private float animationProgress;
    private float prevAnimationProgress;

    private Identifier lootTable;

    public ShulkerLootCrateBlockEntity() {
        super(LootCratesBlockEntityType.SHULKER_LOOT_CRATE_BLOCK_ENTITY);
        this.inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
        this.animationStage = net.minecraft.block.entity.ShulkerBoxBlockEntity.AnimationStage.CLOSED;

        this.lootTable = LootTables.RUINED_PORTAL_CHEST; // TODO
    }

    public void tick() {
        this.updateAnimation();
        if (this.animationStage == net.minecraft.block.entity.ShulkerBoxBlockEntity.AnimationStage.OPENING || this.animationStage == net.minecraft.block.entity.ShulkerBoxBlockEntity.AnimationStage.CLOSING) {
            this.pushEntities();
        }
    }

    protected void updateAnimation() {
        this.prevAnimationProgress = this.animationProgress;
        switch(this.animationStage) {
            case CLOSED:
                this.animationProgress = 0.0F;
                break;
            case OPENING:
                this.animationProgress += 0.1F;
                if (this.animationProgress >= 1.0F) {
                    this.pushEntities();
                    this.animationStage = net.minecraft.block.entity.ShulkerBoxBlockEntity.AnimationStage.OPENED;
                    this.animationProgress = 1.0F;
                    this.updateNeighborStates();
                }
                break;
            case CLOSING:
                this.animationProgress -= 0.1F;
                if (this.animationProgress <= 0.0F) {
                    this.animationStage = net.minecraft.block.entity.ShulkerBoxBlockEntity.AnimationStage.CLOSED;
                    this.animationProgress = 0.0F;
                    this.updateNeighborStates();
                }
                break;
            case OPENED:
                this.animationProgress = 1.0F;
        }

    }

    public net.minecraft.block.entity.ShulkerBoxBlockEntity.AnimationStage getAnimationStage() {
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
        if (blockState.getBlock() instanceof ShulkerBoxBlock) {
            Direction direction = blockState.get(ShulkerBoxBlock.FACING);
            Box box = this.getCollisionBox(direction).offset(this.pos);
            List<Entity> list = this.world.getOtherEntities(null, box);
            if (!list.isEmpty()) {
                for(int i = 0; i < list.size(); ++i) {
                    Entity entity = list.get(i);
                    if (entity.getPistonBehavior() != PistonBehavior.IGNORE) {
                        double d = 0.0D;
                        double e = 0.0D;
                        double f = 0.0D;
                        Box box2 = entity.getBoundingBox();
                        switch(direction.getAxis()) {
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

                        entity.move(MovementType.SHULKER_BOX, new Vec3d(d * (double)direction.getOffsetX(), e * (double)direction.getOffsetY(), f * (double)direction.getOffsetZ()));
                    }
                }

            }
        }
    }

    public int size() {
        return this.inventory.size();
    }

    public boolean onSyncedBlockEvent(int type, int data) {
        if (type == 1) {
            this.viewerCount = data;
            if (data == 0) {
                this.animationStage = net.minecraft.block.entity.ShulkerBoxBlockEntity.AnimationStage.CLOSING;
                this.updateNeighborStates();
            }

            if (data == 1) {
                this.animationStage = net.minecraft.block.entity.ShulkerBoxBlockEntity.AnimationStage.OPENING;
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

    public void onOpen(PlayerEntity player) {

        if (!player.isSpectator()) {
            if (this.viewerCount < 0) {
                this.viewerCount = 0;
            }

            ++this.viewerCount;
            this.world.addSyncedBlockEvent(this.pos, this.getCachedState().getBlock(), 1, this.viewerCount);
            if (this.viewerCount == 1) {
                this.world.playSound(null, this.pos, SoundEvents.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
            }
        }

    }

    public void onClose(PlayerEntity player) {
        if (!player.isSpectator()) {
            --this.viewerCount;
            this.world.addSyncedBlockEvent(this.pos, this.getCachedState().getBlock(), 1, this.viewerCount);
            if (this.viewerCount <= 0) {
                this.world.playSound(null, this.pos, SoundEvents.BLOCK_SHULKER_BOX_CLOSE, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
            }
        }

    }

    protected Text getContainerName() {
        return new TranslatableText("container.lootcrates.shulker_crate");
    }

    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.deserializeInventory(tag);
    }

    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        return this.serializeInventory(tag);
    }

    public void deserializeInventory(CompoundTag tag) {
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(tag) && tag.contains("Items", 9)) {
            Inventories.fromTag(tag, this.inventory);
        }

    }

    public CompoundTag serializeInventory(CompoundTag tag) {
        if (!this.serializeLootTable(tag)) {
            Inventories.toTag(tag, this.inventory, false);
        }

        return tag;
    }

    protected DefaultedList<ItemStack> getInvStackList() {
        return this.inventory;
    }

    protected void setInvStackList(DefaultedList<ItemStack> list) {
        this.inventory = list;
    }

    public int[] getAvailableSlots(Direction side) {
        return AVAILABLE_SLOTS;
    }

    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return !(Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock);
    }

    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    public float getAnimationProgress(float f) {
        return MathHelper.lerp(f, this.prevAnimationProgress, this.animationProgress);
    }

    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new ShulkerBoxScreenHandler(syncId, playerInventory, this);
    }

    public boolean suffocates() {
        return this.animationStage == net.minecraft.block.entity.ShulkerBoxBlockEntity.AnimationStage.CLOSED;
    }

    public static enum AnimationStage {
        CLOSED,
        OPENING,
        OPENED,
        CLOSING;

        private AnimationStage() {
        }
    }

}
