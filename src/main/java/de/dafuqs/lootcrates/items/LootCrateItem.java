package de.dafuqs.lootcrates.items;

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
                tooltip.add(new LiteralText("Name: " + name));
            }
            if (compoundTag.contains("Locked") && compoundTag.getBoolean("Locked")) {
                if (compoundTag.contains("DoNotConsumeKeyOnUnlock") && compoundTag.getBoolean("DoNotConsumeKeyOnUnlock")) {
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
                if (compoundTag.contains("LootGenerationTimeInTicks")) {
                    long lootGenerationTimeInTicks = compoundTag.getLong("LootGenerationTimeInTicks");
                    tooltip.add(new LiteralText("Generates loot every " + lootGenerationTimeInTicks + " ticks"));
                }
                if (compoundTag.contains("LootTableSeed")) {
                    long lootTableSeed = compoundTag.getLong("LootTableSeed");
                    tooltip.add(new LiteralText("Fixed seed: " + lootTableSeed));
                }
                if (compoundTag.contains("OncePerPlayer") && compoundTag.getBoolean("OncePerPlayer")) {
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
            blockEntityTag.putBoolean("Locked", true);
            if(doNotConsumeKeyOnUnlock) {
                blockEntityTag.putBoolean("DoNotConsumeKeyOnUnlock", true);
            }
        }

        if(lootGenerationTimeInTicks > 0) {
            blockEntityTag.putLong("LootGenerationTimeInTicks", lootGenerationTimeInTicks);
        }
        if(lootTableSeed != 0) {
            blockEntityTag.putLong("LootTableSeed", lootTableSeed);
        }
        if(oncePerPlayer) {
            blockEntityTag.putBoolean("OncePerPlayer", true);
        }

        compoundTag.put("BlockEntityTag", blockEntityTag);
        return compoundTag;
    }

}
