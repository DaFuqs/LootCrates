package de.dafuqs.lootcrates.blocks.shulker;

import de.dafuqs.lootcrates.*;
import net.fabricmc.api.*;
import net.minecraft.block.*;
import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.*;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.util.*;
import net.minecraft.client.util.math.*;
import net.minecraft.util.math.*;

@Environment(EnvType.CLIENT)
public class ShulkerLootCrateEntityRenderer implements BlockEntityRenderer<ShulkerLootCrateBlockEntity> {

    private final ShulkerEntityModel<?> model;

    public ShulkerLootCrateEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.model = new ShulkerEntityModel<>(ctx.getLayerModelPart(EntityModelLayers.SHULKER));
    }

    public void render(ShulkerLootCrateBlockEntity shulkerLootCrateBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
        Direction direction = Direction.UP;
        if (shulkerLootCrateBlockEntity.hasWorld()) {
            BlockState blockState = shulkerLootCrateBlockEntity.getWorld().getBlockState(shulkerLootCrateBlockEntity.getPos());
            if (blockState.getBlock() instanceof ShulkerLootCrateBlock) {
                direction = blockState.get(ShulkerLootCrateBlock.FACING);
            }
        }

        SpriteIdentifier spriteIdentifier = LootCrateAtlas.getShulkerTexture(shulkerLootCrateBlockEntity);

        boolean hasTransparency = LootCrateAtlas.hasTransparency(shulkerLootCrateBlockEntity);
        VertexConsumer vertexConsumer;
        if (hasTransparency) {
            vertexConsumer = spriteIdentifier.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntityTranslucent);
        } else {
            vertexConsumer = spriteIdentifier.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntityCutoutNoCull);
        }

        matrixStack.push();
        matrixStack.translate(0.5D, 0.5D, 0.5D);
        float g = 0.9995F;
        matrixStack.scale(0.9995F, 0.9995F, 0.9995F);
        matrixStack.multiply(direction.getRotationQuaternion());
        matrixStack.scale(1.0F, -1.0F, -1.0F);
        matrixStack.translate(0.0D, -1.0D, 0.0D);
        ModelPart modelPart = this.model.getLid();
        modelPart.setPivot(0.0F, 24.0F - shulkerLootCrateBlockEntity.getAnimationProgress(f) * 0.5F * 16.0F, 0.0F);
        modelPart.yaw = 270.0F * shulkerLootCrateBlockEntity.getAnimationProgress(f) * 0.017453292F;
        this.model.render(matrixStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.pop();
    }

}
