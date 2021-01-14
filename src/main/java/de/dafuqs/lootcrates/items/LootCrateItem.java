package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.LootCratesBlocks;
import de.dafuqs.lootcrates.LootCratesItems;
import de.dafuqs.lootcrates.enums.LootCrateTagNames;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
                    long lootGenerationTimeInTicks = compoundTag.getLong(LootCrateTagNames.ReplenishTimeTicks.toString());
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_time", lootGenerationTimeInTicks));
                }
                if (compoundTag.contains("LootTableSeed")) {
                    long lootTableSeed = compoundTag.getLong("LootTableSeed");
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.fixed_seed", lootTableSeed));
                }
                if (compoundTag.contains(LootCrateTagNames.OncePerPlayer.toString()) && compoundTag.getBoolean(LootCrateTagNames.OncePerPlayer.toString())) {
                    tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.once_per_player"));
                }
            }
        } else {
            tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.no_data_set"));
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
