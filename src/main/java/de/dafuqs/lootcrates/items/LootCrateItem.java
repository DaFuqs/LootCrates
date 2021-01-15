package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.LootCratesBlocks;
import de.dafuqs.lootcrates.enums.LootCrateRarity;
import de.dafuqs.lootcrates.enums.LootCrateTagNames;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class LootCrateItem extends BlockItem {

    public LootCrateItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        super.appendTooltip(itemStack, world, tooltip, tooltipContext);

        CompoundTag compoundTag = itemStack.getSubTag("BlockEntityTag");
        if (compoundTag != null) {

            // lock
            boolean locked = false;
            if (compoundTag.contains(LootCrateTagNames.Locked.toString()) && compoundTag.getBoolean(LootCrateTagNames.Locked.toString())) {
                locked = true;
                LootCrateRarity itemRarity = LootCrateItem.getCrateItemRarity((LootCrateItem) (itemStack.getItem()));

                if (compoundTag.contains(LootCrateTagNames.DoNotConsumeKeyOnUnlock.toString()) && compoundTag.getBoolean(LootCrateTagNames.DoNotConsumeKeyOnUnlock.toString())) {
                    switch (itemRarity) {
                        case UNCOMMON:
                            tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.locked_use_uncommon"));
                            break;
                        case RARE:
                            tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.locked_use_rare"));
                            break;
                        case EPIC:
                            tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.locked_use_epic"));
                            break;
                        case GHOST:
                            tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.locked_use_ghost"));
                            break;
                        default:
                            tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.locked_use_common"));
                            break;
                    }
                } else {
                    switch (itemRarity) {
                        case UNCOMMON:
                            tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.locked_consume_uncommon"));
                            break;
                        case RARE:
                            tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.locked_consume_rare"));
                            break;
                        case EPIC:
                            tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.locked_consume_epic"));
                            break;
                        case GHOST:
                            tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.locked_consume_ghost"));
                            break;
                        default:
                            tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.locked_consume_common"));
                            break;
                    }
                }
            }

            boolean advanced = tooltipContext.isAdvanced();
            long replenishTimeTicks = 0;
            if (compoundTag.contains(LootCrateTagNames.ReplenishTimeTicks.toString())) {
                replenishTimeTicks = compoundTag.getLong(LootCrateTagNames.ReplenishTimeTicks.toString());
            }

            boolean oncePerPlayer = compoundTag.contains(LootCrateTagNames.OncePerPlayer.toString()) && compoundTag.getBoolean(LootCrateTagNames.OncePerPlayer.toString());
            boolean wasOpened = compoundTag.contains(LootCrateTagNames.LastReplenishTimeTick.toString()) && compoundTag.getLong(LootCrateTagNames.LastReplenishTimeTick.toString()) != 0;

            if (replenishTimeTicks <= 0 && !oncePerPlayer && wasOpened) {
                // cannot generate more loot
                tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.already_looted"));
            } else {
                tooltip.add(getReplenishTimeHumanReadableText(replenishTimeTicks));

                // oncePerPlayer really is only useful when replenish time is positive
                if (oncePerPlayer && replenishTimeTicks > 0) {
                    if(compoundTag.contains(LootCrateTagNames.RegisteredPlayerUUIDs.toString())) {
                        ListTag playerUUIDsTag = compoundTag.getList(LootCrateTagNames.RegisteredPlayerUUIDs.toString(), 11);
                        int playerCount = playerUUIDsTag.size();
                        tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.once_per_player_with_count", playerCount));
                    } else {
                        tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.once_per_player"));
                    }
                }

                if(advanced) {
                    if (compoundTag.contains("LootTable")) {
                        String lootTableText = compoundTag.getString("LootTable");
                        tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.loot_table", lootTableText));
                    }
                    if (compoundTag.contains("LootTableSeed")) {
                        long lootTableSeed = compoundTag.getLong("LootTableSeed");
                        tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.fixed_seed", lootTableSeed));
                    }
                }

            }

            if(!locked) {
                if (compoundTag.contains("Items", 9)) {
                    DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(27, ItemStack.EMPTY);
                    Inventories.fromTag(compoundTag, defaultedList);
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

    private TranslatableText getReplenishTimeHumanReadableText(long replenishTime) {
        if(replenishTime >= 1728000) { // 1 day
            return new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_time_days", replenishTime / 1728000F);
        } else if(replenishTime >= 72000) { // 1 hour
            return new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_time_hours", replenishTime / 72000F);
        } else if(replenishTime >= 1200) { // 1 minute
            return new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_time_minutes", replenishTime / 1200F);
        } else if(replenishTime <= 0) {
            // does not replenish
            return new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_time_once");
        } else { // in ticks
            return new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_time_ticks", replenishTime);
        }
    }

    public static CompoundTag getLootCrateItemCompoundTag(Identifier lootTable, boolean locked, boolean doNotConsumeKeyOnUnlock, long lootGenerationTimeInTicks, long lootTableSeed, boolean oncePerPlayer) {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag blockEntityTag = new CompoundTag();

        blockEntityTag.putString("LootTable", lootTable.toString());
        if(locked) {
            blockEntityTag.putBoolean(LootCrateTagNames.Locked.toString(), true);
            if(doNotConsumeKeyOnUnlock) {
                blockEntityTag.putBoolean(LootCrateTagNames.DoNotConsumeKeyOnUnlock.toString(), true);
            }
        }

        if(lootGenerationTimeInTicks > 0) {
            blockEntityTag.putLong(LootCrateTagNames.ReplenishTimeTicks.toString(), lootGenerationTimeInTicks);
        }
        if(lootTableSeed != 0) {
            blockEntityTag.putLong("LootTableSeed", lootTableSeed);
        }
        if(oncePerPlayer) {
            blockEntityTag.putBoolean(LootCrateTagNames.OncePerPlayer.toString(), true);
        }

        compoundTag.put("BlockEntityTag", blockEntityTag);
        return compoundTag;
    }

    protected static LootCrateRarity getCrateItemRarity(LootCrateItem item) {
        if (item.equals(LootCratesBlocks.UNCOMMON_CHEST_LOOT_CRATE_ITEM) || item.equals(LootCratesBlocks.UNCOMMON_SHULKER_LOOT_CRATE_ITEM)) {
            return LootCrateRarity.UNCOMMON;
        } else if (item.equals(LootCratesBlocks.RARE_CHEST_LOOT_CRATE_ITEM) || item.equals(LootCratesBlocks.RARE_SHULKER_LOOT_CRATE_ITEM)) {
            return LootCrateRarity.RARE;
        } else if (item.equals(LootCratesBlocks.EPIC_CHEST_LOOT_CRATE_ITEM) || item.equals(LootCratesBlocks.EPIC_SHULKER_LOOT_CRATE_ITEM)) {
            return LootCrateRarity.EPIC;
        } else if (item.equals(LootCratesBlocks.GHOST_CHEST_LOOT_CRATE_ITEM) || item.equals(LootCratesBlocks.GHOST_SHULKER_LOOT_CRATE_ITEM)) {
            return LootCrateRarity.GHOST;
        } else {
            return LootCrateRarity.COMMON;
        }
    }

}
