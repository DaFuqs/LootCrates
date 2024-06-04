package de.dafuqs.lootcrates.components;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import de.dafuqs.lootcrates.blocks.*;
import net.minecraft.loot.*;
import net.minecraft.registry.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

import java.time.*;
import java.time.temporal.*;
import java.util.*;

public record LootCrateLootComponent(RegistryKey<LootTable> lootTable, long seed, LockMode lockMode, ReplenishTimeScale replenishTimeScale, long replenishTime, InventoryDeletionMode inventoryDeletionMode, boolean trackedPerPlayer, boolean trapped) {
    
    public static final Codec<LootCrateLootComponent> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			RegistryKey.createCodec(RegistryKeys.LOOT_TABLE).fieldOf("loot_table").forGetter(LootCrateLootComponent::lootTable),
			Codec.LONG.optionalFieldOf("seed", 0L).forGetter(LootCrateLootComponent::seed),
			LockMode.CODEC.optionalFieldOf("lock_mode", LockMode.NOT_LOCKED).forGetter(LootCrateLootComponent::lockMode),
			ReplenishTimeScale.CODEC.optionalFieldOf("replenish_time_scale", ReplenishTimeScale.NEVER).forGetter(LootCrateLootComponent::replenishTimeScale),
			Codec.LONG.optionalFieldOf("replenish_time", -1L).forGetter(LootCrateLootComponent::replenishTime),
			InventoryDeletionMode.CODEC.optionalFieldOf("inventory_deletion_mode", InventoryDeletionMode.NEVER).forGetter(LootCrateLootComponent::inventoryDeletionMode),
			Codec.BOOL.optionalFieldOf("tracked_per_player", false).forGetter(LootCrateLootComponent::trackedPerPlayer),
			Codec.BOOL.optionalFieldOf("trapped", false).forGetter(LootCrateLootComponent::trapped)
	).apply(instance, LootCrateLootComponent::new));
    
    public enum InventoryDeletionMode implements StringIdentifiable {
        NEVER("never"), // the crate never deletes its inventory. Useful when the crate drops on a per-player basis, like on mob kill
        ON_OPEN("on_open"), // the crate deletes its inventory before it gets opened by the player, giving the player a fresh inventory every time
        WHEN_REPLENISHED("when_replenished"); // the crate deletes its inventory each time it generates new loot
        
        public static final EnumCodec<InventoryDeletionMode> CODEC = StringIdentifiable.createCodec(InventoryDeletionMode::values);
        
        private final String name;
        
        InventoryDeletionMode(String name) {
            this.name = name;
        }
        
        @Override
        public String asString() {
            return this.name;
        }
        
    }
    
    public enum LockMode implements StringIdentifiable {
        NOT_LOCKED("lot_locked", false, false, false),
        REQUIRE_KEY("require_key",true, false, false),
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
            if(requiresKey) {
                if(playerCrateData.isEmpty()) {
                    return false;
                }
                
                PlayerCrateData playerCrateData1 = playerCrateData.get();
                return playerCrateData1.unlockTime > 0;
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
            if(playerCrateData.isEmpty()) {
                return true;
            }
            
            switch (this) {
                case TICKS -> {
                    // check if there was enough time since the last opening
                    return world.getTime() > playerCrateData.get().replenishTime + replenishTime;
                }
                case HOURS -> {
                    ZonedDateTime now = ZonedDateTime.now();
                    ZonedDateTime then = Instant.ofEpochMilli(playerCrateData.get().replenishTime).atZone(ZoneId.systemDefault());
                    
                    return now.getYear() > then.getYear() || now.getDayOfYear() > then.getDayOfYear() || now.getHour() > then.getHour();
                }
                case DAYS -> {
                    ZonedDateTime now = ZonedDateTime.now();
                    ZonedDateTime then = Instant.ofEpochMilli(playerCrateData.get().replenishTime).atZone(ZoneId.systemDefault());
    
                    return now.getYear() > then.getYear() || now.getDayOfYear() > then.getDayOfYear();
                }
                case WEEKS -> {
                    ZonedDateTime now = ZonedDateTime.now();
                    ZonedDateTime then = Instant.ofEpochMilli(playerCrateData.get().replenishTime).atZone(ZoneId.systemDefault());
                    
                    return now.getYear() > then.getYear() || now.get(ChronoField.ALIGNED_WEEK_OF_YEAR) > then.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
                }
                case MONTHS -> {
                    ZonedDateTime now = ZonedDateTime.now();
                    ZonedDateTime then = Instant.ofEpochMilli(playerCrateData.get().replenishTime).atZone(ZoneId.systemDefault());
                    
                    return now.getYear() > then.getYear() || now.getMonthValue() > then.getMonthValue();
                }
                case SECONDS -> {
                    long currentTime = ZonedDateTime.now().toInstant().toEpochMilli() / 1000L;
                    return currentTime > (playerCrateData.get().replenishTime + replenishTime) / 1000L;
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
}
