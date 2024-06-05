package de.dafuqs.lootcrates.components;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import net.minecraft.entity.player.*;
import org.jetbrains.annotations.*;

import java.time.*;
import java.util.*;

public record LootCratePlayerDataComponent(List<PlayerCrateData> playerData) {
	
	public static final Codec<LootCratePlayerDataComponent> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			PlayerCrateData.CODEC.listOf().fieldOf("player_data").forGetter(LootCratePlayerDataComponent::playerData)
	).apply(instance, LootCratePlayerDataComponent::new));
	
	private static final UUID NO_PLAYER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
	
	public static LootCratePlayerDataComponent lockForPlayer(@Nullable LootCratePlayerDataComponent playerDataComponent, PlayerEntity player, boolean trackedPerPlayer) {
		return setUnlockTime(playerDataComponent, player, trackedPerPlayer, -1);
	}
	
	public static LootCratePlayerDataComponent unlockForPlayer(@Nullable LootCratePlayerDataComponent playerDataComponent, PlayerEntity player, boolean trackedPerPlayer, @NotNull ReplenishTimeScale replenishTimeScale) {
		long time = replenishTimeScale.usesRealTime ? ZonedDateTime.now().toInstant().toEpochMilli() : player.getWorld().getTime();
		return setUnlockTime(playerDataComponent, player, trackedPerPlayer, time);
	}
	
	public static LootCratePlayerDataComponent setReplenishedForPlayer(@Nullable LootCratePlayerDataComponent playerDataComponent, PlayerEntity player, boolean trackedPerPlayer, @NotNull ReplenishTimeScale replenishTimeScale) {
		long time = replenishTimeScale.usesRealTime ? ZonedDateTime.now().toInstant().toEpochMilli() : player.getWorld().getTime();
		return setReplenishTime(playerDataComponent, player, trackedPerPlayer, time);
	}
	
	private static LootCratePlayerDataComponent setUnlockTime(@Nullable LootCratePlayerDataComponent playerDataComponent, PlayerEntity player, boolean trackedPerPlayer, long unlockTime) {
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
		
		return new LootCratePlayerDataComponent(newData);
	}
	
	public static LootCratePlayerDataComponent setReplenishTime(@Nullable LootCratePlayerDataComponent playerDataComponent, PlayerEntity player, boolean trackedPerPlayer, long replenishTime) {
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
		
		return new LootCratePlayerDataComponent(newData);
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
