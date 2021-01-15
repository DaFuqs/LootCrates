package de.dafuqs.lootcrates.blocks;

import de.dafuqs.lootcrates.enums.LootCrateTagNames;
import de.dafuqs.lootcrates.items.LootKeyItem;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Rarity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class LootCrateBlockEntity extends LootableContainerBlockEntity {

    private DefaultedList<ItemStack> inventory;

    private boolean locked;
    private boolean doNotConsumeKeyOnUnlock;
    private boolean oncePerPlayer;
    private List<UUID> registeredPlayerUUIDs;

    private long replenishTimeTicks;
    private long lastReplenishTimeTick;

    protected LootCrateBlockEntity(BlockEntityType<?> blockEntityType, DefaultedList<ItemStack> inventory) {
        super(blockEntityType);
        this.inventory = inventory;
        registeredPlayerUUIDs = new ArrayList<>();
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);

        Inventories.toTag(tag, this.inventory, false);
        tag = addLootCrateBlockTags(tag);

        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);

        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if(tag.contains("Items", 9)) {
            Inventories.fromTag(tag, this.inventory);
        }

        setLootCrateBlockTags(tag);
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    protected DefaultedList<ItemStack> getInvStackList() {
        return this.inventory;
    }

    @Override
    protected void setInvStackList(DefaultedList<ItemStack> list) {
        this.inventory = list;
    }

    protected void playSound(SoundEvent soundEvent) {
        if(hasWorld()) {
            double d = (double) this.pos.getX() + 0.5D;
            double e = (double) this.pos.getY() + 0.5D;
            double f = (double) this.pos.getZ() + 0.5D;
            this.world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    public boolean shouldGenerateNewLoot(PlayerEntity player) {
        if(hasWorld()) {
            // if replenish time is set to -1: just generate loot once
            if(this.replenishTimeTicks <= 0 && this.lastReplenishTimeTick == 0) {
                this.lastReplenishTimeTick = world.getTime();
                return true;
            }
            // check if there was enough time since the last opening
            if (lastReplenishTimeTick == 0 || this.world.getTime() > this.lastReplenishTimeTick + this.replenishTimeTicks) {
                if (this.oncePerPlayer) {
                    if (this.registeredPlayerUUIDs.contains(player.getUuid())) {
                        return false;
                    } else {
                        this.lastReplenishTimeTick = world.getTime();
                        this.registeredPlayerUUIDs.add(player.getUuid());
                        this.markDirty();
                        return true;
                    }
                } else {
                    this.lastReplenishTimeTick = world.getTime();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.getInvStackList().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public void checkLootInteraction(@Nullable PlayerEntity player) {
        // only players can generate container loot
        if (player != null && this.lootTableId != null && this.world.getServer() != null && shouldGenerateNewLoot(player)) {
            LootTable lootTable = this.world.getServer().getLootManager().getTable(this.lootTableId);
            if (player instanceof ServerPlayerEntity) {
                Criteria.PLAYER_GENERATES_CONTAINER_LOOT.test((ServerPlayerEntity)player, this.lootTableId);
            }
            LootContext.Builder builder = (new LootContext.Builder((ServerWorld)this.world)).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(this.pos)).random(this.lootTableSeed);
            builder.luck(player.getLuck()).parameter(LootContextParameters.THIS_ENTITY, player);
            lootTable.supplyInventory(this, builder.build(LootContextTypes.CHEST));
        }
    }

    public CompoundTag addLootCrateBlockTags(CompoundTag tag) {
        if(this.replenishTimeTicks != 0) {
            tag.putLong(LootCrateTagNames.ReplenishTimeTicks.toString(), this.replenishTimeTicks);
        }
        if(this.lastReplenishTimeTick != 0) {
            tag.putLong(LootCrateTagNames.LastReplenishTimeTick.toString(), this.lastReplenishTimeTick);
        }
        if(this.locked) {
            tag.putBoolean(LootCrateTagNames.Locked.toString(), true);
            if(this.doNotConsumeKeyOnUnlock) {
                tag.putBoolean(LootCrateTagNames.DoNotConsumeKeyOnUnlock.toString(), true);
            }
        }
        if(this.oncePerPlayer) {
            tag.putBoolean(LootCrateTagNames.OncePerPlayer.toString(), true);
            if(this.registeredPlayerUUIDs.size() > 0) {
                ListTag registeredPlayers = new ListTag();
                for (UUID uuid : this.registeredPlayerUUIDs) {
                    registeredPlayers.add(NbtHelper.fromUuid(uuid));
                }
                tag.put(LootCrateTagNames.RegisteredPlayerUUIDs.toString(), registeredPlayers);
            }
        }
        this.serializeLootTable(tag);

        return tag;
    }

    public void setLootCrateBlockTags(CompoundTag tag) {
        this.registeredPlayerUUIDs = new ArrayList<>();

        this.deserializeLootTable(tag);

        if(tag.contains(LootCrateTagNames.ReplenishTimeTicks.toString())) {
            this.replenishTimeTicks = tag.getLong(LootCrateTagNames.ReplenishTimeTicks.toString());
        } else {
            this.replenishTimeTicks = 0;
        }
        if(tag.contains(LootCrateTagNames.LastReplenishTimeTick.toString())) {
            this.lastReplenishTimeTick = tag.getLong(LootCrateTagNames.LastReplenishTimeTick.toString());
        } else {
            this.lastReplenishTimeTick = 0;
        }

        if(tag.contains(LootCrateTagNames.Locked.toString()) && tag.getBoolean(LootCrateTagNames.Locked.toString())) {
            this.locked = true;
            this.doNotConsumeKeyOnUnlock = tag.contains(LootCrateTagNames.DoNotConsumeKeyOnUnlock.toString()) && tag.getBoolean(LootCrateTagNames.DoNotConsumeKeyOnUnlock.toString());
        } else {
            this.locked = false;
            this.doNotConsumeKeyOnUnlock = false;
        }

        if(tag.contains(LootCrateTagNames.OncePerPlayer.toString()) && tag.getBoolean(LootCrateTagNames.OncePerPlayer.toString())) {
            this.oncePerPlayer = true;
            if(tag.contains(LootCrateTagNames.RegisteredPlayerUUIDs.toString())) {
                ListTag playerUUIDs = tag.getList(LootCrateTagNames.RegisteredPlayerUUIDs.toString(), 11);
                for (Tag playerUUID : playerUUIDs) {
                    this.registeredPlayerUUIDs.add(NbtHelper.toUuid(playerUUID));
                }
            }
        } else {
            this.oncePerPlayer = false;
        }
    }

    public boolean isLocked() {
        return this.locked;
    }

    public boolean doesUnlock(Item item) {
        if(world == null) {
            return false;
        }

        Block block = world.getBlockState(pos).getBlock();

        if(item instanceof LootKeyItem && block instanceof LootCrateBlock) {
            Rarity itemRarity = LootKeyItem.getKeyRarity((LootKeyItem) item);
            Rarity blockRarity = LootCrateBlock.getCrateRarity(block);
            return itemRarity.equals(blockRarity);
        } else {
            return false;
        }
    }

    public boolean doesConsumeKeyOnUnlock() {
        return !doNotConsumeKeyOnUnlock;
    }

    public void unlock() {
        this.locked = false;
    }
}
