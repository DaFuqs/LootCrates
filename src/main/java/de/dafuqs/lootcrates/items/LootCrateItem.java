package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.LootCrateAtlas;
import de.dafuqs.lootcrates.blocks.PlayerCrateData;
import de.dafuqs.lootcrates.blocks.modes.InventoryDeletionMode;
import de.dafuqs.lootcrates.blocks.modes.LockMode;
import de.dafuqs.lootcrates.blocks.modes.ReplenishMode;
import de.dafuqs.lootcrates.enums.LootCrateTagNames;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
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
            
            boolean trackedPerPlayer = compound.contains(LootCrateTagNames.TrackedPerPlayer.toString()) && compound.getBoolean(LootCrateTagNames.TrackedPerPlayer.toString());
            Optional<PlayerCrateData> playerCrateData = Optional.empty();
            if(trackedPerPlayer) {
                playerCrateData = PlayerCrateData.getPlayerSpecificCrateData(compound, MinecraftClient.getInstance().player.getUuid());
            } else {
                if(compound.contains("Data")) {
                    playerCrateData = Optional.of(PlayerCrateData.fromCompoundWithoutUUID(compound.getCompound("Data")));
                }
            }
            
            ReplenishMode replenishMode = ReplenishMode.NEVER;
            if (compound.contains(LootCrateTagNames.ReplenishMode.toString())) {
                try {
                    replenishMode = ReplenishMode.valueOf(compound.getString(LootCrateTagNames.ReplenishMode.toString()));
                } catch (IllegalArgumentException ignored) { } // nonexistant value
            }
    
            long replenishTimeTicks = -1;
            if (compound.contains(LootCrateTagNames.ReplenishTimeTicks.toString())) {
                replenishTimeTicks = compound.getLong(LootCrateTagNames.ReplenishTimeTicks.toString());
            }
            
            if(playerCrateData.isEmpty() || playerCrateData.get().replenishTime < 0 || replenishMode.canReplenish(world, playerCrateData, replenishTimeTicks)) {
                tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.contains_loot"));
            } else {
                if(trackedPerPlayer) {
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.already_looted_by_you"));
                } else {
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.already_looted"));
                }
            }
    
            LockMode lockMode = LockMode.NONE; // DONE
            if (compound.contains(LootCrateTagNames.LockMode.toString())) {
                try {
                    lockMode = LockMode.valueOf(compound.getString(LootCrateTagNames.LockMode.toString()));
                } catch (IllegalArgumentException ignored) { } // nonexistant value
            }
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
            if(replenishMode != ReplenishMode.NEVER && trackedPerPlayer) {
                tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.tracked_per_player"));
            }
    
            if(lockMode.relocks()) {
                tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.relocks"));
            }
    
            InventoryDeletionMode inventoryDeletionMode = InventoryDeletionMode.NEVER;
            if (compound.contains(LootCrateTagNames.InventoryDeletionMode.toString())) {
                try {
                    inventoryDeletionMode = InventoryDeletionMode.valueOf(compound.getString(LootCrateTagNames.InventoryDeletionMode.toString()));
                } catch (IllegalArgumentException ignored) { } // nonexistant value
            }
            switch (inventoryDeletionMode) {
                case ON_OPEN -> {
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.inventory_deletes_on_open"));
                }
                case WHEN_REPLENISHED -> {
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.inventory_deletes_when_replenished"));
                }
            }
    
            boolean trapped = compound.contains(LootCrateTagNames.Trapped.toString()) && compound.getBoolean(LootCrateTagNames.Trapped.toString());
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
    
    private @Nullable TranslatableText getReplenishTimeGameTimeHumanReadableText(long replenishTime) {
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
    
    private @Nullable TranslatableText getReplenishTimeRealTimeHumanReadableText(long replenishTime) {
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
