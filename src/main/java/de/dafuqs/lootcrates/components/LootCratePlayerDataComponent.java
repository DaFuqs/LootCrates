package de.dafuqs.lootcrates.components;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import de.dafuqs.lootcrates.blocks.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import org.jetbrains.annotations.*;

import java.time.*;
import java.util.*;

public record LootCratePlayerDataComponent(List<PlayerCrateData> playerData) {
	
	public static final Codec<LootCratePlayerDataComponent> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			PlayerCrateData.CODEC.listOf().fieldOf("player_data").forGetter(LootCratePlayerDataComponent::playerData)
	).apply(instance, LootCratePlayerDataComponent::new));
	
	private static final UUID NO_PLAYER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
	
	public static void lockForPlayer(ItemStack stack, PlayerEntity player, boolean trackedPerPlayer) {
		setUnlockTime(stack, player, trackedPerPlayer, -1);
	}
	
	public static void unlockForPlayer(ItemStack stack, PlayerEntity player, boolean trackedPerPlayer, @NotNull ReplenishTimeScale replenishTimeScale) {
		long time = replenishTimeScale.usesRealTime ? ZonedDateTime.now().toInstant().toEpochMilli() : player.getWorld().getTime();
		setUnlockTime(stack, player, trackedPerPlayer, time);
	}
	
	public static void setReplenishedForPlayer(ItemStack stack, PlayerEntity player, boolean trackedPerPlayer, @NotNull ReplenishTimeScale replenishTimeScale) {
		long time = replenishTimeScale.usesRealTime ? ZonedDateTime.now().toInstant().toEpochMilli() : player.getWorld().getTime();
		setReplenishTime(stack, player, trackedPerPlayer, time);
	}
	
	private static void setUnlockTime(@NotNull ItemStack stack, PlayerEntity player, boolean trackedPerPlayer, long unlockTime) {
		@Nullable LootCratePlayerDataComponent playerDataComponent = stack.get(LootCrateDataComponentTypes.LOOT_CRATE_PLAYER_DATA);
		UUID playerUUID = trackedPerPlayer ? player.getUuid() : NO_PLAYER_UUID;
		
		List<PlayerCrateData> newData = new ArrayList<>();
		if (playerDataComponent != null) {
			newData.addAll(playerDataComponent.playerData);
		}
		
		@Nullable PlayerCrateData foundEntry = null;
		for (PlayerCrateData entry : newData) {
			if (entry.uuid().equals(playerUUID)) {
				foundEntry = entry;
				break;
			}
		}
		
		if (foundEntry == null) {
			newData.add(new PlayerCrateData(playerUUID, -1, unlockTime));
		} else {
			newData.remove(foundEntry);
			newData.add(new PlayerCrateData(playerUUID, foundEntry.replenishTime(), unlockTime));
		}
		
		LootCratePlayerDataComponent newPlayerDataComponent = new LootCratePlayerDataComponent(newData);
		stack.set(LootCrateDataComponentTypes.LOOT_CRATE_PLAYER_DATA, newPlayerDataComponent);
	}
	
	public static void setReplenishTime(@NotNull ItemStack stack, PlayerEntity player, boolean trackedPerPlayer, long replenishTime) {
		@Nullable LootCratePlayerDataComponent playerDataComponent = stack.get(LootCrateDataComponentTypes.LOOT_CRATE_PLAYER_DATA);
		UUID playerUUID = trackedPerPlayer ? player.getUuid() : NO_PLAYER_UUID;
		
		List<PlayerCrateData> newData = new ArrayList<>();
		if (playerDataComponent != null) {
			newData.addAll(playerDataComponent.playerData);
		}
		
		@Nullable PlayerCrateData foundEntry = null;
		for (PlayerCrateData entry : newData) {
			if (entry.uuid().equals(playerUUID)) {
				foundEntry = entry;
				break;
			}
		}
		
		if (foundEntry == null) {
			newData.add(new PlayerCrateData(playerUUID, replenishTime, -1));
		} else {
			newData.remove(foundEntry);
			newData.add(new PlayerCrateData(playerUUID, replenishTime, foundEntry.unlockTime()));
		}
		
		LootCratePlayerDataComponent newPlayerDataComponent = new LootCratePlayerDataComponent(newData);
		stack.set(LootCrateDataComponentTypes.LOOT_CRATE_PLAYER_DATA, newPlayerDataComponent);
	}
	
	
	public Optional<PlayerCrateData> get(PlayerEntity player, boolean trackedPerPlayer) {
		UUID playerUUID = trackedPerPlayer ? player.getUuid() : NO_PLAYER_UUID;
		
		for (PlayerCrateData entry : playerData) {
			if (entry.uuid().equals(playerUUID)) {
				return Optional.of(entry);
			}
		}
		
		return Optional.empty();
	}
	
}
