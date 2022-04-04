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
import net.minecraft.nbt.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class LootCrateBlockEntity extends LootableContainerBlockEntity {
    
    public static class PlayerCrateData {
        private long lastReplenishTime;
        private long lastUnlockTime; // for the relock to only lock back up when there is new loot to generate
        private boolean unlocked;
        
        public PlayerCrateData(long lastReplenishTime, long lastUnlockTime, boolean unlocked) {
            this.lastReplenishTime = lastReplenishTime;
            this.lastUnlockTime = lastUnlockTime;
            this.unlocked = unlocked;
        }
        
        public PlayerCrateData(long time) {
            this.lastReplenishTime = time;
            this.lastUnlockTime = time;
            this.unlocked = true;
        }
    
        public long getLastReplenishTime() {
            return lastReplenishTime;
        }
    
        public long getLastUnlockTime() {
            return lastUnlockTime;
        }
    
        public boolean isUnlocked() {
            return unlocked;
        }
        
        public void setUnlocked(boolean unlocked, long time) {
            if(!this.unlocked && unlocked) {
                this.lastUnlockTime = time;
            }
            this.unlocked = unlocked;
        }
        
        public NbtCompound toCompound(@Nullable UUID uuid) {
            NbtCompound nbtCompound = new NbtCompound();
            if(uuid != null) {
                nbtCompound.putUuid("UUID", uuid);
            }
            nbtCompound.putLong("LastReplenishTime", getLastReplenishTime());
            nbtCompound.putLong("LastUnlockTime", getLastUnlockTime());
            nbtCompound.putBoolean("Unlocked", isUnlocked());
            return nbtCompound;
        }
        
        @Contract("_ -> new")
        public static @NotNull Pair<UUID, PlayerCrateData> fromCompoundWithUUID(@NotNull NbtCompound nbtCompound) {
            UUID uuid = null;
            if(nbtCompound.contains("UUID")) {
                uuid = nbtCompound.getUuid("UUID");
            }
            return new Pair<>(uuid, fromCompoundWithoutUUID(nbtCompound));
        }
        
        @Contract("_ -> new")
        public static @NotNull PlayerCrateData fromCompoundWithoutUUID(@NotNull NbtCompound nbtCompound) {
            return new PlayerCrateData(nbtCompound.getLong("LastReplenishTime"), nbtCompound.getLong("LastUnlockTime"), nbtCompound.getBoolean("Unlocked"));
        }
        
    }
    
    protected DefaultedList<ItemStack> inventory;

    private LockMode lockMode;
    private ReplenishMode replenishMode;
    private InventoryDeletionMode inventoryDeletionMode;
    private boolean trapped;
    private long replenishTimeTicks;
    private boolean trackedPerPlayer;
    
    private ScheduledTickEvent scheduledTickEvent;
    
    @Nullable
    private PlayerCrateData defaultCrateData = null;
    @Nullable
    private HashMap<UUID, PlayerCrateData> playerCrateData = null;

    
    protected LootCrateBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        
        this.lockMode = LockMode.NONE;
        this.replenishMode = ReplenishMode.NEVER;
        this.inventoryDeletionMode = InventoryDeletionMode.NEVER;
        this.inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        Inventories.writeNbt(tag, this.inventory, false);
        putLootCrateBlockTags(tag);
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
    
    public Optional<PlayerCrateData> getPlayerCrateData(PlayerEntity playerEntity) {
        if(trackedPerPlayer) {
            if(playerCrateData.containsKey(playerEntity.getUuid())) {
                return Optional.of(playerCrateData.get(playerEntity.getUuid()));
            }
            return Optional.empty();
        } else {
            if(defaultCrateData == null) {
                return Optional.empty();
            }
            return Optional.of(defaultCrateData);
        }
    }
    
    public void setPlayerCrateData(PlayerEntity playerEntity, long time) {
        if(trackedPerPlayer) {
            playerCrateData.put(playerEntity.getUuid(), new PlayerCrateData(time));
        } else {
            defaultCrateData = new PlayerCrateData(time);
        }
        this.markDirty();
    }
    
    public boolean tryGenerateLoot(PlayerEntity player) {
        boolean canGenerateNewLoot = canGenerateNewLoot(player);
        if(canGenerateNewLoot) {
            setPlayerCrateData(player, world.getTime());
        }
        return canGenerateNewLoot;
    }

    public boolean canGenerateNewLoot(PlayerEntity player) {
        if(hasWorld()) {
            Optional<PlayerCrateData> playerCrateDataOptional = getPlayerCrateData(player);
            if(playerCrateDataOptional.isEmpty()) {
                return true;
            } else {
                PlayerCrateData playerCrateData = playerCrateDataOptional.get();
                switch (this.replenishMode) {
                    case NEVER -> {
                        // crate was opened before (in general or by that player)
                        // => just generate loot once
                        return false;
                    }
                    case INVERVAL -> {
                        long currMod = this.world.getTime() / this.replenishTimeTicks;
                        long lastMod = playerCrateData.getLastReplenishTime() / this.replenishTimeTicks;
                        return currMod > lastMod;
                    }
                    case PASSED_TIME_SINCE_LAST_OPEN -> {
                        // check if there was enough time since the last opening
                        return this.world.getTime() > playerCrateData.getLastReplenishTime() + this.replenishTimeTicks;
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
        if (player != null && this.lootTableId != null && this.world.getServer() != null && tryGenerateLoot(player)) {
            LootTable lootTable = this.world.getServer().getLootManager().getTable(this.lootTableId);
            if (player instanceof ServerPlayerEntity) {
                Criteria.PLAYER_GENERATES_CONTAINER_LOOT.trigger((ServerPlayerEntity)player, this.lootTableId);
            }
            LootContext.Builder builder = (new LootContext.Builder((ServerWorld)this.world)).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(this.pos)).random(this.lootTableSeed);
            builder.luck(player.getLuck()).parameter(LootContextParameters.THIS_ENTITY, player);
            lootTable.supplyInventory(this, builder.build(LootContextTypes.CHEST));
        }
    }

    public NbtCompound putLootCrateBlockTags(NbtCompound tag) {
        this.serializeLootTable(tag);
        
        tag.putBoolean(LootCrateTagNames.TrackedPerPlayer.toString(), this.trackedPerPlayer);
        if(this.trackedPerPlayer) {
            NbtList playerList = new NbtList();
            for(Map.Entry<UUID, PlayerCrateData> playerCrateData : this.playerCrateData.entrySet()) {
                playerList.add(playerCrateData.getValue().toCompound(playerCrateData.getKey()));
            }
            tag.put("PlayerData", playerList);
        } else {
            PlayerCrateData playerCrateData = this.defaultCrateData;
            if(playerCrateData != null) {
                tag.put("Data", playerCrateData.toCompound(null));
            }
        }
        
        tag.putString(LootCrateTagNames.LockMode.toString(), this.lockMode.toString());
        tag.putString(LootCrateTagNames.ReplenishMode.toString(), this.replenishMode.toString());
        tag.putString(LootCrateTagNames.InventoryDeletionMode.toString(), this.inventoryDeletionMode.toString());
        tag.putBoolean(LootCrateTagNames.Trapped.toString(), this.trapped);
        tag.putLong(LootCrateTagNames.ReplenishTimeTicks.toString(), this.replenishTimeTicks);
        
        return tag;
    }

    public void setLootCrateBlockTags(NbtCompound tag) {
        this.deserializeLootTable(tag);
        
        if(tag.contains(LootCrateTagNames.TrackedPerPlayer.toString()) && tag.getBoolean(LootCrateTagNames.TrackedPerPlayer.toString())) {
            this.trackedPerPlayer = true;
            this.playerCrateData = new HashMap<>();
            NbtList nbtList = tag.getList("PlayerData", NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < nbtList.size(); i++) {
                Pair<UUID, PlayerCrateData> data = PlayerCrateData.fromCompoundWithUUID(nbtList.getCompound(i));
                this.playerCrateData.put(data.getLeft(), data.getRight());
            }
        } else {
            this.trackedPerPlayer = false;
            if(tag.contains("Data", NbtElement.COMPOUND_TYPE)) {
                this.defaultCrateData = PlayerCrateData.fromCompoundWithoutUUID(tag.getCompound("Data"));
            }
        }
        
        if(tag.contains(LootCrateTagNames.ReplenishTimeTicks.toString())) {
            this.replenishTimeTicks = tag.getLong(LootCrateTagNames.ReplenishTimeTicks.toString());
        } else {
            this.replenishTimeTicks = -1;
        }
        
        this.trapped = tag.contains(LootCrateTagNames.Trapped.toString()) && tag.getBoolean(LootCrateTagNames.Trapped.toString());
    
        if(tag.contains(LootCrateTagNames.LockMode.toString())) {
            this.lockMode = LockMode.valueOf(tag.getString(LootCrateTagNames.LockMode.toString()));
        } else {
            this.lockMode = LockMode.NONE;
        }
        if(tag.contains(LootCrateTagNames.ReplenishMode.toString())) {
            this.replenishMode = ReplenishMode.valueOf(tag.getString(LootCrateTagNames.ReplenishMode.toString()));
        } else {
            this.replenishMode = ReplenishMode.NEVER;
        }
        if(tag.contains(LootCrateTagNames.InventoryDeletionMode.toString())) {
            this.inventoryDeletionMode = InventoryDeletionMode.valueOf(tag.getString(LootCrateTagNames.InventoryDeletionMode.toString()));
        } else {
            this.inventoryDeletionMode = InventoryDeletionMode.NEVER;
        }
    }
    
    public void setData(ReplenishMode replenishMode, int replenishTimeTicks, boolean trackedPerPlayer, LockMode lockMode, InventoryDeletionMode inventoryDeletionMode, boolean trapped) {
        this.replenishMode = replenishMode;
        this.replenishTimeTicks = replenishTimeTicks;
        this.trackedPerPlayer = trackedPerPlayer;
        this.lockMode = lockMode;
        this.inventoryDeletionMode = inventoryDeletionMode;
        this.trapped = trapped;
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
        boolean relocksForNewLoot = this.lockMode.relocks();
        if (relocksForNewLoot) {
            Optional<PlayerCrateData> playerCrateDataOptional = getPlayerCrateData(player);
            
            if(playerCrateDataOptional.isPresent()) {
                PlayerCrateData playerCrateData = playerCrateDataOptional.get();
                 if(playerCrateData.isUnlocked() && playerCrateData.getLastUnlockTime() < playerCrateData.getLastReplenishTime() && this.canGenerateNewLoot(player)) {
                     playerCrateData.setUnlocked(false, world.getTime());
                }
            }
        }
    }

    public boolean isUnlocked(PlayerEntity player) {
        Optional<PlayerCrateData> optionalPlayerCrateData = getPlayerCrateData(player);
        if(optionalPlayerCrateData.isPresent()) {
            return optionalPlayerCrateData.get().isUnlocked();
        }
        return this.lockMode.requiresKey();
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

    public void unlock(World world, PlayerEntity player) {
        setPlayerCrateData(player, world.getTime()); // TODO: unlock
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
