package de.dafuqs.lootcrates.components;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import de.dafuqs.lootcrates.*;
import de.dafuqs.lootcrates.enums.*;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.*;
import net.minecraft.loot.*;
import net.minecraft.registry.*;
import net.minecraft.text.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

import java.util.*;

public record LootCrateLootComponent(RegistryKey<LootTable> lootTable, long seed, LockMode lockMode,
									 ReplenishTimeScale replenishTimeScale, long replenishTime,
									 InventoryDeletionMode inventoryDeletionMode, boolean trackedPerPlayer,
									 boolean trapped) {
	
	public static final Codec<LootCrateLootComponent> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			RegistryKey.createCodec(RegistryKeys.LOOT_TABLE).fieldOf("loot_table").forGetter(LootCrateLootComponent::lootTable),
			Codec.LONG.optionalFieldOf("seed", 0L).forGetter(LootCrateLootComponent::seed),
			LockMode.CODEC.optionalFieldOf("lock_mode", LockMode.NOT_LOCKED).forGetter(LootCrateLootComponent::lockMode),
			ReplenishTimeScale.CODEC.optionalFieldOf("replenish_time_scale", ReplenishTimeScale.NEVER).forGetter(LootCrateLootComponent::replenishTimeScale),
			Codec.LONG.optionalFieldOf("replenish_time", -1L).forGetter(LootCrateLootComponent::replenishTime),
			InventoryDeletionMode.CODEC.optionalFieldOf("inventory_deletion_mode", InventoryDeletionMode.NEVER).forGetter(LootCrateLootComponent::inventoryDeletionMode),
			Codec.BOOL.optionalFieldOf("tracked_per_player", false).forGetter(LootCrateLootComponent::trackedPerPlayer),
			Codec.BOOL.optionalFieldOf("trapped", false).forGetter(LootCrateLootComponent::trapped)
	).apply(instance, LootCrateLootComponent::new));
	
	public boolean isUnlocked(Optional<PlayerCrateData> playerCrateData) {
		return this.lockMode.isUnlocked(playerCrateData);
	}
	
	public void appendTooltip(LootCrateVariant variant, Optional<PlayerCrateData> playerCrateData, World world, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
		
		if (playerCrateData.isEmpty() || playerCrateData.get().replenishTime() < 0 || replenishTimeScale.canReplenish(world, playerCrateData, replenishTime)) {
			tooltip.add(Text.translatable("item.lootcrates.loot_crate.tooltip.contains_loot"));
		} else {
			if (trackedPerPlayer) {
				tooltip.add(Text.translatable("item.lootcrates.loot_crate.tooltip.already_looted_by_you"));
			} else {
				tooltip.add(Text.translatable("item.lootcrates.loot_crate.tooltip.already_looted"));
			}
		}
		
		if (!lockMode.isUnlocked(playerCrateData)) {
			tooltip.add(LootCrateAtlas.getItemLockedTooltip(variant, lockMode));
		}
		
		switch (replenishTimeScale) {
			case HOURS -> {
				tooltip.add(Text.translatable("item.lootcrates.loot_crate.tooltip.replenish_hourly"));
			}
			case DAYS -> {
				tooltip.add(Text.translatable("item.lootcrates.loot_crate.tooltip.replenish_daily"));
			}
			case WEEKS -> {
				tooltip.add(Text.translatable("item.lootcrates.loot_crate.tooltip.replenish_weekly"));
			}
			case MONTHS -> {
				tooltip.add(Text.translatable("item.lootcrates.loot_crate.tooltip.replenish_monthly"));
			}
			case TICKS -> {
				Text text = getReplenishTimeGameTimeHumanReadableText(replenishTime);
				if (text != null) {
					tooltip.add(text);
				}
			}
			case SECONDS -> {
				Text text = getReplenishTimeRealTimeHumanReadableText(replenishTime);
				if (text != null) {
					tooltip.add(text);
				}
			}
		}
		if (trackedPerPlayer) {
			tooltip.add(Text.translatable("item.lootcrates.loot_crate.tooltip.tracked_per_player"));
		}
		if (lockMode.relocks()) {
			tooltip.add(Text.translatable("item.lootcrates.loot_crate.tooltip.relocks"));
		}
		
		switch (inventoryDeletionMode) {
			case ON_OPEN -> {
				tooltip.add(Text.translatable("item.lootcrates.loot_crate.tooltip.inventory_deletes_on_open"));
			}
			case WHEN_REPLENISHED -> {
				tooltip.add(Text.translatable("item.lootcrates.loot_crate.tooltip.inventory_deletes_when_replenished"));
			}
		}
		
		if (trapped) {
			tooltip.add(Text.translatable("item.lootcrates.loot_crate.tooltip.trapped"));
		}
		
		// loot table and seed
		boolean advanced = type.isAdvanced();
		if (advanced) {
			tooltip.add(Text.translatable("item.lootcrates.loot_crate.tooltip.loot_table", lootTable.getValue().toString()));
			if (seed != 0) {
				tooltip.add(Text.translatable("item.lootcrates.loot_crate.tooltip.fixed_seed", seed));
			}
		}
	}
	
	public static @Nullable Text getReplenishTimeGameTimeHumanReadableText(long replenishTime) {
		if (replenishTime >= 1728000) { // 1 day
			return Text.translatable("item.lootcrates.loot_crate.tooltip.replenish_game_time_days", replenishTime / 1728000F);
		} else if (replenishTime >= 72000) { // 1 hour
			return Text.translatable("item.lootcrates.loot_crate.tooltip.replenish_game_time_hours", replenishTime / 72000F);
		} else if (replenishTime >= 1200) { // 1 minute
			return Text.translatable("item.lootcrates.loot_crate.tooltip.replenish_game_time_minutes", replenishTime / 1200F);
		} else if (replenishTime <= 0) {
			// does not replenish
			return null;
		} else { // in ticks
			return Text.translatable("item.lootcrates.loot_crate.tooltip.replenish_game_time_ticks", replenishTime);
		}
	}
	
	public static @Nullable Text getReplenishTimeRealTimeHumanReadableText(long replenishTime) {
		if (replenishTime >= 1728000) { // 1 day
			return Text.translatable("item.lootcrates.loot_crate.tooltip.replenish_real_time_days", replenishTime / 1728000F);
		} else if (replenishTime >= 72000) { // 1 hour
			return Text.translatable("item.lootcrates.loot_crate.tooltip.replenish_real_time_hours", replenishTime / 72000F);
		} else if (replenishTime >= 1200) { // 1 minute
			return Text.translatable("item.lootcrates.loot_crate.tooltip.replenish_real_time_minutes", replenishTime / 1200F);
		} else if (replenishTime <= 0) {
			// does not replenish
			return null;
		} else { // in ticks
			return Text.translatable("item.lootcrates.loot_crate.tooltip.replenish_real_time_ticks", replenishTime);
		}
	}
	
	
}
