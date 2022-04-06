package de.dafuqs.lootcrates.blocks.modes;

public enum ReplenishMode {
	NEVER(false, false),
	GAME_TIME(false, true), // time since last opening
	HOURLY(true, false), // each new hour
	DAILY(true, false), // each day at 0:00
	REAL_TIME(true, true); // real life hours, like after 24h
	
	public final boolean usesRealTime; // real time = computer clock. Else ingame ticks
	public final boolean usesTickData; // if "replenishTimeTicks" is needed for that mode to work
	
	ReplenishMode (boolean usesRealTime, boolean usesTickData) {
		this.usesRealTime = usesRealTime;
		this.usesTickData = usesTickData;
	}
	
}