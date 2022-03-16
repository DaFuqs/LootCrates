package de.dafuqs.lootcrates.blocks;

import de.dafuqs.lootcrates.LootCrateAtlas;
import de.dafuqs.lootcrates.LootCrates;
import de.dafuqs.lootcrates.blocks.chest.ChestLootCrateBlockEntity;
import de.dafuqs.lootcrates.blocks.modes.LockMode;
import de.dafuqs.lootcrates.enums.LootCrateRarity;
import de.dafuqs.lootcrates.enums.LootCrateTagNames;
import de.dafuqs.lootcrates.enums.ScheduledTickEvent;
import de.dafuqs.lootcrates.items.LootKeyItem;
import de.dafuqs.lootcrates.blocks.modes.InventoryDeletionMode;
import de.dafuqs.lootcrates.blocks.modes.ReplenishMode;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class LootCrateBlockEntity extends LootableContainerBlockEntity {

    public class PlayerCrateData {
        private UUID playerUUID;
        private long lastReplenishTime;
        private long lastUnlockTime; // for the relock to only lock back up when there is new loot to generate
        private boolean unlocked;
        
        public PlayerCrateData(UUID playerUUID, long time) {
            this.playerUUID = playerUUID;
            this.lastReplenishTime = time;
            this.lastUnlockTime = time;
            this.unlocked = true;
        }
    }
    
    protected DefaultedList<ItemStack> inventory;

    private LockMode lockMode;
    private ReplenishMode replenishMode;
    private InventoryDeletionMode inventoryDeletionMode;
    private boolean trapped;
    private long replenishTimeTicks;
    private boolean trackedPerPlayer;
    private List<PlayerCrateData> playerCrateData;

    private ScheduledTickEvent scheduledTickEvent;
    
    protected LootCrateBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        this.inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
        this.playerCrateData = new ArrayList<>();
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        Inventories.writeNbt(tag, this.inventory, false);
        addLootCrateBlockTags(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if(tag.contains("Items", 9)) {
            Inventories.readNbt(tag, this.inventory);
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
        playSound(soundEvent, 1.0F);
    }

    protected void playSound(SoundEvent soundEvent, float volume) {
        if(hasWorld()) {
            double d = (double) this.pos.getX() + 0.5D;
            double e = (double) this.pos.getY() + 0.5D;
            double f = (double) this.pos.getZ() + 0.5D;
            this.world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5F * volume, this.world.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    public boolean shouldGenerateNewLoot(PlayerEntity player, boolean test) {
        if(hasWorld()) {
            // if replenish time is set to <=0: just generate loot once
            
            if(this.replenishTimeTicks <= 0) {
                if(this.lastReplenishTimeTick == 0) {
                    this.lastReplenishTimeTick = world.getTime();
                    return true;
                } else {
                    return false;
                }
            } else {
                // check if there was enough time since the last opening
                if (lastReplenishTimeTick == 0 || this.world.getTime() > this.lastReplenishTimeTick + this.replenishTimeTicks) {
                    if (this.trackedPerPlayer) {
                        if (this.registeredPlayerUUIDsAndLastLootReplenishTime.contains(player.getUuid())) {
                            return false;
                        } else {
                            this.lastReplenishTimeTick = world.getTime();
                            if(!test) {
                                this.registeredPlayerUUIDsAndLastLootReplenishTime.add(player.getUuid());
                                this.markDirty();
                            }
                            return true;
                        }
                    } else {
                        this.lastReplenishTimeTick = world.getTime();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public NbtCompound serializeInventory(NbtCompound tag) {
        Inventories.writeNbt(tag, this.inventory, false);
        return tag;
    }

    @Override
    public boolean isEmpty() {
        return this.getInvStackList().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public void checkLootInteraction(@Nullable PlayerEntity player) {
        // only players can generate container loot
        if (player != null && this.lootTableId != null && this.world.getServer() != null && shouldGenerateNewLoot(player, false)) {
            LootTable lootTable = this.world.getServer().getLootManager().getTable(this.lootTableId);
            if (player instanceof ServerPlayerEntity) {
                Criteria.PLAYER_GENERATES_CONTAINER_LOOT.trigger((ServerPlayerEntity)player, this.lootTableId);
            }
            LootContext.Builder builder = (new LootContext.Builder((ServerWorld)this.world)).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(this.pos)).random(this.lootTableSeed);
            builder.luck(player.getLuck()).parameter(LootContextParameters.THIS_ENTITY, player);
            lootTable.supplyInventory(this, builder.build(LootContextTypes.CHEST));
        }
    }

    public NbtCompound addLootCrateBlockTags(NbtCompound tag) {
        if(this.replenishTimeTicks != 0) {
            tag.putLong(LootCrateTagNames.ReplenishTimeTicks.toString(), this.replenishTimeTicks);
        }

        if(this.lastReplenishTimeTick > 0) {
            tag.putLong(LootCrateTagNames.LastReplenishTimeTick.toString(), this.lastReplenishTimeTick);
        }
        if(this.locked) {
            tag.putBoolean(LootCrateTagNames.Locked.toString(), true);
        }
        if(this.doNotConsumeKeyOnUnlock) {
            tag.putBoolean(LootCrateTagNames.DoNotConsumeKeyOnUnlock.toString(), true);
        }
        if(this.relocksForNewLoot) {
            tag.putBoolean(LootCrateTagNames.RelocksWhenNewLoot.toString(), true);
            tag.putLong(LootCrateTagNames.LastUnlockTimeTick.toString(), this.lastUnlockTimeTick);
        }
        if(this.trapped) {
            tag.putBoolean(LootCrateTagNames.Trapped.toString(), true);
        }
        if(this.trackedPerPlayer) {
            tag.putBoolean(LootCrateTagNames.OncePerPlayer.toString(), true);
            if(this.registeredPlayerUUIDsAndLastLootReplenishTime.size() > 0) {
                NbtList registeredPlayers = new NbtList();
                for (UUID uuid : this.registeredPlayerUUIDsAndLastLootReplenishTime) {
                    registeredPlayers.add(NbtHelper.fromUuid(uuid));
                }
                tag.put(LootCrateTagNames.RegisteredPlayerUUIDs.toString(), registeredPlayers);
            }
        }
        this.serializeLootTable(tag);

        return tag;
    }

    public void setLootCrateBlockTags(NbtCompound tag) {
        this.registeredPlayerUUIDsAndLastLootReplenishTime = new ArrayList<>();

        this.deserializeLootTable(tag);

        if(tag.contains(LootCrateTagNames.ReplenishTimeTicks.toString())) {
            this.replenishTimeTicks = tag.getLong(LootCrateTagNames.ReplenishTimeTicks.toString());
        } else {
            this.replenishTimeTicks = -1;
        }
        if(tag.contains(LootCrateTagNames.LastReplenishTimeTick.toString())) {
            this.lastReplenishTimeTick = tag.getLong(LootCrateTagNames.LastReplenishTimeTick.toString());
        } else {
            this.lastReplenishTimeTick = 0;
        }
        if(tag.contains(LootCrateTagNames.LastUnlockTimeTick.toString())) {
            this.lastUnlockTimeTick = tag.getLong(LootCrateTagNames.LastUnlockTimeTick.toString());
        } else {
            this.lastUnlockTimeTick = 0;
        }

        this.locked = tag.contains(LootCrateTagNames.Locked.toString()) && tag.getBoolean(LootCrateTagNames.Locked.toString());
        this.doNotConsumeKeyOnUnlock = tag.contains(LootCrateTagNames.DoNotConsumeKeyOnUnlock.toString()) && tag.getBoolean(LootCrateTagNames.DoNotConsumeKeyOnUnlock.toString());
        this.relocksForNewLoot = tag.contains(LootCrateTagNames.RelocksWhenNewLoot.toString()) && tag.getBoolean(LootCrateTagNames.RelocksWhenNewLoot.toString());
        this.trapped = tag.contains(LootCrateTagNames.Trapped.toString()) && tag.getBoolean(LootCrateTagNames.Trapped.toString());

        if(tag.contains(LootCrateTagNames.OncePerPlayer.toString()) && tag.getBoolean(LootCrateTagNames.OncePerPlayer.toString())) {
            this.trackedPerPlayer = true;
            if(tag.contains(LootCrateTagNames.RegisteredPlayerUUIDs.toString())) {
                NbtList playerUUIDs = tag.getList(LootCrateTagNames.RegisteredPlayerUUIDs.toString(), 11);
                for (NbtElement playerUUID : playerUUIDs) {
                    this.registeredPlayerUUIDsAndLastLootReplenishTime.add(NbtHelper.toUuid(playerUUID));
                }
            }
        } else {
            this.trackedPerPlayer = false;
        }
    }

    protected void onInvOpenOrClose(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
        if (this.trapped && oldViewerCount != newViewerCount) {
            Block block = state.getBlock();
            world.updateNeighborsAlways(pos, block);
            world.updateNeighborsAlways(pos.down(), block);
        }
    }

    @Override
    protected Text getContainerName() {
        BlockState state = this.world.getBlockState(this.getPos());
        Text name = state.getBlock().getName();
        if(name.getString().startsWith("§")) {
            return new LiteralText(name.getString().substring(2));
        } else {
            return name;
        }
    }

    public void checkRelock(PlayerEntity player) {
        if(this.relocksForNewLoot && !this.locked && this.lastUnlockTimeTick < this.lastReplenishTimeTick && this.shouldGenerateNewLoot(player, true)) {
            this.locked = true;
        }
    }

    public boolean isLocked(PlayerEntity player) {
        return locked;
    }

    public boolean isTrapped() {
        return this.trapped;
    }

    public boolean doesUnlock(Item item) {
        if(world != null && item instanceof LootKeyItem) {
            Optional<LootCrateBlock> block = getBlock();
            if(block.isPresent()) {
                LootCrateRarity itemRarity = LootCrateAtlas.getKeyRarity((LootKeyItem) item);
                LootCrateRarity blockRarity = LootCrateBlock.getCrateRarity(block.get());
                return itemRarity.equals(blockRarity);
            }
        }
        
        return false;
    }

    public LockMode getLockType() {
        return this.lockMode;
    }

    public void unlock(World world, PlayerEntity playerEntity) {
        
        this.locked = false;
        this.lastUnlockTimeTick = world.getTime();
        this.playSound(LootCrates.CHEST_UNLOCKS_SOUND_EVENT);
    }

    public Optional<LootCrateBlock> getBlock() {
        if(this.world == null) {
            return Optional.empty();
        } else {
            Block block = this.world.getBlockState(this.pos).getBlock();
            if(block instanceof LootCrateBlock lootCrateBlock) {
                return Optional.of(lootCrateBlock);
            } else {
                return Optional.empty();
            }
        }
    }

    public ScheduledTickEvent getRandomTickEvent() {
        if(this.scheduledTickEvent == null) {
            Optional<LootCrateBlock> block = getBlock();
            if(block.isPresent()) {
                scheduledTickEvent = LootCrateAtlas.getRandomTickEvent(block.get());
            }
        }
        return this.scheduledTickEvent;
    }

    public void playOpenSoundEffect() {
        // play default sound
        if(this instanceof ChestLootCrateBlockEntity) {
            playSound(SoundEvents.BLOCK_CHEST_OPEN);
        } else {
            playSound(SoundEvents.BLOCK_SHULKER_BOX_OPEN);
        }

        // also play custom sound, if set
        SoundEvent customSoundEvent = null;
        Optional<LootCrateBlock> block = getBlock();
        if(block.isPresent()) {
            customSoundEvent = LootCrateAtlas.getCustomOpenSoundEvent(block.get());
        }

        if(customSoundEvent != null) {
            playSound(customSoundEvent, 0.4F);
        }
    }

    public void playCloseSoundEffect() {
        // play default sound
        if(this instanceof ChestLootCrateBlockEntity) {
            playSound(SoundEvents.BLOCK_CHEST_CLOSE);
        } else {
            playSound(SoundEvents.BLOCK_SHULKER_BOX_CLOSE);
        }

        // also play custom sound, if set
        SoundEvent customSoundEvent = null;
        Optional<LootCrateBlock> block = getBlock();
        if(block.isPresent()) {
            customSoundEvent = LootCrateAtlas.getCustomCloseSoundEvent(block.get());
        }
        
        if(customSoundEvent != null) {
            playSound(customSoundEvent, 0.4F);
        }
    }

    protected static void playSound(World world, BlockPos pos, BlockState state, SoundEvent soundEvent) {
        double d = (double)pos.getX() + 0.5D;
        double e = (double)pos.getY() + 0.5D;
        double f = (double)pos.getZ() + 0.5D;

        world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
    }

    public void setTrackedPerPlayer(boolean trackedPerPlayer) {
        this.trackedPerPlayer = trackedPerPlayer;
    }

    public void setReplenishTimeTicks(int ticks) {
        this.replenishTimeTicks = ticks;
    }

    public void setTrapped(boolean trapped) {
        this.trapped = trapped;
    }

    public abstract int getCurrentLookingPlayers(BlockView world, BlockPos pos);

}
