package de.dafuqs.lootcrates.blocks.modes;

import de.dafuqs.lootcrates.blocks.PlayerCrateData;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Optional;

public enum ReplenishMode {
	NEVER(false, false),
	GAME_TIME(false, true), // in game ticks since last opening
	HOURLY(true, false), // each new hour
	DAILY(true, false), // each new day
	WEEKLY(true, false), // each new week
	MONTHLY(true, false), // each new month
	REAL_TIME(true, true); // real life milliseconds, like after 24h
	
	public final boolean usesRealTime; // real time = computer clock. Else ingame ticks
	public final boolean requiresTickData; // if "replenishTimeTicks" is needed for that mode to work
	
	ReplenishMode (boolean usesRealTime, boolean requiresTickData) {
		this.usesRealTime = usesRealTime;
		this.requiresTickData = requiresTickData;
	}
	
	public boolean canReplenish(World world, @NotNull Optional<PlayerCrateData> playerCrateData, long replenishTimeTicks) {
		if(playerCrateData.isEmpty()) {
			return true;
		}
		
		switch (this) {
			case GAME_TIME -> {
				// check if there was enough time since the last opening
				return world.getTime() > playerCrateData.get().replenishTime + replenishTimeTicks;
			}
			case HOURLY -> {
				ZonedDateTime now = ZonedDateTime.now();
				ZonedDateTime then = Instant.ofEpochMilli(playerCrateData.get().replenishTime).atZone(ZoneId.systemDefault());
				
				return now.get(ChronoField.YEAR) > then.get(ChronoField.YEAR) || now.get(ChronoField.DAY_OF_YEAR) > then.get(ChronoField.DAY_OF_YEAR) || now.get(ChronoField.HOUR_OF_DAY) > then.get(ChronoField.HOUR_OF_DAY);
			}
			case DAILY -> {
				ZonedDateTime now = ZonedDateTime.now();
				ZonedDateTime then = Instant.ofEpochMilli(playerCrateData.get().replenishTime).atZone(ZoneId.systemDefault());

				return now.get(ChronoField.YEAR) > then.get(ChronoField.YEAR) || now.get(ChronoField.DAY_OF_YEAR) > then.get(ChronoField.DAY_OF_YEAR);
			}
			case WEEKLY -> {
				ZonedDateTime now = ZonedDateTime.now();
				ZonedDateTime then = Instant.ofEpochMilli(playerCrateData.get().replenishTime).atZone(ZoneId.systemDefault());
				
				return now.get(ChronoField.YEAR) > then.get(ChronoField.YEAR) || now.get(ChronoField.ALIGNED_WEEK_OF_YEAR) > then.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
			}
			case MONTHLY -> {
				ZonedDateTime now = ZonedDateTime.now();
				ZonedDateTime then = Instant.ofEpochMilli(playerCrateData.get().replenishTime).atZone(ZoneId.systemDefault());
				
				return now.get(ChronoField.YEAR) > then.get(ChronoField.YEAR) || now.get(ChronoField.MONTH_OF_YEAR) > then.get(ChronoField.MONTH_OF_YEAR);
			}
			case REAL_TIME -> {
				long currentTime = ZonedDateTime.now().toInstant().toEpochMilli();
				return currentTime > playerCrateData.get().replenishTime + replenishTimeTicks;
			}
			default -> { // NEVER
				// crate was opened before (in general or by that player)
				// => just generate loot once
				return false;
			}
		}
	}
	
}