package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.blocks.LootCratesBlockEntityType;
import de.dafuqs.lootcrates.blocks.chest.ChestLootCrateBlockEntityRenderer;
import de.dafuqs.lootcrates.blocks.shulker.ShulkerLootCrateEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.util.Identifier;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class LootCratesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(LootCratesBlockEntityType.CHEST_LOOT_CRATE_BLOCK_ENTITY, ChestLootCrateBlockEntityRenderer::new);
        BlockEntityRendererRegistry.INSTANCE.register(LootCratesBlockEntityType.SHULKER_LOOT_CRATE_BLOCK_ENTITY, ShulkerLootCrateEntityRenderer::new);

        //Register textures in chest atlas
        ClientSpriteRegistryCallback.event(TexturedRenderLayers.CHEST_ATLAS_TEXTURE).register((texture, registry) -> {
            registry.register(new Identifier(LootCrates.MOD_ID, "entity/chest/common_crate"));
            registry.register(new Identifier(LootCrates.MOD_ID, "entity/chest/uncommon_crate"));
            registry.register(new Identifier(LootCrates.MOD_ID, "entity/chest/rare_crate"));
            registry.register(new Identifier(LootCrates.MOD_ID, "entity/chest/epic_crate"));
        });

        ClientSpriteRegistryCallback.event(TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE).register((texture, registry) -> {
            registry.register(new Identifier(LootCrates.MOD_ID, "entity/shulker/common_shulker"));
            registry.register(new Identifier(LootCrates.MOD_ID, "entity/shulker/uncommon_shulker"));
            registry.register(new Identifier(LootCrates.MOD_ID, "entity/shulker/rare_shulker"));
            registry.register(new Identifier(LootCrates.MOD_ID, "entity/shulker/epic_shulker"));
        });
    }
}
