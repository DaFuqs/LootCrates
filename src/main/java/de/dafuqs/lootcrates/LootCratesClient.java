package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.blocks.LootCratesBlockEntityType;
import de.dafuqs.lootcrates.blocks.chest.ChestLootCrateBlockEntityRenderer;
import de.dafuqs.lootcrates.blocks.shulker.ShulkerLootCrateEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class LootCratesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(LootCratesBlockEntityType.CHEST_LOOT_CRATE_BLOCK_ENTITY, ChestLootCrateBlockEntityRenderer::new);
        BlockEntityRendererRegistry.INSTANCE.register(LootCratesBlockEntityType.SHULKER_LOOT_CRATE_BLOCK_ENTITY, ShulkerLootCrateEntityRenderer::new);

        LootCrateAtlas.registerTextureAtlas();
        LootCrateAtlas.registerTransparentBlocks();
        LootCrateAtlas.setupTextures();

        // Add the item group to the creative menu
        new PredefinedLootCratesItemGroup();
    }
}
