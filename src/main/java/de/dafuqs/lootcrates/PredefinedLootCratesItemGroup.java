package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.enums.LootCrateRarity;
import de.dafuqs.lootcrates.items.LootCrateItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class PredefinedLootCratesItemGroup extends ItemGroup {

    public PredefinedLootCratesItemGroup() {
        super(LootCrates.PREDEFINED_CRATES_GROUP.getIndex(), LootCrates.PREDEFINED_CRATES_GROUP.getName());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public ItemStack createIcon() {
        return new ItemStack(LootCrateAtlas.getShulkerCrate(LootCrateRarity.RARE));
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

        ArrayList<Long> replenishTimeTicksValues = new ArrayList<>();
        replenishTimeTicksValues.add(-1L);      // once
        replenishTimeTicksValues.add(1L);       // 1 tick
        replenishTimeTicksValues.add(1728000L); // 1 day

        ArrayList<Boolean> booleans = new ArrayList<>() {{
            add(true);
            add(false);
        }};

        List<Item> allLootCrates = LootCrateAtlas.getAllCrateItems();
        Set<Identifier> allLootTables = LootTables.getAll();

        for(Item lootCrateItem : allLootCrates) {
            for (Identifier lootTable : allLootTables) {
                for (Long replenishTimeTicks : replenishTimeTicksValues) {
                    for (boolean locked : booleans) {
                        for (boolean trapped : booleans) {
                            for (boolean doNotConsumeKeyOnUnlock : booleans) {
                                for (boolean oncePerPlayer : booleans) {
                                    if (oncePerPlayer && replenishTimeTicks < 0) {
                                        // oncePerPlayer really is only useful when replenish time is positive
                                    } else {
                                        if (doNotConsumeKeyOnUnlock && !locked) {
                                            // no use in that tag when there is no lock, is there?
                                        } else {
                                            NbtCompound compound = LootCrateItem.getLootCrateItemCompoundTag(lootTable, locked, doNotConsumeKeyOnUnlock, replenishTimeTicks, 0, oncePerPlayer, trapped);
                                            ItemStack itemStack = new ItemStack(lootCrateItem);
                                            itemStack.setNbt(compound);
                                            stacks.add(itemStack);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return stacks;
    }

}