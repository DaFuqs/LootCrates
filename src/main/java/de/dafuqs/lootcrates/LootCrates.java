package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.blocks.LootCratesBlocks;
import de.dafuqs.lootcrates.items.LootCratesItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.loader.launch.common.FabricLauncher;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class LootCrates implements ModInitializer {

    public static final String MOD_ID = "lootcrates";

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "loot_crates"),
            () -> new ItemStack(Blocks.COBBLESTONE));

    @Override
    public void onInitialize() {
        LootCratesBlocks.initialize();
        LootCratesItems.initialize();
    }

}
