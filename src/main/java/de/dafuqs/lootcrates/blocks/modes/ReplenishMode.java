package de.dafuqs.lootcrates.blocks.modes;

import de.dafuqs.lootcrates.blocks.PlayerCrateData;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

public enum ReplenishMode {
	NEVER(false, false),
	GAME_TIME(false, true), // time since last opening
	HOURLY(true, false), // each new hour
	DAILY(true, false), // each new day
	WEEKLY(true, false), // each new week
	MONTHLY(true, false), // each new month
	REAL_TIME(true, true); // real life hours, like after 24h
	
	public final boolean usesRealTime; // real time = computer clock. Else ingame ticks
	public final boolean usesTickData; // if "replenishTimeTicks" is needed for that mode to work
	
	ReplenishMode (boolean usesRealTime, boolean usesTickData) {
		this.usesRealTime = usesRealTime;
		this.usesTickData = usesTickData;
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
				Calendar calendar = GregorianCalendar.getInstance();
				Date lastDate = new Date(playerCrateData.get().replenishTime);
				calendar.add(Calendar.HOUR, -1);
				Date currentDate = calendar.getTime();
				
				return currentDate.after(lastDate);
			}
			case DAILY -> {
				Calendar calendar = GregorianCalendar.getInstance();
				Date lastDate = new Date(playerCrateData.get().replenishTime);
				calendar.add(Calendar.DAY_OF_MONTH, -1);
				Date currentDate = calendar.getTime();
				
				return currentDate.after(lastDate);
			}
			case WEEKLY -> {
				Calendar calendar = GregorianCalendar.getInstance();
				Date lastDate = new Date(playerCrateData.get().replenishTime);
				calendar.add(Calendar.WEEK_OF_YEAR, -1);
				Date currentDate = calendar.getTime();
				
				return currentDate.after(lastDate);
			}
			case MONTHLY -> {
				Calendar calendar = GregorianCalendar.getInstance();
				Date lastDate = new Date(playerCrateData.get().replenishTime);
				calendar.add(Calendar.MONTH, -1);
				Date currentDate = calendar.getTime();
				
				return currentDate.after(lastDate);
			}
			case REAL_TIME -> {
				Calendar calendar = GregorianCalendar.getInstance();
				long currentTime = calendar.getTime().getTime(); // Milliseconds since unix epoch
				
				return currentTime > playerCrateData.get().replenishTime + replenishTimeTicks;
			}
			default -> {
				// crate was opened before (in general or by that player)
				// => just generate loot once
				return false;
			}
		}
	}
	
}