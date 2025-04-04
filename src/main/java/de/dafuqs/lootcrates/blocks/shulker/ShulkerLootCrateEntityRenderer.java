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
	
	private final ShulkerLootCrateBlockModel model;
	
	public ShulkerLootCrateEntityRenderer(BlockEntityRendererFactory.Context ctx) {
		this.model = new ShulkerLootCrateBlockModel(ctx.getLayerModelPart(EntityModelLayers.SHULKER_BOX));
	}
	
	@Override
	public void render(ShulkerLootCrateBlockEntity shulkerLootCrateBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, int overlay, Vec3d cameraPos) {
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
		matrixStack.translate(0.5F, 0.5F, 0.5F);
		float g = 0.9995F;
		matrixStack.scale(0.9995F, 0.9995F, 0.9995F);
		matrixStack.multiply(direction.getRotationQuaternion());
		matrixStack.scale(1.0F, -1.0F, -1.0F);
		matrixStack.translate(0.0F, -1.0F, 0.0F);
		float openness = shulkerLootCrateBlockEntity.getAnimationProgress(f);
		this.model.animateLid(openness);
		this.model.render(matrixStack, vertexConsumer, light, overlay);
		matrixStack.pop();
	}
	
	@Environment(EnvType.CLIENT)
	private static class ShulkerLootCrateBlockModel extends Model {
		private final ModelPart lid;
		
		public ShulkerLootCrateBlockModel(ModelPart root) {
			super(root, RenderLayer::getEntityCutoutNoCull);
			this.lid = root.getChild("lid");
		}
		
		public void animateLid(float openness) {
			this.lid.setOrigin(0.0F, 24.0F - openness * 0.5F * 16.0F, 0.0F);
			this.lid.yaw = 270.0F * openness * 0.017453292F;
		}
	}
	
}
