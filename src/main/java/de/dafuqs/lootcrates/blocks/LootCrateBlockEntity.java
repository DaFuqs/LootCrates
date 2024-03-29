package de.dafuqs.lootcrates.blocks;

import de.dafuqs.lootcrates.LootCrateAtlas;
import de.dafuqs.lootcrates.LootCrates;
import de.dafuqs.lootcrates.blocks.chest.ChestLootCrateBlockEntity;
import de.dafuqs.lootcrates.blocks.modes.InventoryDeletionMode;
import de.dafuqs.lootcrates.blocks.modes.LockMode;
import de.dafuqs.lootcrates.blocks.modes.ReplenishMode;
import de.dafuqs.lootcrates.enums.LootCrateRarity;
import de.dafuqs.lootcrates.enums.LootCrateTagNames;
import de.dafuqs.lootcrates.enums.ScheduledTickEvent;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.*;

public abstract class LootCrateBlockEntity extends LootableContainerBlockEntity {
    
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
    private HashMap<UUID, PlayerCrateData> playerCrateData = new HashMap<>();

    
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
    
    public boolean tryGenerateLoot(PlayerEntity player) {
        boolean inventoryCleared = false;
        if(inventoryDeletionMode == InventoryDeletionMode.ON_OPEN) {
            this.clear();
            inventoryCleared = true;
        }
        
        boolean canGenerateNewLoot = canReplenish(player);
        if(canGenerateNewLoot) {
            if(!inventoryCleared && inventoryDeletionMode == InventoryDeletionMode.WHEN_REPLENISHED) {
                this.clear();
            }
    
            setReplenishTimeToNow(player);
        }
        return canGenerateNewLoot;
    }
    
    private void setReplenishTimeToNow(PlayerEntity player) {
        long time = this.replenishMode.usesRealTime ? ZonedDateTime.now().toInstant().toEpochMilli() : world.getTime();
        
        if(trackedPerPlayer) {
            if(this.playerCrateData.containsKey(player.getUuid())) {
                PlayerCrateData playerCrateData = this.playerCrateData.get(player.getUuid());
                playerCrateData.replenishTime = time;
            } else {
                playerCrateData.put(player.getUuid(), new PlayerCrateData(time, -1));
            }
        } else {
            if(defaultCrateData == null) {
                defaultCrateData = new PlayerCrateData(time, -1);
            } else {
                defaultCrateData.replenishTime = time;
            }
        }
        this.markDirty();
    }
    
    public boolean canReplenish(PlayerEntity player) {
        if(hasWorld()) {
            Optional<PlayerCrateData> playerCrateDataOptional = getPlayerCrateData(player);
            if(playerCrateDataOptional.isEmpty()) {
                return true;
            } else {
                return canReplenish(world, playerCrateDataOptional, replenishMode, replenishTimeTicks);
            }
        }
        return false;
    }
    
    public static boolean canReplenish(World world, Optional<PlayerCrateData> playerCrateData, ReplenishMode replenishMode, long replenishTimeTicks) {
        if(playerCrateData.isEmpty()) {
            return true;
        } else {
            return replenishMode.canReplenish(world, playerCrateData, replenishTimeTicks);
        }
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
            this.lockMode = LockMode.valueOf(tag.getString(LootCrateTagNames.LockMode.toString()).toUpperCase(Locale.ROOT));
        } else {
            this.lockMode = LockMode.NONE;
        }
        if(tag.contains(LootCrateTagNames.ReplenishMode.toString())) {
            this.replenishMode = ReplenishMode.valueOf(tag.getString(LootCrateTagNames.ReplenishMode.toString()).toUpperCase(Locale.ROOT));
        } else {
            this.replenishMode = ReplenishMode.NEVER;
        }
        if(tag.contains(LootCrateTagNames.InventoryDeletionMode.toString())) {
            this.inventoryDeletionMode = InventoryDeletionMode.valueOf(tag.getString(LootCrateTagNames.InventoryDeletionMode.toString()).toUpperCase(Locale.ROOT));
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
            return Text.literal(name.getString().substring(2));
        } else {
            return name;
        }
    }

    public void relockIfNecessary(PlayerEntity player) {
        Optional<PlayerCrateData> playerCrateDataOptional = getPlayerCrateData(player);
        if(shouldRelock(world, replenishMode, replenishTimeTicks, lockMode, playerCrateDataOptional)) {
            playerCrateDataOptional.get().unlockTime = -1;
            this.markDirty();
        }
    }
    
    public static boolean shouldRelock(World world, ReplenishMode replenishMode, long replenishTimeTicks, LockMode lockMode, Optional<PlayerCrateData> playerCrateData) {
        if (lockMode.relocks()) {
            if(playerCrateData.isPresent()) {
                if(playerCrateData.get().unlockTime < playerCrateData.get().replenishTime && LootCrateBlockEntity.canReplenish(world, playerCrateData, replenishMode, replenishTimeTicks)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isUnlocked(PlayerEntity player) {
        Optional<PlayerCrateData> optionalPlayerCrateData = getPlayerCrateData(player);
        return lockMode.isUnlocked(optionalPlayerCrateData);
    }

    public boolean isTrapped() {
        return this.trapped;
    }

    public boolean doesItemUnlock(Item item) {
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
        Optional<PlayerCrateData> optionalPlayerCrateData = getPlayerCrateData(player);
        
        if(optionalPlayerCrateData.isPresent()) {
            PlayerCrateData playerCrateData = optionalPlayerCrateData.get();
            playerCrateData.unlockTime = world.getTime();
        } else {
            long time = this.replenishMode.usesRealTime ? ZonedDateTime.now().toInstant().toEpochMilli() : world.getTime();
            if (trackedPerPlayer) {
                playerCrateData.put(player.getUuid(), new PlayerCrateData(-1, time));
            } else {
                defaultCrateData = new PlayerCrateData(-1, time);
            }
        }
        
        this.markDirty();
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

    protected static void playSound(@NotNull World world, @NotNull BlockPos pos, BlockState state, SoundEvent soundEvent) {
        double d = (double)pos.getX() + 0.5D;
        double e = (double)pos.getY() + 0.5D;
        double f = (double)pos.getZ() + 0.5D;

        world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
    }

    public abstract int getCurrentLookingPlayers(BlockView world, BlockPos pos);

}
