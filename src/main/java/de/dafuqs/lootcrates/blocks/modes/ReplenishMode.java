package de.dafuqs.lootcrates.blocks.modes;

public enum ReplenishMode {
	NEVER,
	PASSED_TIME_SINCE_LAST_OPEN, // time since last opening
	HOURLY, // between real life hours
	DAILY, // at 0:00 real life day
	WEEKLY // each Monday 0:00
}