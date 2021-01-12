package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.blocks.LootCratesBlockEntityType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class LootCrates implements ModInitializer {

    public static final String MOD_ID = "lootcrates";

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "loot_crates"),
            () -> new ItemStack(LootCratesBlocks.EPIC_CHEST_LOOT_CRATE));

    @Override
    public void onInitialize() {
        LootCratesBlocks.register();
        LootCratesItems.register();
        LootCratesBlockEntityType.register();
    }

}
