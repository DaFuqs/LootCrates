package de.dafuqs.lootcrates.config;

import de.dafuqs.lootcrates.components.*;
import de.dafuqs.lootcrates.enums.*;
import net.minecraft.loot.*;
import net.minecraft.registry.*;
import org.jetbrains.annotations.*;

public class LootCrateReplacementEntry {
	
	@Nullable
	public final LootCrateVariant lootCrateVariant;
	@Nullable
	public final RegistryKey<LootTable> lootTable;
	
	public final LockMode lockMode;
	public final ReplenishTimeScale replenishTimeScale;
	public final InventoryDeletionMode inventoryDeletionMode;
	public final boolean trackedPerPlayer;
	public final int replenishTimeTicks;
	
	public final int weight;
	
	public LootCrateReplacementEntry(@Nullable LootCrateVariant lootCrateVariant, @Nullable RegistryKey<LootTable> lootTable, ReplenishTimeScale replenishTimeScale, int replenishTimeTicks, LockMode lockMode, InventoryDeletionMode inventoryDeletionMode, boolean trackedPerPlayer, int weight) {
		this.lootCrateVariant = lootCrateVariant;
		this.lootTable = lootTable;
		
		this.lockMode = lockMode;
		this.replenishTimeScale = replenishTimeScale;
		this.inventoryDeletionMode = inventoryDeletionMode;
		this.trackedPerPlayer = trackedPerPlayer;
		this.replenishTimeTicks = replenishTimeTicks;
		
		this.weight = weight;
	}
	
}