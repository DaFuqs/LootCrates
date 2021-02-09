package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.blocks.LootCratesBlockEntityType;
import de.dafuqs.lootcrates.enums.LootCrateRarity;
import de.dafuqs.lootcrates.enums.ScheduledTickEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class LootCrates implements ModInitializer {

    public static final String MOD_ID = "lootcrates";

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "loot_crates"),
            () -> new ItemStack(LootCrateAtlas.getLootCrate(LootCrateRarity.EPIC)));

    public static final ItemGroup PREDEFINED_ITEM_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "predefined_loot_crates"),
            () -> new ItemStack(LootCrateAtlas.getShulkerCrate(LootCrateRarity.EPIC)));

    @Override
    public void onInitialize() {
        // All the different types of crates
        LootCrateDefinition commonLootCrate = new LootCrateDefinition(LootCrateRarity.COMMON, Rarity.COMMON, MaterialColor.WOOD, 0, false, false);
        LootCrateDefinition uncommonLootCrate = new LootCrateDefinition(LootCrateRarity.UNCOMMON, Rarity.UNCOMMON, MaterialColor.YELLOW, 0, false, false);
        LootCrateDefinition rareLootCrate = new LootCrateDefinition(LootCrateRarity.RARE, Rarity.RARE, MaterialColor.BLUE, 0, false, false);
        LootCrateDefinition epicLootCrate = new LootCrateDefinition(LootCrateRarity.EPIC, Rarity.EPIC, MaterialColor.PURPLE, 6,false, false);
        LootCrateDefinition ghostLootCrate = new LootCrateDefinition(LootCrateRarity.GHOST, Rarity.EPIC, MaterialColor.GREEN, 0, true, false);
        LootCrateDefinition blazeLootCrate = new LootCrateDefinition(LootCrateRarity.BLAZE, Rarity.EPIC, MaterialColor.LAVA, 15, false, true);

        epicLootCrate.setCustomSounds(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundEvents.ENTITY_ENDER_DRAGON_FLAP);
        ghostLootCrate.setCustomSounds(SoundEvents.ENTITY_GHAST_SHOOT, SoundEvents.ENTITY_GHAST_AMBIENT);
        blazeLootCrate.setCustomSounds(SoundEvents.ENTITY_BLAZE_AMBIENT, SoundEvents.ENTITY_BLAZE_SHOOT);
        blazeLootCrate.setScheduledTickEvent(ScheduledTickEvent.FIRE);

        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.COMMON, commonLootCrate);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.UNCOMMON, uncommonLootCrate);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.RARE, rareLootCrate);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.EPIC, epicLootCrate);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.GHOST, ghostLootCrate);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.BLAZE, blazeLootCrate);

        // The block entity type
        LootCratesBlockEntityType.register();

        // Add the item group to the creative menu
        new PredefinedLootCratesItemGroup();
    }

}
