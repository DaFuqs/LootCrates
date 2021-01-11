//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package de.dafuqs.lootcrates.blocks.lootcrate;

import de.dafuqs.lootcrates.blocks.LootCratesBlockEntityType;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.block.ChestAnimationProgress;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;

public class LootCrateBlockEntity extends LootableContainerBlockEntity implements Tickable {

    private DefaultedList<ItemStack> inventory;
    private int viewerCount;
    protected float animationAngle;
    protected float lastAnimationAngle;

    private Identifier lootTable;
    private int replenishTimeTicks;
    private int lastReplenishTimeTicks;

    public LootCrateBlockEntity() {
        super(LootCratesBlockEntityType.LOOT_CRATE_BLOCK_ENTITY);
        this.inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);

        this.lootTable = LootTables.BASTION_TREASURE_CHEST; // TODO
        this.replenishTimeTicks = 600; // = 30 seconds
        this.lastReplenishTimeTicks = 0;
    }

    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        if (!this.serializeLootTable(tag)) {
            Inventories.toTag(tag, this.inventory);
        }

        return tag;
    }

    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(tag)) {
            Inventories.fromTag(tag, this.inventory);
        }

    }

    public int size() {
        return 27;
    }

    protected DefaultedList<ItemStack> getInvStackList() {
        return this.inventory;
    }

    protected void setInvStackList(DefaultedList<ItemStack> list) {
        this.inventory = list;
    }

    protected Text getContainerName() {
        return new TranslatableText("container.lootcrates.loot_crate");
    }

    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, this);
    }

    public void onOpen(PlayerEntity player) {
        if (!player.isSpectator()) {
            if (this.viewerCount < 0) {
                this.viewerCount = 0;
            }

            ++this.viewerCount;
            this.onInvOpenOrClose();
        }
    }

    protected void onInvOpenOrClose() {
        Block block = this.getCachedState().getBlock();
        if (block instanceof LootCrateBlock) {
            this.world.addSyncedBlockEvent(this.pos, block, 1, this.viewerCount);
        }
    }

    @Override
    public void tick() {
        this.lastAnimationAngle = this.animationAngle;
        if (this.viewerCount > 0 && this.animationAngle == 0.0F) {
            this.playSound(SoundEvents.BLOCK_CHEST_OPEN);
        }

        if (this.viewerCount == 0 && this.animationAngle > 0.0F || this.viewerCount > 0 && this.animationAngle < 1.0F) {
            float g = this.animationAngle;
            if (this.viewerCount > 0) {
                this.animationAngle += 0.1F;
            } else {
                this.animationAngle -= 0.1F;
            }

            if (this.animationAngle > 1.0F) {
                this.animationAngle = 1.0F;
            }

            if (this.animationAngle < 0.5F && g >= 0.5F) {
                this.playSound(SoundEvents.BLOCK_CHEST_CLOSE);
            }

            if (this.animationAngle < 0.0F) {
                this.animationAngle = 0.0F;
            }
        }
    }

    public boolean onSyncedBlockEvent(int type, int data) {
        if (type == 1) {
            this.viewerCount = data;
            return true;
        } else {
            return super.onSyncedBlockEvent(type, data);
        }
    }

    public void onClose(PlayerEntity player) {
        if (!player.isSpectator()) {
            --this.viewerCount;
        }
        this.onInvOpenOrClose();
    }

    private void playSound(SoundEvent soundEvent) {
        double d = (double)this.pos.getX() + 0.5D;
        double e = (double)this.pos.getY() + 0.5D;
        double f = (double)this.pos.getZ() + 0.5D;
        this.world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
    }

    @Environment(EnvType.CLIENT)
    public float getAnimationProgress(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastAnimationAngle, this.animationAngle);
    }

}
