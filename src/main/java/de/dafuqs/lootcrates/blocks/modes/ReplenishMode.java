package de.dafuqs.lootcrates.blocks.modes;

public enum ReplenishMode {
	NEVER(false),
	GAME_TIME(false), // time since last opening
	HOURLY(true), // each new hour
	DAILY(true), // each day at 0:00
	REAL_TIME(true); // real life hours, like after 24h
	
	public final boolean usesRealTime;
	
	ReplenishMode (boolean usesRealTime) {
		this.usesRealTime = usesRealTime;
	}
}