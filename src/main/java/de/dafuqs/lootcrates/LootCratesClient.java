package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.blocks.LootCratesBlockEntityType;
import de.dafuqs.lootcrates.blocks.lootcrate.LootCrateBlockEntity;
import de.dafuqs.lootcrates.blocks.lootcrate.LootCrateBlockEntityRenderer;
import de.dafuqs.lootcrates.blocks.shulkercrate.ShulkerLootCrateEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.render.block.entity.ShulkerBoxBlockEntityRenderer;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class LootCratesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(LootCratesBlockEntityType.LOOT_CRATE_BLOCK_ENTITY, LootCrateBlockEntityRenderer::new);
        BlockEntityRendererRegistry.INSTANCE.register(LootCratesBlockEntityType.SHULKER_LOOT_CRATE_BLOCK_ENTITY, ShulkerLootCrateEntityRenderer::new);
    }
}
