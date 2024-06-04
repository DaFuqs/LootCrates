package de.dafuqs.lootcrates.components;

import de.dafuqs.lootcrates.blocks.*;
import net.minecraft.util.*;

import java.util.*;

public enum LockMode implements StringIdentifiable {
	NOT_LOCKED("lot_locked", false, false, false),
	REQUIRE_KEY("require_key", true, false, false),
	REQUIRE_KEY_RELOCK("require_key_relock", true, false, true),
	CONSUME_KEY("consume_key", true, true, false),
	CONSUME_KEY_RELOCK("consume_key_relock", true, true, true);
	
	private final String name;
	private final boolean requiresKey;
	private final boolean consumesKey;
	private final boolean relocks;
	
	public static final EnumCodec<LockMode> CODEC = StringIdentifiable.createCodec(LockMode::values);
	
	LockMode(String name, boolean requiresKey, boolean consumesKey, boolean relocks) {
		this.name = name;
		this.requiresKey = requiresKey;
		this.consumesKey = consumesKey;
		this.relocks = relocks;
	}
	
	public boolean isUnlocked(Optional<PlayerCrateData> playerCrateData) {
		if (requiresKey) {
			if (playerCrateData.isEmpty()) {
				return false;
			}
			return playerCrateData.get().unlockTime() > 0;
		}
		
		return true;
	}
	
	public boolean consumesKey() {
		return this.consumesKey;
	}
	
	public boolean relocks() {
		return this.relocks;
	}
	
	@Override
	public String asString() {
		return this.name;
	}
	
}
