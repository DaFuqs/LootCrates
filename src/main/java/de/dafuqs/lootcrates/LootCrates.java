package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.blocks.*;
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
import net.minecraft.entity.*;
import net.minecraft.item.*;
import net.minecraft.loot.*;
import net.minecraft.loot.context.*;
import net.minecraft.registry.*;
import net.minecraft.sound.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import org.apache.logging.log4j.*;

import java.util.*;

public class LootCrates implements ModInitializer {

    public static final String MOD_ID = "lootcrates";
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static LootCratesConfig CONFIG;

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "loot_crates"),
            () -> new ItemStack(LootCrateAtlas.getLootCrate(LootCrateRarity.COMMON)));

    public static final ItemGroup PREDEFINED_CRATES_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "predefined_loot_crates"),
            () -> new ItemStack(Items.AIR)); // Is set in the tab directly

    public static final ItemGroup PREDEFINED_BAGS_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "predefined_loot_bags"),
            () -> new ItemStack(Items.AIR)); // Is set in the tab directly

    public static final Identifier CHEST_UNLOCKS_SOUND_ID = new Identifier(MOD_ID, "chest_unlocks");
    public static SoundEvent CHEST_UNLOCKS_SOUND_EVENT = SoundEvent.of(CHEST_UNLOCKS_SOUND_ID);

    public static DispenserBehavior LOOT_BAG_DISPENSER_BEHAVIOR = (pointer, stack) -> {
        if(stack.getItem() instanceof LootBagItem lootBagItem) {
            Identifier lootTableId = lootBagItem.getLootTableIdentifier(stack);
            if (lootTableId != null) {
                long lootTableSeed = lootBagItem.getLootTableSeed(stack);

                LootTable lootTable = pointer.getWorld().getServer().getLootManager().getLootTable(lootTableId);
                
                LootContextParameterSet.Builder builder = (new LootContextParameterSet.Builder(pointer.getWorld())).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pointer.getPos()));
                List<ItemStack> lootStacks = lootTable.generateLoot(builder.build(LootContextTypes.CHEST), lootTableSeed);
                Position position = DispenserBlock.getOutputLocation(pointer);
                Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);

                for (ItemStack lootStack : lootStacks) {
                    ItemEntity itemEntity = new ItemEntity(pointer.getWorld(), position.getX(), position.getY(), position.getZ(), lootStack);
                    itemEntity.setVelocity(direction.getOffsetX() * 0.2, direction.getOffsetY() * 0.2, direction.getOffsetZ() * 0.2);
                    pointer.getWorld().spawnEntity(itemEntity);
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
        log(Level.INFO, "Loading config...");
        AutoConfig.register(LootCratesConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(LootCratesConfig.class).getConfig();

        // All the different types of crates
        log(Level.INFO, "Loading crate definitions...");
        LootCrateDefinition commonLootCrate = new LootCrateDefinition(LootCrateRarity.COMMON, Rarity.COMMON, MapColor.DIRT_BROWN, 0, false, false);
        LootCrateDefinition uncommonLootCrate = new LootCrateDefinition(LootCrateRarity.UNCOMMON, Rarity.UNCOMMON, MapColor.YELLOW, 0, false, false);
        LootCrateDefinition rareLootCrate = new LootCrateDefinition(LootCrateRarity.RARE, Rarity.RARE, MapColor.BLUE, 0, false, false);
        LootCrateDefinition epicLootCrate = new LootCrateDefinition(LootCrateRarity.EPIC, Rarity.EPIC, MapColor.PURPLE, 6,false, false);
        LootCrateDefinition ghostLootCrate = new LootCrateDefinition(LootCrateRarity.GHOST, Rarity.EPIC, MapColor.GREEN, 0, true, false);
        LootCrateDefinition blazeLootCrate = new LootCrateDefinition(LootCrateRarity.BLAZE, Rarity.EPIC, MapColor.ORANGE, 15, false, true);

        epicLootCrate.setCustomSounds(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundEvents.ENTITY_ENDER_DRAGON_FLAP);
        ghostLootCrate.setCustomSounds(SoundEvents.ENTITY_GHAST_AMBIENT, SoundEvents.ENTITY_GHAST_AMBIENT);
        blazeLootCrate.setCustomSounds(SoundEvents.ENTITY_BLAZE_AMBIENT, SoundEvents.ENTITY_BLAZE_SHOOT);
        if(CONFIG.BlazingCratesCauseFire) {
            blazeLootCrate.setScheduledTickEvent(ScheduledTickEvent.FIRE);
        }

        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.COMMON, commonLootCrate);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.UNCOMMON, uncommonLootCrate);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.RARE, rareLootCrate);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.EPIC, epicLootCrate);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.GHOST, ghostLootCrate);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.BLAZE, blazeLootCrate);

        // The block entity type
        LootCratesBlockEntityType.register();
    
        log(Level.INFO, "Registering sounds...");
        Registry.register(Registries.SOUND_EVENT, CHEST_UNLOCKS_SOUND_ID, CHEST_UNLOCKS_SOUND_EVENT);
    
        log(Level.INFO, "Loading LootCratesWorldgenSettings.json and registering the replacer");
        LootCratesWorldgenReplacer.initialize();
        if(CONFIG.ReplaceVanillaWorldgenChests) {
            ServerTickEvents.END_SERVER_TICK.register(LootCratesWorldgenReplacer::tick);
        }

        log(Level.INFO, "Finished!");
    }
    
    public static void log(Level logLevel, String message) {
        LOGGER.log(logLevel, "[LootCrates] " + message);
    }
    
}
