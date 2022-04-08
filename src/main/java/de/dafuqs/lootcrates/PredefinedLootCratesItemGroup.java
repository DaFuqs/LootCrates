package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.blocks.modes.InventoryDeletionMode;
import de.dafuqs.lootcrates.blocks.modes.LockMode;
import de.dafuqs.lootcrates.blocks.modes.ReplenishMode;
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
        replenishTimeTicksValues.add(1L);       // 1 tick
        replenishTimeTicksValues.add(72000L);   // 1 hour
        replenishTimeTicksValues.add(1728000L); // 1 day
        
        ArrayList<Boolean> booleans = new ArrayList<>() {{
            add(false);
            add(true);
        }};

        Item lootCrateItem = LootCrateAtlas.getAllCrateItems().get(0);
        Set<Identifier> allLootTables = LootTables.getAll();

        for (Identifier lootTable : allLootTables) {
            if(lootTable.getNamespace().equals("minecraft") && lootTable.getPath().startsWith("chests/")) { // to reduce the lists size. These are just examples, after all
                for (LockMode lockMode : LockMode.values()) {
                    for (boolean trackedPerPlayer : booleans) {
                        for (ReplenishMode replenishMode : ReplenishMode.values()) {
                            if(lockMode.relocks() && replenishMode == ReplenishMode.NEVER) {
                                continue; // there is nothing to relock
                            }
                            
                            if(replenishMode.requiresTickData) {
                                for (Long replenishTimeTicks : replenishTimeTicksValues) {
                                    NbtCompound compound = LootCrateItem.getLootCrateItemCompoundTag(lootTable, lockMode, replenishMode, InventoryDeletionMode.NEVER, replenishTimeTicks, trackedPerPlayer, false);
                                    ItemStack itemStack = new ItemStack(lootCrateItem);
                                    itemStack.setNbt(compound);
                                    stacks.add(itemStack);
                                }
                            } else {
                                NbtCompound compound = LootCrateItem.getLootCrateItemCompoundTag(lootTable, lockMode, replenishMode, InventoryDeletionMode.NEVER, 0, trackedPerPlayer, false);
                                ItemStack itemStack = new ItemStack(lootCrateItem);
                                itemStack.setNbt(compound);
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