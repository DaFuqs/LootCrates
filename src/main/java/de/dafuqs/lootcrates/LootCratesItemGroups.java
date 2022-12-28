package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.blocks.modes.InventoryDeletionMode;
import de.dafuqs.lootcrates.blocks.modes.LockMode;
import de.dafuqs.lootcrates.blocks.modes.ReplenishMode;
import de.dafuqs.lootcrates.enums.LootCrateRarity;
import de.dafuqs.lootcrates.items.LootBagItem;
import de.dafuqs.lootcrates.items.LootCrateItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class LootCratesItemGroups {
	
	public static final ItemGroup ITEM_GROUP = FabricItemGroup.builder(new Identifier(LootCrates.MOD_ID, "loot_crates"))
			.displayName(Text.translatable("itemGroup.lootcrates.loot_crates"))
			.icon(() -> new ItemStack(LootCrateAtlas.getLootCrate(LootCrateRarity.COMMON)))
			.entries((enabledFeatures, entries, operatorEnabled) -> {
				for(LootCrateRarity rarity : LootCrateRarity.values()) {
					entries.add(LootCrateAtlas.getLootCrate(rarity));
				}
				for(LootCrateRarity rarity : LootCrateRarity.values()) {
					entries.add(LootCrateAtlas.getLootBagItem(rarity));
				}
			})
			.build();
	
	public static final ItemGroup PREDEFINED_CRATES_GROUP = FabricItemGroup.builder(new Identifier(LootCrates.MOD_ID, "predefined_loot_crates"))
			.displayName(Text.translatable("itemGroup.lootcrates.predefined_loot_crates"))
			.icon(() -> new ItemStack(LootCrateAtlas.getLootCrate(LootCrateRarity.EPIC)))
			.entries((enabledFeatures, entries, operatorEnabled) -> {
				ArrayList<Long> replenishTimeTicksValues = new ArrayList<>();
				replenishTimeTicksValues.add(1L);       // 1 tick
				replenishTimeTicksValues.add(72000L);   // 1 hour
				replenishTimeTicksValues.add(1728000L); // 1 day
				
				ArrayList<Boolean> booleans = new ArrayList<>() {{
					add(false);
					add(true);
				}};
				
				Item lootCrateItem = LootCrateAtlas.getAllCrateItems().get(0);
				for (Identifier lootTable : LootTables.getAll()) {
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
											entries.add(itemStack);
										}
									} else {
										NbtCompound compound = LootCrateItem.getLootCrateItemCompoundTag(lootTable, lockMode, replenishMode, InventoryDeletionMode.NEVER, 0, trackedPerPlayer, false);
										ItemStack itemStack = new ItemStack(lootCrateItem);
										itemStack.setNbt(compound);
										entries.add(itemStack);
									}
								}
							}
						}
					}
				}
			}).build();
	
	public static final ItemGroup PREDEFINED_BAGS_GROUP = FabricItemGroup.builder(new Identifier(LootCrates.MOD_ID, "predefined_loot_bags"))
			.displayName(Text.translatable("itemGroup.lootcrates.predefined_loot_bags"))
			.icon(() -> new ItemStack(LootCrateAtlas.getLootBagItem(LootCrateRarity.EPIC)))
			.entries((enabledFeatures, entries, operatorEnabled) -> {
				Item lootBagItem = LootCrateAtlas.getAllLootBagItems().get(0);
				
				for (Identifier lootTable : LootTables.getAll()) {
					if(lootTable.getNamespace().equals("minecraft") && lootTable.getPath().startsWith("chests/")) { // to reduce the lists size
						NbtCompound compound = LootBagItem.getItemCompoundTag(lootTable, 0);
						ItemStack itemStack = new ItemStack(lootBagItem);
						itemStack.setNbt(compound);
						entries.add(itemStack);
					}
				}
			}).build();
	
	public static void register() {
	
	}
	
}
