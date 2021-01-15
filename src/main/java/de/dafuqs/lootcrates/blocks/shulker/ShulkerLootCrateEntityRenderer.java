package de.dafuqs.lootcrates.blocks.shulker;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class ShulkerLootCrateEntityRenderer extends BlockEntityRenderer<ShulkerLootCrateBlockEntity> {

    private final ShulkerEntityModel<?> model;

    public ShulkerLootCrateEntityRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        super(blockEntityRenderDispatcher);
        this.model = new ShulkerEntityModel<>();
    }

    @Override
    public void render(ShulkerLootCrateBlockEntity shulkerLootCrateBlockEntity, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, int overlay) {
        Direction direction = Direction.UP;
        if (shulkerLootCrateBlockEntity.hasWorld()) {
            BlockState blockState = shulkerLootCrateBlockEntity.getWorld().getBlockState(shulkerLootCrateBlockEntity.getPos());
            if (blockState.getBlock() instanceof ShulkerBoxBlock) {
                direction = blockState.get(ShulkerBoxBlock.FACING);
            }
        }

        SpriteIdentifier spriteIdentifier2 = shulkerLootCrateBlockEntity.getTexture();

        matrixStack.push();

        matrixStack.translate(0.5D, 0.5D, 0.5D);
        matrixStack.scale(0.9995F, 0.9995F, 0.9995F);
        matrixStack.multiply(direction.getRotationQuaternion());
        matrixStack.scale(1.0F, -1.0F, -1.0F);
        matrixStack.translate(0.0D, -1.0D, 0.0D);

        boolean hasTransparency = shulkerLootCrateBlockEntity.hasTransparency();
        VertexConsumer vertexConsumer;
        if(hasTransparency) {
            vertexConsumer = spriteIdentifier2.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntityTranslucent);
        } else {
            vertexConsumer = spriteIdentifier2.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntityCutoutNoCull);
        }

        this.model.getBottomShell().render(matrixStack, vertexConsumer, light, overlay);
        matrixStack.translate(0.0D, (-shulkerLootCrateBlockEntity.getAnimationProgress(tickDelta) * 0.5F), 0.0D);
        matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(270.0F * shulkerLootCrateBlockEntity.getAnimationProgress(tickDelta)));
        this.model.getTopShell().render(matrixStack, vertexConsumer, light, overlay);

        matrixStack.pop();
    }

}
