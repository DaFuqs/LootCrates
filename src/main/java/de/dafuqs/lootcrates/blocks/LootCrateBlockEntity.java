package de.dafuqs.lootcrates.blocks;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public abstract class LootCrateBlockEntity extends LootableContainerBlockEntity {

    private DefaultedList<ItemStack> inventory;

    private long replenishTimeTicks;
    private long lastReplenishTimeTick;

    protected LootCrateBlockEntity(BlockEntityType<?> blockEntityType, DefaultedList<ItemStack> inventory) {
        super(blockEntityType);
        this.inventory = inventory;
    }

    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putLong("ReplenishTimeTicks", this.replenishTimeTicks);
        tag.putLong("LastReplenishTimeTick", this.lastReplenishTimeTick);

        this.serializeLootTable(tag);
        Inventories.toTag(tag, this.inventory, false);

        return tag;
    }

    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);

        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        this.deserializeLootTable(tag);
        if(tag.contains("Items", 9)) {
            Inventories.fromTag(tag, this.inventory);
        }

        this.replenishTimeTicks = tag.getLong("ReplenishTimeTicks");
        this.lastReplenishTimeTick = tag.getLong("LastReplenishTimeTick");

        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(tag)) {
            Inventories.fromTag(tag, this.inventory);
        }
    }

    public int size() {
        return this.inventory.size();
    }

    protected DefaultedList<ItemStack> getInvStackList() {
        return this.inventory;
    }

    protected void setInvStackList(DefaultedList<ItemStack> list) {
        this.inventory = list;
    }

    protected void playSound(SoundEvent soundEvent) {
        double d = (double)this.pos.getX() + 0.5D;
        double e = (double)this.pos.getY() + 0.5D;
        double f = (double)this.pos.getZ() + 0.5D;
        this.world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
    }

    public boolean shouldGenerateNewLoot() {
        return(this.lastReplenishTimeTick >= this.world.getTime() + this.replenishTimeTicks);
    }

    @Override
    public void checkLootInteraction(@Nullable PlayerEntity player) {
        if (shouldGenerateNewLoot()) {
            super.checkLootInteraction(player);
        }
    }

    public CompoundTag getBlockEntityTag() {
        return toTag(new CompoundTag());
    }

}
