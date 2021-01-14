package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.enums.LootCrateTagNames;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
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
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;

public class LootCrateItem extends BlockItem {

    public LootCrateItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        super.appendTooltip(itemStack, world, tooltip, tooltipContext);

        CompoundTag compoundTag = itemStack.getSubTag("BlockEntityTag");
        if (compoundTag != null) {
            if (compoundTag.contains(LootCrateTagNames.Locked.toString()) && compoundTag.getBoolean(LootCrateTagNames.Locked.toString())) {
                if (compoundTag.contains(LootCrateTagNames.DoNotConsumeKeyOnUnlock.toString()) && compoundTag.getBoolean(LootCrateTagNames.DoNotConsumeKeyOnUnlock.toString())) {
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.locked_use"));
                } else {
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.locked_consume"));
                }
            } else {
                tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.unlocked"));
            }

            boolean advanced = tooltipContext.isAdvanced();
            if(advanced) {
                if (compoundTag.contains("LootTable")) {
                    String lootTableText = compoundTag.getString("LootTable");
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.loot_table", lootTableText));
                }
                if (compoundTag.contains(LootCrateTagNames.ReplenishTimeTicks.toString())) {
                    long replenishTimeTicks = compoundTag.getLong(LootCrateTagNames.ReplenishTimeTicks.toString());
                    tooltip.add(getReplenishTimeHumanReadableText(replenishTimeTicks));
                }
                if (compoundTag.contains("LootTableSeed")) {
                    long lootTableSeed = compoundTag.getLong("LootTableSeed");
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.fixed_seed", lootTableSeed));
                }
                if (compoundTag.contains(LootCrateTagNames.OncePerPlayer.toString()) && compoundTag.getBoolean(LootCrateTagNames.OncePerPlayer.toString())) {
                    if(compoundTag.contains(LootCrateTagNames.RegisteredPlayerUUIDs.toString())) {
                        ListTag playerUUIDsTag = compoundTag.getList(LootCrateTagNames.RegisteredPlayerUUIDs.toString(), 11);
                        int playerCount = playerUUIDsTag.size();
                        tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.once_per_player_with_count", playerCount));
                    } else {
                        tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.once_per_player"));
                    }
                }
            }

            if (compoundTag.contains("Items", 9)) {
                DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(27, ItemStack.EMPTY);
                Inventories.fromTag(compoundTag, defaultedList);
                int i = 0;
                int j = 0;
                Iterator var9 = defaultedList.iterator();

                while(var9.hasNext()) {
                    ItemStack is = (ItemStack)var9.next();
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
                    tooltip.add((new TranslatableText("container.shulkerBox.more", new Object[]{j - i})).formatted(Formatting.ITALIC));
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

}
