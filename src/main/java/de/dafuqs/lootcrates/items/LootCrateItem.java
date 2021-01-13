package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.blocks.LootCrateTags;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
            if (compoundTag.contains("CustomName")) {
                String name = compoundTag.getString("CustomName");
                tooltip.add(new LiteralText("CustomName: " + name));
            }
            if (compoundTag.contains(LootCrateTags.TagNames.Locked.toString()) && compoundTag.getBoolean(LootCrateTags.TagNames.Locked.toString())) {
                if (compoundTag.contains(LootCrateTags.TagNames.DoNotConsumeKeyOnUnlock.toString()) && compoundTag.getBoolean(LootCrateTags.TagNames.DoNotConsumeKeyOnUnlock.toString())) {
                    tooltip.add(new LiteralText("Locked (unlock it with a key of same rarity)"));
                } else {
                    tooltip.add(new LiteralText("Locked (unlocking will use up a key of the same rarity)"));
                }
            } else {
                tooltip.add(new LiteralText("Unlocked"));
            }

            boolean advanced = tooltipContext.isAdvanced();
            if(advanced) {
                if (compoundTag.contains("LootTable")) {
                    String lootTableText = compoundTag.getString("LootTable");
                    tooltip.add(new LiteralText("Loot Table: " + lootTableText));
                }
                if (compoundTag.contains(LootCrateTags.TagNames.ReplenishTimeTicks.toString())) {
                    long lootGenerationTimeInTicks = compoundTag.getLong(LootCrateTags.TagNames.ReplenishTimeTicks.toString());
                    tooltip.add(new LiteralText("Generates loot every " + lootGenerationTimeInTicks + " ticks"));
                }
                if (compoundTag.contains("LootTableSeed")) {
                    long lootTableSeed = compoundTag.getLong("LootTableSeed");
                    tooltip.add(new LiteralText("Fixed seed: " + lootTableSeed));
                }
                if (compoundTag.contains(LootCrateTags.TagNames.OncePerPlayer.toString()) && compoundTag.getBoolean(LootCrateTags.TagNames.OncePerPlayer.toString())) {
                    tooltip.add(new LiteralText("Once per player"));
                }
            }
        } else {
            tooltip.add(new LiteralText("No data set"));
        }
    }

    public static CompoundTag getLootCrateItemCompoundTag(Identifier lootTable, boolean locked, boolean doNotConsumeKeyOnUnlock, long lootGenerationTimeInTicks, long lootTableSeed, boolean oncePerPlayer) {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag blockEntityTag = new CompoundTag();

        blockEntityTag.putString("LootTable", lootTable.toString());
        if(locked) {
            blockEntityTag.putBoolean(LootCrateTags.TagNames.Locked.toString(), true);
            if(doNotConsumeKeyOnUnlock) {
                blockEntityTag.putBoolean(LootCrateTags.TagNames.DoNotConsumeKeyOnUnlock.toString(), true);
            }
        }

        if(lootGenerationTimeInTicks > 0) {
            blockEntityTag.putLong(LootCrateTags.TagNames.ReplenishTimeTicks.toString(), lootGenerationTimeInTicks);
        }
        if(lootTableSeed != 0) {
            blockEntityTag.putLong("LootTableSeed", lootTableSeed);
        }
        if(oncePerPlayer) {
            blockEntityTag.putBoolean(LootCrateTags.TagNames.OncePerPlayer.toString(), true);
        }

        compoundTag.put("BlockEntityTag", blockEntityTag);
        return compoundTag;
    }

}
