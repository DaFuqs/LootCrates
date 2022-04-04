package de.dafuqs.lootcrates.blocks.modes;

public enum ReplenishMode {
	NEVER,
	PASSED_TIME_SINCE_LAST_OPEN, // time since last opening
	INVERVAL, // real life hours, like "once each day", or "each ingame day"
}