package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.items.LootCrateItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class PredefinedLootCratesItemGroup extends ItemGroup {

    public PredefinedLootCratesItemGroup() {
        super(LootCrates.PREDEFINED_ITEM_GROUP.getIndex(), LootCrates.PREDEFINED_ITEM_GROUP.getName());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public ItemStack createIcon() {
        return new ItemStack(LootCratesBlocks.EPIC_SHULKER_LOOT_CRATE);
    }

    @Environment(EnvType.CLIENT)
    public void appendStacks(DefaultedList<ItemStack> stacks) {
        ArrayList<ItemStack> predefined = getPredefinedLootCrates();
        stacks.addAll(predefined);
    }

    /**
     * Generates a default item for a lot of predefined values of itemStacks
     * @return All generated ItemStacks
     */
    private ArrayList<ItemStack> getPredefinedLootCrates() {
        ArrayList<ItemStack> stacks = new ArrayList<>();

        ArrayList<Long> lootGenerationTimeInTicksValues = new ArrayList<>();
        lootGenerationTimeInTicksValues.add(-1L);      // once
        lootGenerationTimeInTicksValues.add(20L);      // 1 second
        lootGenerationTimeInTicksValues.add(72000L);   // 1 hour
        lootGenerationTimeInTicksValues.add(1728000L); // 1 day

        ArrayList<Boolean> booleans = new ArrayList<>();
        booleans.add(true);
        booleans.add(false);

        List<Item> allLootCrates = LootCratesBlocks.getLootCrateItems();
        Set<Identifier> allLootTables = LootTables.getAll();

        for(Item lootCrateItem : allLootCrates) {
            for (Identifier lootTable : allLootTables) {
                for (Long lootGenerationTimeInTicks : lootGenerationTimeInTicksValues) {
                    for (boolean locked : booleans) {
                        for (boolean doNotConsumeKeyOnUnlock : booleans) {
                            for (boolean oncePerPlayer : booleans) {
                                CompoundTag compoundTag = LootCrateItem.getLootCrateItemCompoundTag(lootTable, locked, doNotConsumeKeyOnUnlock, lootGenerationTimeInTicks, 0, oncePerPlayer);
                                ItemStack itemStack = new ItemStack(lootCrateItem);
                                itemStack.setTag(compoundTag);
                                stacks.add(itemStack);
                            }
                        }
                    }
                }
            }
        }

        return stacks;
    }

}