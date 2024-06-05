package de.dafuqs.lootcrates.components;

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
	
}
