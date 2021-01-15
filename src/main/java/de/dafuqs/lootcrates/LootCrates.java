package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.blocks.LootCratesBlockEntityType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;

public class LootCrates implements ModInitializer {

    public static final String MOD_ID = "lootcrates";

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "loot_crates"),
            () -> new ItemStack(LootCratesBlocks.EPIC_CHEST_LOOT_CRATE));

    public static final ItemGroup PREDEFINED_ITEM_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "predefined_loot_crates"),
            () -> new ItemStack(LootCratesBlocks.EPIC_SHULKER_LOOT_CRATE));

    @Override
    public void onInitialize() {
        LootCratesBlocks.register();
        LootCratesItems.register();
        LootCratesBlockEntityType.register();

        new PredefinedLootCratesItemGroup(); // add it to creative menu
    }

}
