package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.blocks.*;
import de.dafuqs.lootcrates.components.*;
import de.dafuqs.lootcrates.config.*;
import de.dafuqs.lootcrates.enums.*;
import de.dafuqs.lootcrates.items.*;
import de.dafuqs.lootcrates.worldgen.*;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.serializer.*;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.block.*;
import net.minecraft.block.dispenser.*;
import net.minecraft.component.type.*;
import net.minecraft.entity.*;
import net.minecraft.item.*;
import net.minecraft.loot.*;
import net.minecraft.loot.context.*;
import net.minecraft.registry.*;
import net.minecraft.server.world.*;
import net.minecraft.sound.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

import java.util.*;

public class LootCrates implements ModInitializer {
	
	public static final String MOD_ID = "lootcrates";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static LootCratesConfig CONFIG;
	
	public static final Identifier CHEST_UNLOCKS_SOUND_ID = new Identifier(MOD_ID, "chest_unlocks");
	public static final SoundEvent CHEST_UNLOCKS_SOUND_EVENT = SoundEvent.of(CHEST_UNLOCKS_SOUND_ID);
	
	public static final DispenserBehavior LOOT_BAG_DISPENSER_BEHAVIOR = (pointer, stack) -> {
		if (stack.getItem() instanceof LootBagItem lootBagItem) {
			@Nullable ContainerLootComponent lootComponent = lootBagItem.getLootComponent(stack);
			if (lootComponent != null) {
				ServerWorld world = pointer.world();
				LootTable lootTable = world.getServer().getReloadableRegistries().getLootTable(lootComponent.lootTable());
				
				LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(pointer.world()).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pointer.pos()));
				List<ItemStack> lootStacks = lootTable.generateLoot(builder.build(LootContextTypes.CHEST), lootComponent.seed());
				Position position = DispenserBlock.getOutputLocation(pointer);
				Direction direction = pointer.state().get(DispenserBlock.FACING);
				
				for (ItemStack lootStack : lootStacks) {
					ItemEntity itemEntity = new ItemEntity(pointer.world(), position.getX(), position.getY(), position.getZ(), lootStack);
					itemEntity.setVelocity(direction.getOffsetX() * 0.2, direction.getOffsetY() * 0.2, direction.getOffsetZ() * 0.2);
					world.spawnEntity(itemEntity);
				}
			}
			stack.decrement(1);
			return stack;
		}
		return stack;
	};
	
	@Override
	public void onInitialize() {
		// Config
		LOGGER.info("Loading config...");
		AutoConfig.register(LootCratesConfig.class, JanksonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(LootCratesConfig.class).getConfig();
		
		LootCrateDataComponentTypes.register();
		
		// All the different types of crates
		LOGGER.info("Loading crate definitions...");
		LootCrateDefinition commonLootCrate = new LootCrateDefinition(LootCrateVariant.COMMON, Rarity.COMMON, MapColor.DIRT_BROWN, 0, false, false);
		LootCrateDefinition uncommonLootCrate = new LootCrateDefinition(LootCrateVariant.UNCOMMON, Rarity.UNCOMMON, MapColor.YELLOW, 0, false, false);
		LootCrateDefinition rareLootCrate = new LootCrateDefinition(LootCrateVariant.RARE, Rarity.RARE, MapColor.BLUE, 0, false, false);
		LootCrateDefinition epicLootCrate = new LootCrateDefinition(LootCrateVariant.EPIC, Rarity.EPIC, MapColor.PURPLE, 6, false, false);
		LootCrateDefinition ghostLootCrate = new LootCrateDefinition(LootCrateVariant.GHOST, Rarity.EPIC, MapColor.GREEN, 0, true, false);
		LootCrateDefinition blazeLootCrate = new LootCrateDefinition(LootCrateVariant.BLAZE, Rarity.EPIC, MapColor.ORANGE, 15, false, true);
		
		epicLootCrate.setCustomSounds(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundEvents.ENTITY_ENDER_DRAGON_FLAP);
		ghostLootCrate.setCustomSounds(SoundEvents.ENTITY_GHAST_AMBIENT, SoundEvents.ENTITY_GHAST_AMBIENT);
		blazeLootCrate.setCustomSounds(SoundEvents.ENTITY_BLAZE_AMBIENT, SoundEvents.ENTITY_BLAZE_SHOOT);
		if (CONFIG.BlazingCratesCauseFire) {
			blazeLootCrate.setScheduledTickEvent(ScheduledTickEvent.FIRE);
		}
		
		LootCrateAtlas.registerLootCrateDefinition(LootCrateVariant.COMMON, commonLootCrate);
		LootCrateAtlas.registerLootCrateDefinition(LootCrateVariant.UNCOMMON, uncommonLootCrate);
		LootCrateAtlas.registerLootCrateDefinition(LootCrateVariant.RARE, rareLootCrate);
		LootCrateAtlas.registerLootCrateDefinition(LootCrateVariant.EPIC, epicLootCrate);
		LootCrateAtlas.registerLootCrateDefinition(LootCrateVariant.GHOST, ghostLootCrate);
		LootCrateAtlas.registerLootCrateDefinition(LootCrateVariant.BLAZE, blazeLootCrate);
		
		// The block entity type
		LootCratesBlockEntityType.register();
		
		LOGGER.info("Registering sounds...");
		Registry.register(Registries.SOUND_EVENT, CHEST_UNLOCKS_SOUND_ID, CHEST_UNLOCKS_SOUND_EVENT);
		
		LOGGER.info("Loading LootCratesWorldgenSettings.json and registering the replacer");
		if (CONFIG.ReplaceVanillaWorldgenChests) {
			ServerTickEvents.END_SERVER_TICK.register(LootCratesWorldgenReplacer::tick);
		}
		
		ServerLifecycleEvents.SERVER_STARTING.register(server -> LootCratesWorldgenReplacer.initialize(server.getOverworld()));
		
		LootCratesItemGroups.register(CONFIG.AddExampleCratesAndBundlesItemGroups);
		
		LOGGER.info("Finished!");
	}
	
}
