package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.blocks.LootCratesBlockEntityType;
import de.dafuqs.lootcrates.blocks.chest.ChestLootCrateBlockEntityRenderer;
import de.dafuqs.lootcrates.blocks.shulker.ShulkerLootCrateEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class LootCratesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LootCrateAtlas.registerTextureAtlas();
        LootCrateAtlas.registerTransparentBlocks();
        LootCrateAtlas.setupTextures();

        BlockEntityRendererRegistry.register(LootCratesBlockEntityType.CHEST_LOOT_CRATE_BLOCK_ENTITY, ChestLootCrateBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(LootCratesBlockEntityType.SHULKER_LOOT_CRATE_BLOCK_ENTITY, ShulkerLootCrateEntityRenderer::new);
    }
}
