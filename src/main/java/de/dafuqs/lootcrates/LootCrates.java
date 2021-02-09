package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.blocks.LootCratesBlockEntityType;
import de.dafuqs.lootcrates.enums.LootCrateRarity;
import de.dafuqs.lootcrates.enums.ScheduledTickEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
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
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.COMMON, Rarity.COMMON, MaterialColor.WHITE, 0, false, false, ScheduledTickEvent.NONE);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.UNCOMMON, Rarity.UNCOMMON, MaterialColor.YELLOW, 0, false, false, ScheduledTickEvent.NONE);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.RARE, Rarity.RARE, MaterialColor.BLUE, 0, false, false, ScheduledTickEvent.NONE);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.EPIC, Rarity.EPIC, MaterialColor.PURPLE, 6,false, false, ScheduledTickEvent.NONE, SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundEvents.ENTITY_ENDER_DRAGON_FLAP);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.GHOST, Rarity.EPIC, MaterialColor.GREEN, 0, true, false, ScheduledTickEvent.NONE);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.BLAZE, Rarity.EPIC, MaterialColor.LAVA, 15, false, true, ScheduledTickEvent.FIRE, SoundEvents.ENTITY_BLAZE_AMBIENT, SoundEvents.ENTITY_BLAZE_SHOOT);

        LootCratesBlockEntityType.register();

        new PredefinedLootCratesItemGroup(); // add it to creative menu
    }

}
