package de.dafuqs.lootcrates.blocks;

import net.minecraft.nbt.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class PlayerCrateData {
	
	public long replenishTime;
	public long unlockTime; // for the relock to only lock back up when there is new loot to generate
	
	public PlayerCrateData(long replenishTime, long unlockTime) {
		this.replenishTime = replenishTime;
		this.unlockTime = unlockTime;
	}
	
	public NbtCompound toCompound(@Nullable UUID uuid) {
		NbtCompound nbtCompound = new NbtCompound();
		if (uuid != null) {
			nbtCompound.putUuid("UUID", uuid);
		}
		nbtCompound.putLong("ReplenishTime", replenishTime);
		nbtCompound.putLong("UnlockTime", unlockTime);
		return nbtCompound;
	}
	
	@Contract("_ -> new")
	public static @NotNull Pair<UUID, PlayerCrateData> fromCompoundWithUUID(@NotNull NbtCompound nbtCompound) {
		UUID uuid = null;
		if (nbtCompound.contains("UUID")) {
			uuid = nbtCompound.getUuid("UUID");
		}
		return new Pair<>(uuid, fromCompoundWithoutUUID(nbtCompound));
	}
	
	@Contract("_ -> new")
	public static @NotNull PlayerCrateData fromCompoundWithoutUUID(@NotNull NbtCompound nbtCompound) {
		return new PlayerCrateData(nbtCompound.getLong("ReplenishTime"), nbtCompound.getLong("UnlockTime"));
	}
	
	public static @NotNull Optional<PlayerCrateData> getPlayerSpecificCrateData(NbtCompound nbtCompound, UUID uuid) {
		if (nbtCompound != null) {
			NbtList nbtList = nbtCompound.getList("PlayerData", NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < nbtList.size(); i++) {
				NbtCompound playerCompound = nbtList.getCompound(i);
				if (uuid.equals(playerCompound.getUuid("UUID"))) {
					return Optional.of(PlayerCrateData.fromCompoundWithoutUUID(playerCompound));
				}
			}
		}
		return Optional.empty();
	}
	
}
