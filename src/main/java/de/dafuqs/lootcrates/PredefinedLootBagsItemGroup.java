package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.enums.LootCrateRarity;
import de.dafuqs.lootcrates.items.LootBagItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class PredefinedLootBagsItemGroup extends ItemGroup {

    public PredefinedLootBagsItemGroup() {
        super(LootCrates.PREDEFINED_BAGS_GROUP.getIndex(), LootCrates.PREDEFINED_BAGS_GROUP.getName());
    }

    @Contract(" -> new")
    @Environment(EnvType.CLIENT)
    @Override
    public @NotNull ItemStack createIcon() {
        return new ItemStack(LootCrateAtlas.getLootBagItem(LootCrateRarity.EPIC));
    }

    @Environment(EnvType.CLIENT)
    public void appendStacks(@NotNull DefaultedList<ItemStack> stacks) {
        ArrayList<ItemStack> predefined = getPredefinedLootBags();
        stacks.addAll(predefined);
    }

    /**
     * Generates a default item for a lot of predefined values of itemStacks
     * @return All generated ItemStacks
     */
    private @NotNull ArrayList<ItemStack> getPredefinedLootBags() {
        ArrayList<ItemStack> stacks = new ArrayList<>();

        List<Item> allLootBags = LootCrateAtlas.getAllLootBagItems();
        Set<Identifier> allLootTables = LootTables.getAll();

        for(Item lootBagItem : allLootBags) {
            for (Identifier lootTable : allLootTables) {
                if(!lootTable.getPath().contains("entities")) { // to reduce the lists size a bit
                    NbtCompound compound = LootBagItem.getItemCompoundTag(lootTable, 0);
                    ItemStack itemStack = new ItemStack(lootBagItem);
                    itemStack.setNbt(compound);
                    stacks.add(itemStack);
                }
            }
        }

        return stacks;
    }

}