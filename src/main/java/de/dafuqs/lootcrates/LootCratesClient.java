package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.blocks.*;
import de.dafuqs.lootcrates.blocks.chest.*;
import de.dafuqs.lootcrates.blocks.shulker.*;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.client.rendering.v1.*;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class LootCratesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LootCrateAtlas.registerTransparentBlocks();
        LootCrateAtlas.setupTextures();

        BlockEntityRendererRegistry.register(LootCratesBlockEntityType.CHEST_LOOT_CRATE_BLOCK_ENTITY, ChestLootCrateBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(LootCratesBlockEntityType.SHULKER_LOOT_CRATE_BLOCK_ENTITY, ShulkerLootCrateEntityRenderer::new);
    }
}
