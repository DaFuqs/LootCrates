package de.dafuqs.lootcrates.worldgen;

import com.google.gson.*;
import de.dafuqs.lootcrates.LootCrateAtlas;
import de.dafuqs.lootcrates.LootCrates;
import de.dafuqs.lootcrates.blocks.LootCrateBlock;
import de.dafuqs.lootcrates.blocks.LootCrateBlockEntity;
import de.dafuqs.lootcrates.blocks.chest.ChestLootCrateBlock;
import de.dafuqs.lootcrates.enums.LootCrateRarity;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.logging.log4j.Level;

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
				"once_per_player": true,
				"replenish_time_ticks": 1,
				"lock": "none",
				"weight": 1
			}
		]
	},
	{
		"loot_table": "minecraft:example_loot_table",
		"entries": [
			{
				"crate_rarity": "common",
				"once_per_player": true,
				"replenish_time_ticks": 1,
				"lock": "require_key",
				"weight": 3
			},
			{
				"crate_rarity": "uncommon",
				"loot_table": "minecraft:example_loot_table_uncommon",
				"once_per_player": true,
				"replenish_time_ticks": 1,
				"lock": "consume_key",
				"weight": 2
			},
			{
				"crate_rarity": "rare",
				"loot_table": "minecraft:example_loot_table_rare",
				"once_per_player": true,
				"replenish_time_ticks": 1,
				"lock": "consume_key",
				"weight": 1
			}
		]
	},
	{
		"loot_table": "minecraft:chests/bastion_bridge",
		"entries": [{
			"crate_rarity": "blaze",
			"once_per_player": true,
			"replenish_time_ticks": 1
		}]
	},
	{
		"loot_table": "minecraft:chests/bastion_bridge",
		"entries": [{
			"crate_rarity": "blaze",
			"once_per_player": true,
			"replenish_time_ticks": 1
		}]
	},
	{
		"loot_table": "minecraft:chests/bastion_hoglin_stable",
		"entries": [{
			"crate_rarity": "blaze",
			"once_per_player": true,
			"replenish_time_ticks": 1
		}]
	},
	{
		"loot_table": "minecraft:chests/bastion_other",
		"entries": [{
			"crate_rarity": "blaze",
			"once_per_player": true,
			"replenish_time_ticks": 1
		}]
	},
	{
		"loot_table": "minecraft:chests/bastion_treasure",
		"entries": [{
			"crate_rarity": "blaze",
			"once_per_player": true,
			"replenish_time_ticks": 1
		}]
	},
	{
		"loot_table": "minecraft:chests/buried_treasure",
		"entries": [{
			"crate_rarity": "rare",
			"once_per_player": true,
			"replenish_time_ticks": 1
		}]
	},
	{
		"loot_table": "minecraft:chests/end_city_treasure",
		"entries": [{
			"crate_rarity": "epic",
			"once_per_player": true,
			"replenish_time_ticks": 1
		}]
	},
	{
		"loot_table": "minecraft:chests/simple_dungeon",
		"entries": [{
			"crate_rarity": "uncommon",
			"once_per_player": true,
			"replenish_time_ticks": 1
		}]
	},
	{
		"loot_table": "minecraft:chests/stronghold_corridor",
		"entries": [{
			"crate_rarity": "uncommon",
			"once_per_player": true,
			"replenish_time_ticks": 1
		}]
	},
	{
		"loot_table": "minecraft:chests/stronghold_corridor",
		"entries": [{
			"crate_rarity": "uncommon",
			"once_per_player": true,
			"replenish_time_ticks": 1
		}]
	},
	{
		"loot_table": "minecraft:chests/stronghold_corridor",
		"entries": [{
			"crate_rarity": "uncommon",
			"once_per_player": true,
			"replenish_time_ticks": 1
		}]
	},
	{
		"loot_table": "minecraft:chests/underwater_ruin_big",
		"entries": [{
			"crate_rarity": "rare",
			"once_per_player": true,
			"replenish_time_ticks": 1
		}]
	},
	{
		"loot_table": "minecraft:chests/underwater_ruin_small",
		"entries": [{
			"crate_rarity": "rare",
			"once_per_player": true,
			"replenish_time_ticks": 1
		}]
	},
	{
		"loot_table": "minecraft:chests/woodland_mansion",
		"entries": [{
			"crate_rarity": "ghost",
			"once_per_player": true,
			"replenish_time_ticks": 1
		}]
	}
]""";

    public static List<LootCrateReplacementPosition> replacements = new ArrayList<>();

    private static WeightedLootCrateEntryList DefaultLootCrateProviderList = new WeightedLootCrateEntryList(1, new ArrayList<>() {{
        add(new LootCrateReplacementEntry(null, null, true, 1, LockType.NONE, 1));
    }});
    private static final Map<Identifier, WeightedLootCrateEntryList> LootCrateProviders = new HashMap<>();

    public static void initialize() {
        File configFile = new File(FabricLoader.INSTANCE.getConfigDirectory(), "LootCratesWorldgenSettings.json5");
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
                LootCrates.LOGGER.log(Level.ERROR, "[LootCrates] Could not generate config file under " + configFile.getAbsolutePath() + ".\n" + ioException.getLocalizedMessage());
                return;
            }
        }

        JsonElement jsonElement;
        try {
            jsonElement = (JsonElement) JsonHelper.deserialize(GSON, configReader, (Class) JsonElement.class);
        } catch (Exception e) {
            LootCrates.LOGGER.error("[LootCrates] Could not parse the LootCratesWorldgenSettings.json5: " + e.getLocalizedMessage());
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
                boolean oncePerPlayer = false;
                int replenishTimeTicks = 0;
                LockType lockType = LockType.NONE;
                int weight = 1;

                if(JsonHelper.hasString(targetEntry, "crate_rarity")) {
                    lootCrateRarity = LootCrateRarity.valueOf(JsonHelper.getString(targetEntry, "crate_rarity").toUpperCase(Locale.ROOT));
                }
                if(JsonHelper.hasString(targetEntry, "loot_table")) {
                    lootTable = Identifier.tryParse(JsonHelper.getString(targetEntry, "loot_table"));
                }
                if(JsonHelper.hasBoolean(targetEntry, "once_per_player")) {
                    oncePerPlayer = JsonHelper.getBoolean(targetEntry, "once_per_player");
                }
                if(JsonHelper.hasNumber(targetEntry, "replenish_time_ticks")) {
                    replenishTimeTicks = JsonHelper.getInt(targetEntry, "replenish_time_ticks");
                }
                if(JsonHelper.hasString(targetEntry, "lock")) {
                    lockType = LockType.valueOf(JsonHelper.getString(targetEntry, "lock").toUpperCase(Locale.ROOT));
                }
                if(JsonHelper.hasNumber(targetEntry, "weight")) {
                    weight = JsonHelper.getInt(targetEntry, "weight");
                }
                totalWeight += weight;

                lootCrateEntries.add(new LootCrateReplacementEntry(lootCrateRarity, lootTable, oncePerPlayer, replenishTimeTicks, lockType, weight));

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
        if (!replacements.isEmpty()) {

            // Some protection against concurrent modifications
            List<LootCrateReplacementPosition> list = new ArrayList<>(replacements);
            replacements.clear();

            for (LootCrateReplacementPosition replacementPosition : list) {
                try {
                    ServerWorld serverWorld = server.getWorld(replacementPosition.worldKey);
                    if (serverWorld != null && serverWorld.isChunkLoaded(replacementPosition.blockPos)) {
                        BlockEntity blockEntity;
                        try {
                            blockEntity = serverWorld.getBlockEntity(replacementPosition.blockPos);
                        } catch (Exception e) {
                            LootCrates.LOGGER.error("[LootCrates] Error while replacing a container with loot table '" + replacementPosition.lootTable + "' in the world '" + replacementPosition.worldKey + "' at '" + replacementPosition.blockPos + "' ) + " + e.getLocalizedMessage());
                            continue;
                        }
                        if(blockEntity != null && !(blockEntity instanceof LootCrateBlockEntity)) {
                            serverWorld.removeBlockEntity(replacementPosition.blockPos);
                        }

                        BlockState sourceBlockState = serverWorld.getBlockState(replacementPosition.blockPos);
                        Block sourceBlock = sourceBlockState.getBlock();

                        boolean trapped = false;
                        if(!(sourceBlock instanceof LootCrateBlock)) {
                            LootCrateReplacementEntry replacementTargetData = getEntryForLootTable(replacementPosition.lootTable, new Random(replacementPosition.lootTableSeed));

                            if (sourceBlock instanceof ChestBlock) {
                                if (sourceBlock instanceof TrappedChestBlock) {
                                    trapped = true;
                                }
                                serverWorld.setBlockState(replacementPosition.blockPos, LootCrateAtlas.getLootCrate(replacementTargetData.lootCrateRarity).getDefaultState().with(ChestLootCrateBlock.FACING, sourceBlockState.get(ChestBlock.FACING)), 3);
                            } else if (sourceBlock instanceof BarrelBlock) {
                                serverWorld.setBlockState(replacementPosition.blockPos, LootCrateAtlas.getLootBarrel(replacementTargetData.lootCrateRarity).getDefaultState().with(net.minecraft.state.property.Properties.FACING, sourceBlockState.get(Properties.FACING)), 3);
                            } else if (sourceBlock instanceof ShulkerBoxBlock) {
                                serverWorld.setBlockState(replacementPosition.blockPos, LootCrateAtlas.getShulkerCrate(replacementTargetData.lootCrateRarity).getDefaultState().with(net.minecraft.state.property.Properties.FACING, sourceBlockState.get(Properties.FACING)), 3);
                            } else {
                                // the worldgen may have been replaced by other blocks.
                                // Like a mineshaft cutting into a dungeon, replacing the chest with air again
                                // => do not replace
                                continue;
                            }

                            blockEntity = serverWorld.getBlockEntity(replacementPosition.blockPos);
                            if (blockEntity instanceof LootCrateBlockEntity lootCrateBlockEntity) {
                                if(replacementTargetData.lootTable == null) {
                                    // keep the original loot table
                                    lootCrateBlockEntity.setLootTable(replacementPosition.lootTable, replacementPosition.lootTableSeed);
                                } else {
                                    // overwrite with an alternate loot table
                                    lootCrateBlockEntity.setLootTable(replacementTargetData.lootTable, replacementPosition.lootTableSeed);
                                }
                                if(replacementTargetData.oncePerPlayer) {
                                    lootCrateBlockEntity.setOncePerPlayer(true);
                                }

                                lootCrateBlockEntity.setLock(replacementTargetData.lockType);
                                lootCrateBlockEntity.setReplenishTimeTicks(replacementTargetData.replenishTimeTicks);

                                if (trapped) {
                                    lootCrateBlockEntity.setTrapped(true);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LootCrates.LOGGER.error("[LootCrates] Error while replacing a container with loot table '" + replacementPosition.lootTable + "' in the world '" + replacementPosition.worldKey + "' at '" + replacementPosition.blockPos + "') + " + e.getLocalizedMessage());
                }
            }
        }
    }

}
