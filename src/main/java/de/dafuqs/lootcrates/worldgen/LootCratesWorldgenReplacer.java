package de.dafuqs.lootcrates.worldgen;

import com.google.gson.*;
import de.dafuqs.lootcrates.LootCrateAtlas;
import de.dafuqs.lootcrates.LootCrates;
import de.dafuqs.lootcrates.blocks.LootCrateBlock;
import de.dafuqs.lootcrates.blocks.LootCrateBlockEntity;
import de.dafuqs.lootcrates.blocks.chest.ChestLootCrateBlock;
import de.dafuqs.lootcrates.blocks.modes.InventoryDeletionMode;
import de.dafuqs.lootcrates.blocks.modes.LockMode;
import de.dafuqs.lootcrates.blocks.modes.ReplenishMode;
import de.dafuqs.lootcrates.config.LootCrateReplacementEntry;
import de.dafuqs.lootcrates.config.WeightedLootCrateEntryList;
import de.dafuqs.lootcrates.enums.LootCrateRarity;
import de.dafuqs.lootcrates.mixin.LootTableAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class LootCratesWorldgenReplacer {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final String DEFAULT_CONFIG = """
[
	{
		"loot_table": "",
		"entries": [
			{
				"crate_rarity": "common",
				"tracked_per_player": true,
				"lock_mode": "none",
				"inventory_deletion_mode": "never",
				"replenish_mode": "never",
				"weight": 5
			},
			{
				"crate_rarity": "epic",
				"tracked_per_player": true,
				"lock_mode": "none",
				"inventory_deletion_mode": "never",
				"replenish_mode": "never",
				"weight": 1
			}
		]
	},
	{
		"loot_table": "minecraft:chests/bastion_bridge",
		"entries": [{
			"crate_rarity": "blaze",
			"tracked_per_player": true,
			"lock_mode": "none",
			"inventory_deletion_mode": "never",
			"replenish_mode": "never"
		}]
	},
	{
		"loot_table": "minecraft:chests/bastion_hoglin_stable",
		"entries": [{
			"crate_rarity": "blaze",
			"tracked_per_player": true,
			"lock_mode": "none",
			"inventory_deletion_mode": "never",
			"replenish_mode": "never"
		}]
	},
	{
		"loot_table": "minecraft:chests/bastion_other",
		"entries": [{
			"crate_rarity": "blaze",
			"tracked_per_player": true,
			"lock_mode": "none",
			"inventory_deletion_mode": "never",
			"replenish_mode": "never"
		}]
	},
	{
		"loot_table": "minecraft:chests/bastion_treasure",
		"entries": [{
			"crate_rarity": "blaze",
			"tracked_per_player": true,
			"lock_mode": "none",
			"inventory_deletion_mode": "never",
			"replenish_mode": "never"
		}]
	},
	{
		"loot_table": "minecraft:chests/buried_treasure",
		"entries": [{
			"crate_rarity": "rare",
			"tracked_per_player": true,
			"lock_mode": "none",
			"inventory_deletion_mode": "never",
			"replenish_mode": "never"
		}]
	},
	{
		"loot_table": "minecraft:chests/end_city_treasure",
		"entries": [{
			"crate_rarity": "epic",
			"tracked_per_player": true,
			"lock_mode": "none",
			"inventory_deletion_mode": "never",
			"replenish_mode": "never"
		}]
	},
	{
		"loot_table": "minecraft:chests/simple_dungeon",
		"entries": [{
			"crate_rarity": "uncommon",
			"tracked_per_player": true,
			"lock_mode": "none",
			"inventory_deletion_mode": "never",
			"replenish_mode": "never"
		}]
	},
	{
		"loot_table": "minecraft:chests/stronghold_corridor",
		"entries": [{
			"crate_rarity": "uncommon",
			"tracked_per_player": true,
			"lock_mode": "none",
			"inventory_deletion_mode": "never",
			"replenish_mode": "never"
		}]
	},
	{
		"loot_table": "minecraft:chests/underwater_ruin_big",
		"entries": [{
			"crate_rarity": "rare",
			"tracked_per_player": true,
			"lock_mode": "none",
			"inventory_deletion_mode": "never",
			"replenish_mode": "never"
		}]
	},
	{
		"loot_table": "minecraft:chests/underwater_ruin_small",
		"entries": [{
			"crate_rarity": "rare",
			"tracked_per_player": true,
			"lock_mode": "none",
			"inventory_deletion_mode": "never",
			"replenish_mode": "never"
		}]
	},
	{
		"loot_table": "minecraft:chests/woodland_mansion",
		"entries": [{
			"crate_rarity": "ghost",
			"tracked_per_player": true,
			"lock_mode": "none",
			"inventory_deletion_mode": "never",
			"replenish_mode": "never"
		}]
	}
]""";
    

    public static List<LootCrateReplacementPosition> replacements = new ArrayList<>(); // try at the end of each tick

    private static WeightedLootCrateEntryList DefaultLootCrateProviderList = new WeightedLootCrateEntryList(1, new ArrayList<>() {{
        add(new LootCrateReplacementEntry(null, null, ReplenishMode.GAME_TIME, 1, LockMode.NONE, InventoryDeletionMode.NEVER, true, 1));
    }});
    private static final Map<Identifier, WeightedLootCrateEntryList> LootCrateProviders = new HashMap<>();

    public static void initialize() {
        File configFile = new File(FabricLoader.getInstance().getConfigDirectory(), "LootCratesWorldgenSettings.json5");
        FileReader configReader;
        try {
            configReader = new FileReader(configFile);
        } catch (Exception e) {
            try {
                configFile.createNewFile();

                FileWriter myWriter = new FileWriter(configFile);
                myWriter.write(DEFAULT_CONFIG);
                myWriter.close();

                configReader = new FileReader(configFile);
            } catch (IOException ioException) {
                LootCrates.log(Level.ERROR, "Could not generate config file under " + configFile.getAbsolutePath() + ".\n" + ioException.getLocalizedMessage());
                return;
            }
        }

        JsonElement jsonElement;
        try {
            jsonElement = JsonHelper.deserialize(GSON, configReader, JsonElement.class);
        } catch (Exception e) {
            LootCrates.log(Level.ERROR, "Could not parse the LootCratesWorldgenSettings.json5: " + e.getLocalizedMessage());
            return;
        }

        JsonArray array = jsonElement.getAsJsonArray();
        for(int i = 0; i < array.size(); i++) {
            JsonObject currentEntry = (JsonObject) array.get(i);

            Identifier lootTableIdentifier = Identifier.tryParse(JsonHelper.getString(currentEntry, "loot_table"));
            JsonArray weightedEntries = JsonHelper.getArray(currentEntry, "entries");
            int totalWeight = 0;
            List<LootCrateReplacementEntry> lootCrateEntries = new ArrayList<>();
            for(int j = 0; j < weightedEntries.size(); j++) {
                JsonObject targetEntry = (JsonObject) weightedEntries.get(j);

                LootCrateRarity lootCrateRarity = LootCrateRarity.COMMON;
                Identifier lootTable = null; // null => keep the original loot table
                boolean trackedPerPlayer = false;
                int replenishTimeTicks = -1;
                LockMode lockMode = LockMode.NONE;
                ReplenishMode replenishMode = ReplenishMode.NEVER;
                InventoryDeletionMode inventoryDeletionMode = InventoryDeletionMode.NEVER;
                int weight = 1;

                if(JsonHelper.hasString(targetEntry, "crate_rarity")) {
                    lootCrateRarity = LootCrateRarity.valueOf(JsonHelper.getString(targetEntry, "crate_rarity").toUpperCase(Locale.ROOT));
                }
                if(JsonHelper.hasString(targetEntry, "loot_table")) {
                    lootTable = Identifier.tryParse(JsonHelper.getString(targetEntry, "loot_table"));
                }
                if(JsonHelper.hasBoolean(targetEntry, "tracked_per_player")) {
                    trackedPerPlayer = JsonHelper.getBoolean(targetEntry, "tracked_per_player");
                }
                if(JsonHelper.hasNumber(targetEntry, "replenish_time_ticks")) {
                    replenishTimeTicks = JsonHelper.getInt(targetEntry, "replenish_time_ticks");
                }
                if(JsonHelper.hasString(targetEntry, "replenish_mode")) {
                    replenishMode = ReplenishMode.valueOf(JsonHelper.getString(targetEntry, "replenish_mode").toUpperCase(Locale.ROOT));
                }
                if(JsonHelper.hasString(targetEntry, "lock_mode")) {
                    lockMode = LockMode.valueOf(JsonHelper.getString(targetEntry, "lock_mode").toUpperCase(Locale.ROOT));
                }
                if(JsonHelper.hasString(targetEntry, "inventory_deletion_mode")) {
                    inventoryDeletionMode = InventoryDeletionMode.valueOf(JsonHelper.getString(targetEntry, "inventory_deletion_mode").toUpperCase(Locale.ROOT));
                }
                if(JsonHelper.hasNumber(targetEntry, "weight")) {
                    weight = JsonHelper.getInt(targetEntry, "weight");
                }
                totalWeight += weight;

                lootCrateEntries.add(new LootCrateReplacementEntry(lootCrateRarity, lootTable, replenishMode, replenishTimeTicks, lockMode, inventoryDeletionMode, trackedPerPlayer, weight));

            }

            WeightedLootCrateEntryList weightedLootCrateEntryList = new WeightedLootCrateEntryList(totalWeight, lootCrateEntries);
            if(lootTableIdentifier == null || lootTableIdentifier.getPath().equals("")) {
                DefaultLootCrateProviderList =  weightedLootCrateEntryList;
            } else {
                LootCrateProviders.put(lootTableIdentifier, weightedLootCrateEntryList);
            }
        }
    }

    public static LootCrateReplacementEntry getEntryForLootTable(Identifier lootTable, Random random) {
        if (LootCrateProviders.containsKey(lootTable)) {
            return LootCrateProviders.get(lootTable).getWeightedRandom(random);
        } else {
            // use default
            return DefaultLootCrateProviderList.getWeightedRandom(random);
        }
    }

    public static void tick(MinecraftServer server) {
        for (int i = 0; i < replacements.size(); i++) {
            LootCrateReplacementPosition replacementPosition = replacements.get(i);
            try {
                replace(replacementPosition);
            } catch (Throwable t) {
                LootCrates.log(Level.ERROR, "Error while replacing a container in the world '" + replacementPosition.world.getRegistryKey().getValue() + "' at '" + replacementPosition.blockPos + "': " + t.getLocalizedMessage());
            }
        }
        replacements.clear();
    }
    
    private static boolean replace(@NotNull LootCrateReplacementPosition replacementPosition) {
        ServerWorld serverWorld = replacementPosition.world;
        ChunkPos chunkPos = new ChunkPos(replacementPosition.blockPos);
        Chunk chunk = serverWorld.getChunkManager().getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);
        
        if (chunk != null) {
            BlockState sourceBlockState = serverWorld.getBlockState(replacementPosition.blockPos);
            BlockEntity blockEntity = serverWorld.getBlockEntity(replacementPosition.blockPos);
            if (sourceBlockState.hasBlockEntity() && blockEntity == null) {
                // chunk not finished generation?
                // => try again later
                return false;
            }
            
            if(blockEntity instanceof LootableContainerBlockEntity && !(blockEntity instanceof LootCrateBlockEntity)) {
                Identifier lootTableIdentifier;
                long lootTableSeed;
                
                LootTableAccessor lootTableAccessor = ((LootTableAccessor) blockEntity);
                lootTableIdentifier = lootTableAccessor.getLootTableIdentifier();
                lootTableSeed = lootTableAccessor.getLootTableSeed();

                serverWorld.removeBlockEntity(replacementPosition.blockPos);

                Block sourceBlock = sourceBlockState.getBlock();
                if (!(sourceBlock instanceof LootCrateBlock)) {
                    boolean trapped = false;
                    LootCrateReplacementEntry replacementTargetData = getEntryForLootTable(lootTableIdentifier, serverWorld.random);

                    if (sourceBlock instanceof ChestBlock) {
                        if (sourceBlock instanceof TrappedChestBlock) {
                            trapped = true;
                        }
                        serverWorld.setBlockState(replacementPosition.blockPos, LootCrateAtlas.getLootCrate(replacementTargetData.lootCrateRarity).getDefaultState().with(ChestLootCrateBlock.FACING, sourceBlockState.get(ChestBlock.FACING)));
                    } else if (sourceBlock instanceof BarrelBlock) {
                        serverWorld.setBlockState(replacementPosition.blockPos, LootCrateAtlas.getLootBarrel(replacementTargetData.lootCrateRarity).getDefaultState().with(Properties.FACING, sourceBlockState.get(Properties.FACING)));
                    } else if (sourceBlock instanceof ShulkerBoxBlock) {
                        serverWorld.setBlockState(replacementPosition.blockPos, LootCrateAtlas.getShulkerCrate(replacementTargetData.lootCrateRarity).getDefaultState().with(Properties.FACING, sourceBlockState.get(Properties.FACING)));
                    } else {
                        // the worldgen may have been replaced by other blocks.
                        // Like a mineshaft cutting into a dungeon, replacing the chest with air again
                        // => do not replace
                        return true;
                    }

                    blockEntity = serverWorld.getBlockEntity(replacementPosition.blockPos);
                    if (blockEntity instanceof LootCrateBlockEntity lootCrateBlockEntity) {
                        if (replacementTargetData.lootTable == null) {
                            // keep the original loot table
                            lootCrateBlockEntity.setLootTable(lootTableIdentifier, lootTableSeed);
                        } else {
                            // overwrite with an alternate loot table
                            lootCrateBlockEntity.setLootTable(replacementTargetData.lootTable, lootTableSeed);
                        }
                        
                        lootCrateBlockEntity.setData(replacementTargetData.replenishMode, replacementTargetData.replenishTimeTicks, replacementTargetData.trackedPerPlayer, replacementTargetData.lockMode, replacementTargetData.inventoryDeletionMode, trapped);
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }
    
}
