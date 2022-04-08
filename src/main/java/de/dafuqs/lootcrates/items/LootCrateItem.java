package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.LootCrateAtlas;
import de.dafuqs.lootcrates.blocks.PlayerCrateData;
import de.dafuqs.lootcrates.blocks.modes.InventoryDeletionMode;
import de.dafuqs.lootcrates.blocks.modes.LockMode;
import de.dafuqs.lootcrates.blocks.modes.ReplenishMode;
import de.dafuqs.lootcrates.enums.LootCrateRarity;
import de.dafuqs.lootcrates.enums.LootCrateTagNames;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LootCrateItem extends BlockItem {

    public LootCrateItem(Block block, Settings settings) {
        super(block, settings);
    }
    
    @Override
    public boolean canBeNested() {
        return false;
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        super.appendTooltip(itemStack, world, tooltip, tooltipContext);

        NbtCompound compound = itemStack.getSubNbt("BlockEntityTag");
        if (compound != null) {
            
            boolean trackedPerPlayer = isTrackedPerPlayer(compound);
            Optional<PlayerCrateData> playerCrateData = getPlayerCrateData(compound, trackedPerPlayer);
    
            ReplenishMode replenishMode = getReplenishMode(compound);
            long replenishTimeTicks = getReplenishTimeTicks(compound);
    
            if(playerCrateData.isEmpty() || playerCrateData.get().replenishTime < 0 || replenishMode.canReplenish(world, playerCrateData, replenishTimeTicks)) {
                tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.contains_loot"));
            } else {
                if(trackedPerPlayer) {
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.already_looted_by_you"));
                } else {
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.already_looted"));
                }
            }
    
            LockMode lockMode = getLockMode(compound);
            if(!lockMode.isUnlocked(playerCrateData)) {
                tooltip.add(LootCrateAtlas.getItemLockedTooltip(itemStack, lockMode));
            }

            switch (replenishMode) {
                case HOURLY -> {
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_hourly"));
                }
                case DAILY -> {
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_daily"));
                }
                case WEEKLY -> {
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_weekly"));
                }
                case MONTHLY -> {
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_monthly"));
                }
                case GAME_TIME -> {
                    Text text = getReplenishTimeGameTimeHumanReadableText(replenishTimeTicks);
                    if(text != null) {
                        tooltip.add(text);
                    }
                }
                case REAL_TIME -> {
                    Text text = getReplenishTimeRealTimeHumanReadableText(replenishTimeTicks);
                    if(text != null) {
                        tooltip.add(text);
                    }
                }
            }
            if(trackedPerPlayer) {
                tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.tracked_per_player"));
            }
            if(lockMode.relocks()) {
                tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.relocks"));
            }
    
            InventoryDeletionMode inventoryDeletionMode = getInventoryDeletionMode(compound);
            switch (inventoryDeletionMode) {
                case ON_OPEN -> {
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.inventory_deletes_on_open"));
                }
                case WHEN_REPLENISHED -> {
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.inventory_deletes_when_replenished"));
                }
            }
    
            boolean trapped = isTrapped(compound);
            if(trapped) {
                tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.trapped"));
            }
    
            // loot table and seed
            boolean advanced = tooltipContext.isAdvanced();
            if(advanced) {
                if (compound.contains("LootTable")) {
                    String lootTableText = compound.getString("LootTable");
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.loot_table", lootTableText));
                }
                if (compound.contains("LootTableSeed")) {
                    long lootTableSeed = compound.getLong("LootTableSeed");
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.fixed_seed", lootTableSeed));
                }
            }

            // inventory
            if(!lockMode.isUnlocked(playerCrateData)) {
                if (compound.contains("Items", 9)) {
                    DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(27, ItemStack.EMPTY);
                    Inventories.readNbt(compound, defaultedList);
                    int i = 0;
                    int j = 0;

                    for (ItemStack is : defaultedList) {
                        if (!is.isEmpty()) {
                            ++j;
                            if (i <= 4) {
                                ++i;
                                MutableText mutableText = is.getName().shallowCopy();
                                mutableText.append(" x").append(String.valueOf(is.getCount()));
                                tooltip.add(mutableText);
                            }
                        }
                    }

                    if (j - i > 0) {
                        tooltip.add((new TranslatableText("container.shulkerBox.more", j - i)).formatted(Formatting.ITALIC));
                    }
                }
            }

        } else {
            tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.no_data_set"));
        }
    }
    
    public static boolean isTrapped(@NotNull NbtCompound compound) {
        return compound.contains(LootCrateTagNames.Trapped.toString()) && compound.getBoolean(LootCrateTagNames.Trapped.toString());
    }
    
    @NotNull
    public static InventoryDeletionMode getInventoryDeletionMode(@NotNull NbtCompound compound) {
        InventoryDeletionMode inventoryDeletionMode = InventoryDeletionMode.NEVER;
        if (compound.contains(LootCrateTagNames.InventoryDeletionMode.toString())) {
            try {
                inventoryDeletionMode = InventoryDeletionMode.valueOf(compound.getString(LootCrateTagNames.InventoryDeletionMode.toString()));
            } catch (IllegalArgumentException ignored) { } // nonexistant value
        }
        return inventoryDeletionMode;
    }
    
    @NotNull
    public static LockMode getLockMode(@NotNull NbtCompound compound) {
        LockMode lockMode = LockMode.NONE; // DONE
        if (compound.contains(LootCrateTagNames.LockMode.toString())) {
            try {
                lockMode = LockMode.valueOf(compound.getString(LootCrateTagNames.LockMode.toString()));
            } catch (IllegalArgumentException ignored) { } // nonexistant value
        }
        return lockMode;
    }
    
    @NotNull
    public static ReplenishMode getReplenishMode(@NotNull NbtCompound compound) {
        ReplenishMode replenishMode = ReplenishMode.NEVER;
        if (compound.contains(LootCrateTagNames.ReplenishMode.toString())) {
            try {
                replenishMode = ReplenishMode.valueOf(compound.getString(LootCrateTagNames.ReplenishMode.toString()));
            } catch (IllegalArgumentException ignored) { } // nonexistant value
        }
        return replenishMode;
    }
    
    public static boolean isTrackedPerPlayer(@NotNull NbtCompound compound) {
        return compound.contains(LootCrateTagNames.TrackedPerPlayer.toString()) && compound.getBoolean(LootCrateTagNames.TrackedPerPlayer.toString());
    }
    
    public static Optional<PlayerCrateData> getPlayerCrateData(@NotNull NbtCompound compound, boolean trackedPerPlayer) {
        if(trackedPerPlayer) {
            return PlayerCrateData.getPlayerSpecificCrateData(compound, MinecraftClient.getInstance().player.getUuid());
        } else {
            if(compound.contains("Data")) {
                return Optional.of(PlayerCrateData.fromCompoundWithoutUUID(compound.getCompound("Data")));
            }
        }
        return Optional.empty();
    }
    
    public static long getReplenishTimeTicks(@NotNull NbtCompound compound) {
        if (compound.contains(LootCrateTagNames.ReplenishTimeTicks.toString())) {
            return compound.getLong(LootCrateTagNames.ReplenishTimeTicks.toString());
        }
        return -1;
    }
    
    public static void lockForPlayer(ItemStack itemStack, PlayerEntity player, boolean trackedPerPlayer) {
        setUnlockTime(itemStack, player, trackedPerPlayer, -1);
    }
    
    public static void unlockForPlayer(ItemStack itemStack, PlayerEntity player, boolean trackedPerPlayer, @NotNull ReplenishMode replenishMode) {
        long time = replenishMode.usesRealTime ? ZonedDateTime.now().toInstant().toEpochMilli() : player.world.getTime();
        setUnlockTime(itemStack, player, trackedPerPlayer, time);
    }
    
    private static void setUnlockTime(@NotNull ItemStack itemStack, PlayerEntity player, boolean trackedPerPlayer, long unlockTime) {
        NbtCompound nbtCompound = itemStack.getNbt();
        if(nbtCompound.contains("BlockEntityTag")) {
            NbtCompound blockEntityTag = nbtCompound.getCompound("BlockEntityTag");
            if(blockEntityTag != null) {
                if(trackedPerPlayer) {
                    UUID uuid = player.getUuid();
                    
                    NbtCompound playerDataCompound;
                    if(blockEntityTag.contains("PlayerData", NbtElement.COMPOUND_TYPE)) {
                        playerDataCompound = blockEntityTag.getCompound("PlayerData");
                    } else {
                        playerDataCompound = new NbtCompound();
                        nbtCompound.putUuid("UUID", uuid);
                        nbtCompound.putLong("ReplenishTime", -1);
                    }
                    playerDataCompound.putLong("UnlockTime", unlockTime);
                    
                    blockEntityTag.put("PlayerData", playerDataCompound);
                } else {
                    NbtCompound dataCompound;
                    if(blockEntityTag.contains("Data", NbtElement.COMPOUND_TYPE)) {
                        dataCompound = blockEntityTag.getCompound("Data");
                    } else {
                        dataCompound = new NbtCompound();
                        nbtCompound.putLong("ReplenishTime", -1);
                    }
                    dataCompound.putLong("UnlockTime", unlockTime);
                    
                    blockEntityTag.put("Data", dataCompound);
                }
                blockEntityTag.put("BlockEntityTag", blockEntityTag);
                itemStack.setNbt(blockEntityTag);
            }
        }
    }
    
    public static void setReplenishedForPlayer(ItemStack itemStack, PlayerEntity player, boolean trackedPerPlayer, @NotNull ReplenishMode replenishMode) {
        long time = replenishMode.usesRealTime ? ZonedDateTime.now().toInstant().toEpochMilli() : player.world.getTime();
        setReplenishTime(itemStack, player, trackedPerPlayer, time);
    }
    
    public static void setReplenishTime(@NotNull ItemStack itemStack, PlayerEntity player, boolean trackedPerPlayer, long replenishTime) {
        NbtCompound nbtCompound = itemStack.getNbt();
        if(nbtCompound.contains("BlockEntityTag")) {
            NbtCompound blockEntityTag = nbtCompound.getCompound("BlockEntityTag");
            if(blockEntityTag != null) {
                if(trackedPerPlayer) {
                    UUID uuid = player.getUuid();
                
                    NbtCompound playerDataCompound;
                    if(blockEntityTag.contains("PlayerData", NbtElement.COMPOUND_TYPE)) {
                        playerDataCompound = blockEntityTag.getCompound("PlayerData");
                    } else {
                        playerDataCompound = new NbtCompound();
                        nbtCompound.putUuid("UUID", uuid);
                        nbtCompound.putLong("UnlockTime", -1);
                    }
                    playerDataCompound.putLong("ReplenishTime", replenishTime);
                
                    blockEntityTag.put("PlayerData", playerDataCompound);
                } else {
                    NbtCompound dataCompound;
                    if(blockEntityTag.contains("Data", NbtElement.COMPOUND_TYPE)) {
                        dataCompound = blockEntityTag.getCompound("Data");
                    } else {
                        dataCompound = new NbtCompound();
                        nbtCompound.putLong("UnlockTime", -1);
                    }
                    dataCompound.putLong("ReplenishTime", replenishTime);
                
                    blockEntityTag.put("Data", dataCompound);
                }
                blockEntityTag.put("BlockEntityTag", blockEntityTag);
                itemStack.setNbt(blockEntityTag);
            }
        }
    }
    
    public static boolean consumeKey(@NotNull PlayerEntity player, @NotNull ItemStack itemStack) {
        LootCrateRarity lootCrateRarity = LootCrateAtlas.getCrateItemRarity(itemStack.getItem());
        if(player.isCreative()) {
            return true;
        } else {
            ItemStack lootKeyItemStack = new ItemStack(LootCrateAtlas.getLootKeyItem(lootCrateRarity));
            if (player.getInventory().contains(lootKeyItemStack)) {
                int slot = player.getInventory().getSlotWithStack(lootKeyItemStack);
                player.getInventory().getStack(slot).decrement(1);
                return true;
            }
        }
        return false;
    }
    
    public static @Nullable TranslatableText getReplenishTimeGameTimeHumanReadableText(long replenishTime) {
        if(replenishTime >= 1728000) { // 1 day
            return new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_game_time_days", replenishTime / 1728000F);
        } else if(replenishTime >= 72000) { // 1 hour
            return new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_game_time_hours", replenishTime / 72000F);
        } else if(replenishTime >= 1200) { // 1 minute
            return new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_game_time_minutes", replenishTime / 1200F);
        } else if(replenishTime <= 0) {
            // does not replenish
            return null;
        } else { // in ticks
            return new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_game_time_ticks", replenishTime);
        }
    }
    
    public static @Nullable TranslatableText getReplenishTimeRealTimeHumanReadableText(long replenishTime) {
        if(replenishTime >= 1728000) { // 1 day
            return new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_real_time_days", replenishTime / 1728000F);
        } else if(replenishTime >= 72000) { // 1 hour
            return new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_real_time_hours", replenishTime / 72000F);
        } else if(replenishTime >= 1200) { // 1 minute
            return new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_real_time_minutes", replenishTime / 1200F);
        } else if(replenishTime <= 0) {
            // does not replenish
            return null;
        } else { // in ticks
            return new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_real_time_ticks", replenishTime);
        }
    }

    public static @NotNull NbtCompound getLootCrateItemCompoundTag(@NotNull Identifier lootTable, LockMode lockMode, ReplenishMode replenishMode, InventoryDeletionMode inventoryDeletionMode, long lootGenerationTimeInTicks, boolean trackedPerPlayer, boolean trapped) {
        NbtCompound compoundTag = new NbtCompound();
        NbtCompound blockEntityTag = new NbtCompound();

        blockEntityTag.putString("LootTable", lootTable.toString());
        
        if(lootGenerationTimeInTicks > 0) {
            blockEntityTag.putLong(LootCrateTagNames.ReplenishTimeTicks.toString(), lootGenerationTimeInTicks);
        }
        blockEntityTag.putBoolean(LootCrateTagNames.TrackedPerPlayer.toString(), trackedPerPlayer);
        blockEntityTag.putString(LootCrateTagNames.LockMode.toString(), lockMode.toString());
        blockEntityTag.putString(LootCrateTagNames.ReplenishMode.toString(), replenishMode.toString());
        blockEntityTag.putString(LootCrateTagNames.InventoryDeletionMode.toString(), inventoryDeletionMode.toString());
        blockEntityTag.putBoolean(LootCrateTagNames.Trapped.toString(), trapped);

        compoundTag.put("BlockEntityTag", blockEntityTag);
        return compoundTag;
    }
    
}
