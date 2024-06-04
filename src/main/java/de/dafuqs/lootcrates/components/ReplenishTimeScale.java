package de.dafuqs.lootcrates.components;

import de.dafuqs.lootcrates.blocks.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

import java.time.*;
import java.time.temporal.*;
import java.util.*;

public enum ReplenishTimeScale implements StringIdentifiable {
	NEVER("never", false, false),
	TICKS("ticks", false, true), // in game ticks since last opening
	SECONDS("seconds", true, true), // real life milliseconds, like after 24h
	HOURS("hours", true, false), // each new hour
	DAYS("days", true, false), // each new day
	WEEKS("weeks", true, false), // each new week
	MONTHS("months", true, false); // each new month
	
	private final String name;
	public final boolean usesRealTime; // real time = computer clock. Else ingame ticks
	public final boolean requiresTickData; // if "replenishTimeTicks" is needed for that mode to work
	
	public static final EnumCodec<ReplenishTimeScale> CODEC = StringIdentifiable.createCodec(ReplenishTimeScale::values);
	
	ReplenishTimeScale(String name, boolean usesRealTime, boolean requiresTickData) {
		this.name = name;
		this.usesRealTime = usesRealTime;
		this.requiresTickData = requiresTickData;
	}
	
	public boolean canReplenish(World world, @NotNull Optional<PlayerCrateData> playerCrateData, long replenishTime) {
		if (playerCrateData.isEmpty()) {
			return true;
		}
		
		switch (this) {
			case TICKS -> {
				// check if there was enough time since the last opening
				return world.getTime() > playerCrateData.get().replenishTime() + replenishTime;
			}
			case HOURS -> {
				ZonedDateTime now = ZonedDateTime.now();
				ZonedDateTime then = Instant.ofEpochMilli(playerCrateData.get().replenishTime()).atZone(ZoneId.systemDefault());
				
				return now.getYear() > then.getYear() || now.getDayOfYear() > then.getDayOfYear() || now.getHour() > then.getHour();
			}
			case DAYS -> {
				ZonedDateTime now = ZonedDateTime.now();
				ZonedDateTime then = Instant.ofEpochMilli(playerCrateData.get().replenishTime()).atZone(ZoneId.systemDefault());
				
				return now.getYear() > then.getYear() || now.getDayOfYear() > then.getDayOfYear();
			}
			case WEEKS -> {
				ZonedDateTime now = ZonedDateTime.now();
				ZonedDateTime then = Instant.ofEpochMilli(playerCrateData.get().replenishTime()).atZone(ZoneId.systemDefault());
				
				return now.getYear() > then.getYear() || now.get(ChronoField.ALIGNED_WEEK_OF_YEAR) > then.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
			}
			case MONTHS -> {
				ZonedDateTime now = ZonedDateTime.now();
				ZonedDateTime then = Instant.ofEpochMilli(playerCrateData.get().replenishTime()).atZone(ZoneId.systemDefault());
				
				return now.getYear() > then.getYear() || now.getMonthValue() > then.getMonthValue();
			}
			case SECONDS -> {
				long currentTime = ZonedDateTime.now().toInstant().toEpochMilli() / 1000L;
				return currentTime > (playerCrateData.get().replenishTime() + replenishTime) / 1000L;
			}
			default -> { // NEVER
				// crate was opened before (in general or by that player)
				// => just generate loot once
				return false;
			}
		}
	}
	
	@Override
	public String asString() {
		return this.name;
	}
	
}
