package de.dafuqs.lootcrates.blocks;

import de.dafuqs.lootcrates.LootCrateAtlas;
import de.dafuqs.lootcrates.LootCrates;
import de.dafuqs.lootcrates.blocks.chest.ChestLootCrateBlockEntity;
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
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class LootCrateBlockEntity extends LootableContainerBlockEntity {

    protected DefaultedList<ItemStack> inventory;

    private boolean locked;
    private boolean doNotConsumeKeyOnUnlock;
    private boolean oncePerPlayer;
    private List<UUID> registeredPlayerUUIDs;
    private ScheduledTickEvent scheduledTickEvent;
    private boolean trapped;

    private long replenishTimeTicks;
    private long lastReplenishTimeTick;

    protected LootCrateBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        this.inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
        registeredPlayerUUIDs = new ArrayList<>();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        Inventories.writeNbt(tag, this.inventory, false);
        tag = addLootCrateBlockTags(tag);

        return tag;
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

    public boolean shouldGenerateNewLoot(PlayerEntity player) {
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

    public NbtCompound addLootCrateBlockTags(NbtCompound tag) {
        if(this.replenishTimeTicks != 0) {
            tag.putLong(LootCrateTagNames.ReplenishTimeTicks.toString(), this.replenishTimeTicks);
        }

        if(this.lastReplenishTimeTick > 0) {
            tag.putLong(LootCrateTagNames.LastReplenishTimeTick.toString(), this.lastReplenishTimeTick);
        }
        if(this.locked) {
            tag.putBoolean(LootCrateTagNames.Locked.toString(), true);
            if(this.doNotConsumeKeyOnUnlock) {
                tag.putBoolean(LootCrateTagNames.DoNotConsumeKeyOnUnlock.toString(), true);
            }
        }
        if(this.trapped) {
            tag.putBoolean(LootCrateTagNames.Trapped.toString(), true);
        }
        if(this.oncePerPlayer) {
            tag.putBoolean(LootCrateTagNames.OncePerPlayer.toString(), true);
            if(this.registeredPlayerUUIDs.size() > 0) {
                NbtList registeredPlayers = new NbtList();
                for (UUID uuid : this.registeredPlayerUUIDs) {
                    registeredPlayers.add(NbtHelper.fromUuid(uuid));
                }
                tag.put(LootCrateTagNames.RegisteredPlayerUUIDs.toString(), registeredPlayers);
            }
        }
        this.serializeLootTable(tag);

        return tag;
    }

    public void setLootCrateBlockTags(NbtCompound tag) {
        this.registeredPlayerUUIDs = new ArrayList<>();

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

        if(tag.contains(LootCrateTagNames.Locked.toString()) && tag.getBoolean(LootCrateTagNames.Locked.toString())) {
            this.locked = true;
            this.doNotConsumeKeyOnUnlock = tag.contains(LootCrateTagNames.DoNotConsumeKeyOnUnlock.toString()) && tag.getBoolean(LootCrateTagNames.DoNotConsumeKeyOnUnlock.toString());
        } else {
            this.locked = false;
            this.doNotConsumeKeyOnUnlock = false;
        }
        if(tag.contains(LootCrateTagNames.Trapped.toString()) && tag.getBoolean(LootCrateTagNames.Trapped.toString())) {
            this.trapped = true;
        }

        if(tag.contains(LootCrateTagNames.OncePerPlayer.toString()) && tag.getBoolean(LootCrateTagNames.OncePerPlayer.toString())) {
            this.oncePerPlayer = true;
            if(tag.contains(LootCrateTagNames.RegisteredPlayerUUIDs.toString())) {
                NbtList playerUUIDs = tag.getList(LootCrateTagNames.RegisteredPlayerUUIDs.toString(), 11);
                for (NbtElement playerUUID : playerUUIDs) {
                    this.registeredPlayerUUIDs.add(NbtHelper.toUuid(playerUUID));
                }
            }
        } else {
            this.oncePerPlayer = false;
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

    public boolean isLocked() {
        return this.locked;
    }

    public boolean isTrapped() {
        return this.trapped;
    }

    public boolean doesUnlock(Item item) {
        if(world == null) {
            return false;
        }
        Block block = getBlock();

        if(item instanceof LootKeyItem) {
            LootCrateRarity itemRarity = LootCrateAtlas.getKeyRarity((LootKeyItem) item);
            LootCrateRarity blockRarity = LootCrateBlock.getCrateRarity(block);
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
        this.playSound(LootCrates.CHEST_UNLOCKS_SOUND_EVENT);
    }

    public LootCrateBlock getBlock() {
        return (LootCrateBlock) this.world.getBlockState(this.pos).getBlock();
    }

    public ScheduledTickEvent getRandomTickEvent() {
        if(this.scheduledTickEvent == null) {
            scheduledTickEvent = LootCrateAtlas.getRandomTickEvent(getBlock());
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
        SoundEvent customSoundEvent = LootCrateAtlas.getCustomOpenSoundEvent(getBlock());
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
        SoundEvent customSoundEvent = LootCrateAtlas.getCustomCloseSoundEvent(getBlock());
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

    public void setOncePerPlayer(boolean oncePerPlayer) {
        this.oncePerPlayer = oncePerPlayer;
    }

    public void setReplenishTimeTicks(int ticks) {
        this.replenishTimeTicks = ticks;
    }

    public void setTrapped(boolean trapped) {
        this.trapped = trapped;
    }

    public abstract int getCurrentLookingPlayers(BlockView world, BlockPos pos);

}
