package de.dafuqs.lootcrates.blocks;

import de.dafuqs.lootcrates.LootCratesBlocks;
import de.dafuqs.lootcrates.LootCratesItems;
import de.dafuqs.lootcrates.items.LootKeyItem;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class LootCrateBlockEntity extends LootableContainerBlockEntity {

    private DefaultedList<ItemStack> inventory;

    private boolean locked;
    private boolean doNotConsumeKeyOnUnlock;
    private boolean oncePerPlayer;
    private List<UUID> registeredPlayerGUIDs;

    private long replenishTimeTicks;
    private long lastReplenishTimeTick;

    protected LootCrateBlockEntity(BlockEntityType<?> blockEntityType, DefaultedList<ItemStack> inventory) {
        super(blockEntityType);
        this.inventory = inventory;

        registeredPlayerGUIDs = new ArrayList<>();
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag = addLootCrateBlockTags(tag);
        Inventories.toTag(tag, this.inventory, false);
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
        double d = (double)this.pos.getX() + 0.5D;
        double e = (double)this.pos.getY() + 0.5D;
        double f = (double)this.pos.getZ() + 0.5D;
        this.world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
    }

    public boolean shouldGenerateNewLoot(PlayerEntity player) {
        if(lastReplenishTimeTick == 0 || this.world.getTime() > this.lastReplenishTimeTick + this.replenishTimeTicks) {
            if(this.oncePerPlayer) {
                if(this.registeredPlayerGUIDs.contains(player.getUuid())) {
                    return false;
                } else {
                    this.lastReplenishTimeTick = world.getTime();
                    return true;
                }
            } else {
                this.lastReplenishTimeTick = world.getTime();
                return true;
            }
        }
        return false;
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
        tag.putLong(LootCrateTags.TagNames.ReplenishTimeTicks.toString(), this.replenishTimeTicks);
        tag.putLong(LootCrateTags.TagNames.LastReplenishTimeTick.toString(), this.lastReplenishTimeTick);
        tag.putBoolean(LootCrateTags.TagNames.Locked.toString(), this.locked);
        if(this.locked) {
            tag.putBoolean(LootCrateTags.TagNames.Locked.toString(), true);
            if(doNotConsumeKeyOnUnlock) {
                tag.putBoolean(LootCrateTags.TagNames.DoNotConsumeKeyOnUnlock.toString(), true);
            }
        }
        if(oncePerPlayer) {
            tag.putBoolean(LootCrateTags.TagNames.OncePerPlayer.toString(), true);
            if(registeredPlayerGUIDs.size() > 0) {
                CompoundTag registeredPlayers = new CompoundTag();
                int playerCount = 0;
                for (UUID uuid : this.registeredPlayerGUIDs) {
                    registeredPlayers.putUuid(String.valueOf(playerCount), uuid);
                    playerCount++;
                }
                tag.put(LootCrateTags.TagNames.RegisteredPlayerGUIDs.toString(), registeredPlayers);
            }
        }
        this.serializeLootTable(tag);

        return tag;
    }

    public void setLootCrateBlockTags(CompoundTag tag) {
        this.registeredPlayerGUIDs = new ArrayList<>();

        this.deserializeLootTable(tag);

        if(tag.contains(LootCrateTags.TagNames.ReplenishTimeTicks.toString())) {
            this.replenishTimeTicks = tag.getLong(LootCrateTags.TagNames.ReplenishTimeTicks.toString());
        } else {
            this.replenishTimeTicks = 0;
        }
        if(tag.contains(LootCrateTags.TagNames.LastReplenishTimeTick.toString())) {
            this.lastReplenishTimeTick = tag.getLong(LootCrateTags.TagNames.LastReplenishTimeTick.toString());
        } else {
            this.lastReplenishTimeTick = 0;
        }

        if(tag.contains(LootCrateTags.TagNames.Locked.toString()) && tag.getBoolean(LootCrateTags.TagNames.Locked.toString())) {
            this.locked = true;
            if(tag.contains(LootCrateTags.TagNames.DoNotConsumeKeyOnUnlock.toString())&& tag.getBoolean(LootCrateTags.TagNames.DoNotConsumeKeyOnUnlock.toString())) {
                this.doNotConsumeKeyOnUnlock = true;
            } else {
                this.doNotConsumeKeyOnUnlock = false;
            }
        } else {
            this.locked = false;
            this.doNotConsumeKeyOnUnlock = false;
        }

        if(tag.contains(LootCrateTags.TagNames.OncePerPlayer.toString()) && tag.getBoolean(LootCrateTags.TagNames.OncePerPlayer.toString())) {
            this.oncePerPlayer = true;
            if(tag.contains(LootCrateTags.TagNames.RegisteredPlayerGUIDs.toString())) {
                CompoundTag compoundTag = tag.getCompound(LootCrateTags.TagNames.RegisteredPlayerGUIDs.toString());
                int counter = 0;
                if(compoundTag.contains(String.valueOf(counter))) {
                    UUID uuid = compoundTag.getUuid(String.valueOf(counter));
                    this.registeredPlayerGUIDs.add(uuid);
                    counter++;
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
        Block block = world.getBlockState(pos).getBlock();

        if (item.equals(LootCratesItems.COMMON_CRATE_KEY) && (block.equals(LootCratesBlocks.COMMON_CHEST_LOOT_CRATE) || block.equals(LootCratesBlocks.COMMON_SHULKER_LOOT_CRATE))
                || item.equals(LootCratesItems.UNCOMMON_CRATE_KEY) && (block.equals(LootCratesBlocks.UNCOMMON_CHEST_LOOT_CRATE) || block.equals(LootCratesBlocks.UNCOMMON_SHULKER_LOOT_CRATE))
                || item.equals(LootCratesItems.RARE_CRATE_KEY) && (block.equals(LootCratesBlocks.RARE_CHEST_LOOT_CRATE) || block.equals(LootCratesBlocks.RARE_SHULKER_LOOT_CRATE))
                || item.equals(LootCratesItems.EPIC_CRATE_KEY) && (block.equals(LootCratesBlocks.EPIC_CHEST_LOOT_CRATE) || block.equals(LootCratesBlocks.EPIC_SHULKER_LOOT_CRATE))) {
            return true;
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
