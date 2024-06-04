package de.dafuqs.lootcrates.blocks;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import net.minecraft.util.*;

import java.util.*;

// unlockTime is for the relock to only lock back up when there is new loot to generate
public record PlayerCrateData(UUID uuid, long replenishTime, long unlockTime) {
	
	public static final Codec<PlayerCrateData> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			Uuids.CODEC.fieldOf("uuid").forGetter(PlayerCrateData::uuid),
			Codec.LONG.fieldOf("replenish_time").forGetter(PlayerCrateData::replenishTime),
			Codec.LONG.fieldOf("unlock_time").forGetter(PlayerCrateData::unlockTime)
	).apply(instance, PlayerCrateData::new));
	
	
	/*public NbtCompound toCompound(@Nullable UUID uuid) {
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
	}*/
	
}
