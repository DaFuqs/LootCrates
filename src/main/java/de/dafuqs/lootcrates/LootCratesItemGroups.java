package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.components.*;
import de.dafuqs.lootcrates.enums.*;
import net.fabricmc.fabric.api.itemgroup.v1.*;
import net.minecraft.component.*;
import net.minecraft.component.type.*;
import net.minecraft.item.*;
import net.minecraft.loot.*;
import net.minecraft.registry.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class LootCratesItemGroups {
	
	public static final ItemGroup ITEM_GROUP = FabricItemGroup.builder()
			.displayName(Text.translatable("itemGroup.lootcrates.loot_crates"))
			.icon(() -> new ItemStack(LootCrateAtlas.getLootCrate(LootCrateVariant.COMMON)))
			.entries((displayContext, entries) -> {
				for (LootCrateVariant rarity : LootCrateVariant.values()) {
					entries.add(LootCrateAtlas.getLootCrate(rarity));
					entries.add(LootCrateAtlas.getLootBarrel(rarity));
					entries.add(LootCrateAtlas.getShulkerCrate(rarity));
					entries.add(LootCrateAtlas.getLootKeyItem(rarity));
					entries.add(LootCrateAtlas.getLootBagItem(rarity));
				}
			}).build();
	
	public static @Nullable ItemGroup PREDEFINED_CRATES_GROUP;
	
	public static @Nullable ItemGroup PREDEFINED_BAGS_GROUP;
	
	public static void register(boolean addExampleCratesAndBundlesItemGroups) {
		Registry.register(Registries.ITEM_GROUP, Identifier.of(LootCrates.MOD_ID, "loot_crates"), ITEM_GROUP);
		
		if (addExampleCratesAndBundlesItemGroups) {
			PREDEFINED_CRATES_GROUP = FabricItemGroup.builder()
					.displayName(Text.translatable("itemGroup.lootcrates.predefined_loot_crates"))
					.icon(() -> new ItemStack(LootCrateAtlas.getLootCrate(LootCrateVariant.EPIC)))
					.entries((displayContext, entries) -> entries.addAll(getPredefinedLootCrates())).build();
			
			PREDEFINED_BAGS_GROUP = FabricItemGroup.builder()
					.displayName(Text.translatable("itemGroup.lootcrates.predefined_loot_bags"))
					.icon(() -> new ItemStack(LootCrateAtlas.getLootBagItem(LootCrateVariant.EPIC)))
					.entries((displayContext, entries) -> entries.addAll(getPredefinedLootBags())).build();
			
			Registry.register(Registries.ITEM_GROUP, Identifier.of(LootCrates.MOD_ID, "predefined_loot_crates"), PREDEFINED_CRATES_GROUP);
			Registry.register(Registries.ITEM_GROUP, Identifier.of(LootCrates.MOD_ID, "predefined_loot_bags"), PREDEFINED_BAGS_GROUP);
		}
		
	}
	
	/**
	 * Generates a default item for a lot of predefined values of itemStacks
	 *
	 * @return All generated ItemStacks
	 */
	private static List<ItemStack> getPredefinedLootCrates() {
		List<ItemStack> stacks = new ArrayList<>();
		
		List<Long> replenishTimeTicksValues = new ArrayList<>();
		replenishTimeTicksValues.add(1L);       // 1 tick
		replenishTimeTicksValues.add(72000L);   // 1 hour
		replenishTimeTicksValues.add(1728000L); // 1 day
		
		List<Boolean> booleans = new ArrayList<>() {{
			add(false);
			add(true);
		}};
		
		Item lootCrateItem = LootCrateAtlas.getAllCrateItems().getFirst();
		Set<RegistryKey<LootTable>> allLootTables = LootTables.getAll();
		
		for (RegistryKey<LootTable> lootTableKey : allLootTables) {
			Identifier lootTableId = lootTableKey.getValue();
			if (lootTableId.getNamespace().equals("minecraft") && lootTableId.getPath().startsWith("chests/")) { // to reduce the lists size. These are just examples, after all
				for (LockMode lockMode : LockMode.values()) {
					for (boolean trackedPerPlayer : booleans) {
						for (ReplenishTimeScale replenishTimeScale : ReplenishTimeScale.values()) {
							if (lockMode.relocks() && replenishTimeScale == ReplenishTimeScale.NEVER) {
								continue; // there is nothing to relock
							}
							
							if (replenishTimeScale.requiresTickData) {
								for (Long replenishTimeTicks : replenishTimeTicksValues) {
									ItemStack itemStack = new ItemStack(lootCrateItem);
									LootCrateLootComponent lootComponent = new LootCrateLootComponent(lootTableKey, 0, lockMode, replenishTimeScale, replenishTimeTicks, InventoryDeletionMode.NEVER, trackedPerPlayer, false);
									itemStack.set(LootCrateDataComponentTypes.LOOT_CRATE_LOOT, lootComponent);
									stacks.add(itemStack);
								}
							} else {
								LootCrateLootComponent lootComponent = new LootCrateLootComponent(lootTableKey, 0, lockMode, replenishTimeScale, 0, InventoryDeletionMode.NEVER, trackedPerPlayer, false);
								ItemStack itemStack = new ItemStack(lootCrateItem);
								itemStack.set(LootCrateDataComponentTypes.LOOT_CRATE_LOOT, lootComponent);
								stacks.add(itemStack);
							}
						}
					}
				}
			}
		}
		
		return stacks;
	}
	
	/**
	 * Generates a default item for a lot of predefined values of itemStacks
	 *
	 * @return All generated ItemStacks
	 */
	private static @NotNull ArrayList<ItemStack> getPredefinedLootBags() {
		ArrayList<ItemStack> stacks = new ArrayList<>();
		
		Item lootBagItem = LootCrateAtlas.getAllLootBagItems().get(0);
		Set<RegistryKey<LootTable>> allLootTables = LootTables.getAll();
		
		for (RegistryKey<LootTable> lootTableKey : allLootTables) {
			Identifier lootTableId = lootTableKey.getValue();
			if (lootTableId.getNamespace().equals("minecraft") && lootTableId.getPath().startsWith("chests/")) { // to reduce the lists size
				ItemStack itemStack = new ItemStack(lootBagItem);
				itemStack.set(DataComponentTypes.CONTAINER_LOOT, new ContainerLootComponent(lootTableKey, 0));
				stacks.add(itemStack);
			}
		}
		
		return stacks;
	}
}
