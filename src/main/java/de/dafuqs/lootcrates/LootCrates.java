package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.blocks.*;
import de.dafuqs.lootcrates.blocks.modes.*;
import de.dafuqs.lootcrates.config.*;
import de.dafuqs.lootcrates.enums.*;
import de.dafuqs.lootcrates.items.*;
import de.dafuqs.lootcrates.worldgen.*;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.serializer.*;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.itemgroup.v1.*;
import net.minecraft.block.*;
import net.minecraft.block.dispenser.*;
import net.minecraft.entity.*;
import net.minecraft.item.*;
import net.minecraft.loot.*;
import net.minecraft.loot.context.*;
import net.minecraft.nbt.*;
import net.minecraft.registry.*;
import net.minecraft.sound.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import org.apache.logging.log4j.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class LootCrates implements ModInitializer {

    public static final String MOD_ID = "lootcrates";
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static LootCratesConfig CONFIG;

    public static final ItemGroup ITEM_GROUP = FabricItemGroup.builder().displayName(Text.translatable("itemGroup.lootcrates.loot_crates")).icon(() -> new ItemStack(LootCrateAtlas.getLootCrate(LootCrateRarity.COMMON))).entries(new ItemGroup.EntryCollector() {
        @Override
        public void accept(ItemGroup.DisplayContext displayContext, ItemGroup.Entries entries) {
             for(LootCrateRarity rarity : LootCrateRarity.values()) {
                 entries.add(LootCrateAtlas.getLootCrate(rarity));
                 entries.add(LootCrateAtlas.getLootBarrel(rarity));
                 entries.add(LootCrateAtlas.getShulkerCrate(rarity));
                 entries.add(LootCrateAtlas.getLootKeyItem(rarity));
                 entries.add(LootCrateAtlas.getLootBagItem(rarity));
             }
        }
   }).build();

    public static final ItemGroup PREDEFINED_CRATES_GROUP = FabricItemGroup.builder().displayName(Text.translatable("itemGroup.lootcrates.predefined_loot_crates")).icon(() -> new ItemStack(LootCrateAtlas.getLootCrate(LootCrateRarity.EPIC))).entries(new ItemGroup.EntryCollector() {
        @Override
        public void accept(ItemGroup.DisplayContext displayContext, ItemGroup.Entries entries) {
            entries.addAll(getPredefinedLootCrates());
        }
    }).build(); // Icon is set in the tab directly

    public static final ItemGroup PREDEFINED_BAGS_GROUP = FabricItemGroup.builder().displayName(Text.translatable("itemGroup.lootcrates.predefined_loot_bags")).icon(() -> new ItemStack(LootCrateAtlas.getLootBagItem(LootCrateRarity.EPIC))).entries(new ItemGroup.EntryCollector() {
        @Override
        public void accept(ItemGroup.DisplayContext displayContext, ItemGroup.Entries entries) {
            entries.addAll(getPredefinedLootBags());
        }
    }).build(); // Icon is set in the tab directly
    
    /**
     * Generates a default item for a lot of predefined values of itemStacks
     * @return All generated ItemStacks
     */
    private static ArrayList<ItemStack> getPredefinedLootCrates() {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        
        ArrayList<Long> replenishTimeTicksValues = new ArrayList<>();
        replenishTimeTicksValues.add(1L);       // 1 tick
        replenishTimeTicksValues.add(72000L);   // 1 hour
        replenishTimeTicksValues.add(1728000L); // 1 day
        
        ArrayList<Boolean> booleans = new ArrayList<>() {{
            add(false);
            add(true);
        }};
        
        Item lootCrateItem = LootCrateAtlas.getAllCrateItems().get(0);
        Set<Identifier> allLootTables = LootTables.getAll();
        
        for (Identifier lootTable : allLootTables) {
            if(lootTable.getNamespace().equals("minecraft") && lootTable.getPath().startsWith("chests/")) { // to reduce the lists size. These are just examples, after all
                for (LockMode lockMode : LockMode.values()) {
                    for (boolean trackedPerPlayer : booleans) {
                        for (ReplenishMode replenishMode : ReplenishMode.values()) {
                            if(lockMode.relocks() && replenishMode == ReplenishMode.NEVER) {
                                continue; // there is nothing to relock
                            }
                            
                            if(replenishMode.requiresTickData) {
                                for (Long replenishTimeTicks : replenishTimeTicksValues) {
                                    NbtCompound compound = LootCrateItem.getLootCrateItemCompoundTag(lootTable, lockMode, replenishMode, InventoryDeletionMode.NEVER, replenishTimeTicks, trackedPerPlayer, false);
                                    ItemStack itemStack = new ItemStack(lootCrateItem);
                                    itemStack.setNbt(compound);
                                    stacks.add(itemStack);
                                }
                            } else {
                                NbtCompound compound = LootCrateItem.getLootCrateItemCompoundTag(lootTable, lockMode, replenishMode, InventoryDeletionMode.NEVER, 0, trackedPerPlayer, false);
                                ItemStack itemStack = new ItemStack(lootCrateItem);
                                itemStack.setNbt(compound);
                                stacks.add(itemStack);
                            }
                        }
                    }
                }
            }
        }
        
        return stacks;
    }
    
    /**
     * Generates a default item for a lot of predefined values of itemStacks
     * @return All generated ItemStacks
     */
    private static @NotNull ArrayList<ItemStack> getPredefinedLootBags() {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        
        Item lootBagItem = LootCrateAtlas.getAllLootBagItems().get(0);
        Set<Identifier> allLootTables = LootTables.getAll();
        
        for (Identifier lootTable : allLootTables) {
            if(lootTable.getNamespace().equals("minecraft") && lootTable.getPath().startsWith("chests/")) { // to reduce the lists size
                NbtCompound compound = LootBagItem.getItemCompoundTag(lootTable, 0);
                ItemStack itemStack = new ItemStack(lootBagItem);
                itemStack.setNbt(compound);
                stacks.add(itemStack);
            }
        }
        
        return stacks;
    }
    
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

        Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "loot_crates"), ITEM_GROUP);
        Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "predefined_loot_crates"), PREDEFINED_CRATES_GROUP);
        Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "predefined_loot_bags"), PREDEFINED_BAGS_GROUP);
        
        log(Level.INFO, "Finished!");
    }
    
    public static void log(Level logLevel, String message) {
        LOGGER.log(logLevel, "[LootCrates] " + message);
    }
    
}
