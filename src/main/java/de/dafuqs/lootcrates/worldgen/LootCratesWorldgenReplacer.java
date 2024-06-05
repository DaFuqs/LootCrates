package de.dafuqs.lootcrates.worldgen;

import com.google.gson.*;
import de.dafuqs.lootcrates.*;
import de.dafuqs.lootcrates.blocks.*;
import de.dafuqs.lootcrates.blocks.chest.*;
import de.dafuqs.lootcrates.components.*;
import de.dafuqs.lootcrates.config.*;
import de.dafuqs.lootcrates.enums.*;
import de.dafuqs.lootcrates.mixin.*;
import net.fabricmc.loader.api.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.component.*;
import net.minecraft.loot.*;
import net.minecraft.registry.*;
import net.minecraft.server.*;
import net.minecraft.server.world.*;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.chunk.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;

public class LootCratesWorldgenReplacer {
	
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final String DEFAULT_CONFIG = """
			[
				{
					"loot_table": "",
					"entries": [
						{
							"crate_variant": "common",
							"tracked_per_player": true,
							"lock_mode": "not_locked",
							"inventory_deletion_mode": "never",
							"replenish_time_scale": "never",
							"weight": 5
						},
						{
							"crate_variant": "epic",
							"tracked_per_player": true,
							"lock_mode": "not_locked",
							"inventory_deletion_mode": "never",
							"replenish_time_scale": "never",
							"weight": 1
						}
					]
				},
				{
					"loot_table": "minecraft:chests/bastion_bridge",
					"entries": [{
						"crate_variant": "blaze",
						"tracked_per_player": true,
						"lock_mode": "not_locked",
						"inventory_deletion_mode": "never",
						"replenish_time_scale": "never"
					}]
				},
				{
					"loot_table": "minecraft:chests/bastion_hoglin_stable",
					"entries": [{
						"crate_variant": "blaze",
						"tracked_per_player": true,
						"lock_mode": "not_locked",
						"inventory_deletion_mode": "never",
						"replenish_time_scale": "never"
					}]
				},
				{
					"loot_table": "minecraft:chests/bastion_other",
					"entries": [{
						"crate_variant": "blaze",
						"tracked_per_player": true,
						"lock_mode": "not_locked",
						"inventory_deletion_mode": "never",
						"replenish_time_scale": "never"
					}]
				},
				{
					"loot_table": "minecraft:chests/bastion_treasure",
					"entries": [{
						"crate_variant": "blaze",
						"tracked_per_player": true,
						"lock_mode": "not_locked",
						"inventory_deletion_mode": "never",
						"replenish_time_scale": "never"
					}]
				},
				{
					"loot_table": "minecraft:chests/buried_treasure",
					"entries": [{
						"crate_variant": "rare",
						"tracked_per_player": true,
						"lock_mode": "not_locked",
						"inventory_deletion_mode": "never",
						"replenish_time_scale": "never"
					}]
				},
				{
					"loot_table": "minecraft:chests/end_city_treasure",
					"entries": [{
						"crate_variant": "epic",
						"tracked_per_player": true,
						"lock_mode": "not_locked",
						"inventory_deletion_mode": "never",
						"replenish_time_scale": "never"
					}]
				},
				{
					"loot_table": "minecraft:chests/simple_dungeon",
					"entries": [{
						"crate_variant": "uncommon",
						"tracked_per_player": true,
						"lock_mode": "not_locked",
						"inventory_deletion_mode": "never",
						"replenish_time_scale": "never"
					}]
				},
				{
					"loot_table": "minecraft:chests/stronghold_corridor",
					"entries": [{
						"crate_variant": "uncommon",
						"tracked_per_player": true,
						"lock_mode": "not_locked",
						"inventory_deletion_mode": "never",
						"replenish_time_scale": "never"
					}]
				},
				{
					"loot_table": "minecraft:chests/underwater_ruin_big",
					"entries": [{
						"crate_variant": "rare",
						"tracked_per_player": true,
						"lock_mode": "not_locked",
						"inventory_deletion_mode": "never",
						"replenish_time_scale": "never"
					}]
				},
				{
					"loot_table": "minecraft:chests/underwater_ruin_small",
					"entries": [{
						"crate_variant": "rare",
						"tracked_per_player": true,
						"lock_mode": "not_locked",
						"inventory_deletion_mode": "never",
						"replenish_time_scale": "never"
					}]
				},
				{
					"loot_table": "minecraft:chests/woodland_mansion",
					"entries": [{
						"crate_variant": "ghost",
						"tracked_per_player": true,
						"lock_mode": "not_locked",
						"inventory_deletion_mode": "never",
						"replenish_time_scale": "never"
					}]
				}
			]""";
	
	
	public static final List<LootCrateReplacementPosition> replacements = new ArrayList<>(); // try at the end of each tick
	
	private static WeightedLootCrateEntryList DefaultLootCrateProviderList = new WeightedLootCrateEntryList(1, new ArrayList<>() {{
		add(new LootCrateReplacementEntry(null, null, ReplenishTimeScale.TICKS, 1, LockMode.NOT_LOCKED, InventoryDeletionMode.NEVER, true, 1));
	}});
	private static final Map<RegistryKey<LootTable>, WeightedLootCrateEntryList> LootCrateProviders = new HashMap<>();
	
	public static void initialize(ServerWorld world) {
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
				LootCrates.LOGGER.error("Could not generate config file under {}.\n{}", configFile.getAbsolutePath(), ioException.getLocalizedMessage());
				return;
			}
		}
		
		JsonElement jsonElement;
		try {
			jsonElement = JsonHelper.deserialize(GSON, configReader, JsonElement.class);
		} catch (Exception e) {
			LootCrates.LOGGER.error("Could not parse the LootCratesWorldgenSettings.json5: {}", e.getLocalizedMessage());
			return;
		}
		
		JsonArray array = jsonElement.getAsJsonArray();
		for (int i = 0; i < array.size(); i++) {
			JsonObject currentEntry = (JsonObject) array.get(i);
			
			Identifier lootTableIdentifier = Identifier.tryParse(JsonHelper.getString(currentEntry, "loot_table"));
			JsonArray weightedEntries = JsonHelper.getArray(currentEntry, "entries");
			int totalWeight = 0;
			List<LootCrateReplacementEntry> lootCrateEntries = new ArrayList<>();
			
			try {
				for (int j = 0; j < weightedEntries.size(); j++) {
					JsonObject targetEntry = (JsonObject) weightedEntries.get(j);
					
					LootCrateVariant lootCrateVariant = LootCrateVariant.COMMON;
					Identifier lootTable = null; // null => keep the original loot table
					boolean trackedPerPlayer = false;
					int replenishTimeTicks = -1;
					LockMode lockMode = LockMode.NOT_LOCKED;
					ReplenishTimeScale replenishTimeScale = ReplenishTimeScale.NEVER;
					InventoryDeletionMode inventoryDeletionMode = InventoryDeletionMode.NEVER;
					int weight = 1;
					
					if (JsonHelper.hasString(targetEntry, "crate_variant")) {
						lootCrateVariant = LootCrateVariant.valueOf(JsonHelper.getString(targetEntry, "crate_variant").toUpperCase(Locale.ROOT));
					}
					if (JsonHelper.hasString(targetEntry, "loot_table")) {
						lootTable = Identifier.tryParse(JsonHelper.getString(targetEntry, "loot_table"));
					}
					if (JsonHelper.hasBoolean(targetEntry, "tracked_per_player")) {
						trackedPerPlayer = JsonHelper.getBoolean(targetEntry, "tracked_per_player");
					}
					if (JsonHelper.hasNumber(targetEntry, "replenish_time_ticks")) {
						replenishTimeTicks = JsonHelper.getInt(targetEntry, "replenish_time_ticks");
					}
					if (JsonHelper.hasString(targetEntry, "replenish_time_scale")) {
						replenishTimeScale = ReplenishTimeScale.valueOf(JsonHelper.getString(targetEntry, "replenish_time_scale").toUpperCase(Locale.ROOT));
					}
					if (JsonHelper.hasString(targetEntry, "lock_mode")) {
						lockMode = LockMode.valueOf(JsonHelper.getString(targetEntry, "lock_mode").toUpperCase(Locale.ROOT));
					}
					if (JsonHelper.hasString(targetEntry, "inventory_deletion_mode")) {
						inventoryDeletionMode = InventoryDeletionMode.valueOf(JsonHelper.getString(targetEntry, "inventory_deletion_mode").toUpperCase(Locale.ROOT));
					}
					if (JsonHelper.hasNumber(targetEntry, "weight")) {
						weight = JsonHelper.getInt(targetEntry, "weight");
					}
					totalWeight += weight;
					
					
					lootCrateEntries.add(new LootCrateReplacementEntry(lootCrateVariant, RegistryKey.of(RegistryKeys.LOOT_TABLE, lootTable), replenishTimeScale, replenishTimeTicks, lockMode, inventoryDeletionMode, trackedPerPlayer, weight));
				}
			} catch (Exception e) {
				LootCrates.LOGGER.error("Error while reading worldgen replacement config file: {}", e.getLocalizedMessage());
			}
			
			WeightedLootCrateEntryList weightedLootCrateEntryList = new WeightedLootCrateEntryList(totalWeight, lootCrateEntries);
			if (lootTableIdentifier == null || lootTableIdentifier.getPath().isEmpty()) {
				DefaultLootCrateProviderList = weightedLootCrateEntryList;
			} else {
				LootCrateProviders.put(RegistryKey.of(RegistryKeys.LOOT_TABLE, lootTableIdentifier), weightedLootCrateEntryList);
			}
		}
	}
	
	public static LootCrateReplacementEntry getEntryForLootTable(RegistryKey<LootTable> lootTable, Random random) {
		if (LootCrateProviders.containsKey(lootTable)) {
			return LootCrateProviders.get(lootTable).getWeightedRandom(random);
		} else {
			// use default
			return DefaultLootCrateProviderList.getWeightedRandom(random);
		}
	}
	
	public static void tick(MinecraftServer server) {
		for (LootCrateReplacementPosition replacementPosition : replacements) {
			try {
				replace(replacementPosition);
			} catch (Throwable t) {
				LootCrates.LOGGER.error("Error while replacing a container in the world '{}' at '{}': {}", replacementPosition.world().getRegistryKey().getValue(), replacementPosition.blockPos(), t.getLocalizedMessage());
			}
		}
		replacements.clear();
	}
	
	private static void replace(@NotNull LootCrateReplacementPosition replacementPosition) {
		ServerWorld serverWorld = replacementPosition.world();
		BlockPos pos = replacementPosition.blockPos();
		ChunkPos chunkPos = new ChunkPos(pos);
		Chunk chunk = serverWorld.getChunkManager().getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);
		
		if (chunk == null) {
			return;
		}
		
		BlockState sourceState = serverWorld.getBlockState(pos);
		BlockEntity blockEntity = serverWorld.getBlockEntity(pos);
		if (sourceState.hasBlockEntity() && blockEntity == null) {
			// chunk not finished generation?
			// => try again later
			return;
		}
		
		if (blockEntity instanceof LootableContainerBlockEntity && !(blockEntity instanceof LootCrateBlockEntity)) {
			RegistryKey<LootTable> lootTable;
			long lootTableSeed;
			
			LootTableAccessor lootTableAccessor = ((LootTableAccessor) blockEntity);
			lootTable = lootTableAccessor.getLootTable();
			lootTableSeed = lootTableAccessor.getLootTableSeed();
			
			serverWorld.removeBlockEntity(pos);
			
			Block sourceBlock = sourceState.getBlock();
			if (!(sourceBlock instanceof LootCrateBlock)) {
				boolean trapped = false;
				LootCrateReplacementEntry replacementTargetData = getEntryForLootTable(lootTable, serverWorld.random);
				
				if (sourceBlock instanceof ChestBlock) {
					if (sourceBlock instanceof TrappedChestBlock) {
						trapped = true;
					}
					serverWorld.setBlockState(pos, LootCrateAtlas.getLootCrate(replacementTargetData.lootCrateVariant).getDefaultState().with(ChestLootCrateBlock.FACING, sourceState.get(ChestBlock.FACING)));
				} else if (sourceBlock instanceof BarrelBlock) {
					serverWorld.setBlockState(pos, LootCrateAtlas.getLootBarrel(replacementTargetData.lootCrateVariant).getDefaultState().with(Properties.FACING, sourceState.get(Properties.FACING)));
				} else if (sourceBlock instanceof ShulkerBoxBlock) {
					serverWorld.setBlockState(pos, LootCrateAtlas.getShulkerCrate(replacementTargetData.lootCrateVariant).getDefaultState().with(Properties.FACING, sourceState.get(Properties.FACING)));
				} else {
					// the worldgen may have been replaced by other blocks.
					// Like a mineshaft cutting into a dungeon, replacing the chest with air again
					// => do not replace
					return;
				}
				
				blockEntity = serverWorld.getBlockEntity(pos);
				if (blockEntity instanceof LootCrateBlockEntity lootCrateBlockEntity) {
					if (replacementTargetData.lootTable == null) {
						// keep the original loot table
						lootCrateBlockEntity.setLootTable(lootTable, lootTableSeed);
					} else {
						// overwrite with an alternate loot table
						lootCrateBlockEntity.setLootTable(replacementTargetData.lootTable, lootTableSeed);
					}
					
					ComponentMap components = lootCrateBlockEntity.getComponents();
					LootCrateLootComponent lootComponent = new LootCrateLootComponent(lootTable, lootTableSeed, replacementTargetData.lockMode, replacementTargetData.replenishTimeScale,
							replacementTargetData.replenishTimeTicks, replacementTargetData.inventoryDeletionMode, replacementTargetData.trackedPerPlayer, trapped);

					ComponentMap.Builder builder = ComponentMap.builder().addAll(components).add(LootCrateDataComponentTypes.LOOT_CRATE_LOOT, lootComponent);
					lootCrateBlockEntity.setComponents(builder.build());
					lootCrateBlockEntity.markDirty();
				}
			}
		}
	}
	
}
