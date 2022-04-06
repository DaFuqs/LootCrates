package de.dafuqs.lootcrates.blocks.modes;

import de.dafuqs.lootcrates.blocks.PlayerCrateData;

import java.util.Optional;

public enum LockMode {
	NONE(false, false, false),
	REQUIRE_KEY(true, false, false),
	REQUIRE_KEY_RELOCK(true, false, true),
	CONSUME_KEY(true, true, false),
	CONSUME_KEY_RELOCK(true, true, true);

	private final boolean requiresKey;
	private final boolean consumesKey;
	private final boolean relocks;
	
	LockMode(boolean requiresKey, boolean consumesKey, boolean relocks) {
		this.requiresKey = requiresKey;
		this.consumesKey = consumesKey;
		this.relocks = relocks;
	}
	
	public boolean isUnlocked(Optional<PlayerCrateData> playerCrateData) {
		if(requiresKey) {
			if(playerCrateData.isEmpty()) {
				return false;
			} else {
				PlayerCrateData playerCrateData1 = playerCrateData.get();
				if (relocks()) {
					return playerCrateData1.unlockTime > 0 && playerCrateData1.unlockTime > playerCrateData1.replenishTime;
				} else {
					return playerCrateData1.unlockTime > 0;
				}
			}
		} else {
			return true;
		}
	}
	
	public boolean requiresKey() {
		return this.requiresKey;
	}
    
    public boolean consumesKey() {
		return this.consumesKey;
    }
	
    public boolean relocks() {
		return this.relocks;
    }
    
}