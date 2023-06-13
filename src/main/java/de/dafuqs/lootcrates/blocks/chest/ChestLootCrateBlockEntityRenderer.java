package de.dafuqs.lootcrates.blocks.chest;

import de.dafuqs.lootcrates.*;
import net.fabricmc.api.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.*;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.util.*;
import net.minecraft.client.util.math.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

@Environment(EnvType.CLIENT)
public class ChestLootCrateBlockEntityRenderer<T extends BlockEntity & LidOpenable> implements BlockEntityRenderer<T> {

    private final ModelPart singleChestLid;
    private final ModelPart singleChestBase;
    private final ModelPart singleChestLatch;

    public ChestLootCrateBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        ModelPart modelPart = ctx.getLayerModelPart(EntityModelLayers.CHEST);
        this.singleChestBase = modelPart.getChild("bottom");
        this.singleChestLid = modelPart.getChild("lid");
        this.singleChestLatch = modelPart.getChild("lock");
    }

    private void render(MatrixStack matrices, VertexConsumer vertices, ModelPart lid, ModelPart latch, ModelPart base, float openFactor, int light, int overlay) {
        lid.pitch = -(openFactor * 1.5707964F);
        latch.pitch = lid.pitch;
        lid.render(matrices, vertices, light, overlay);
        latch.render(matrices, vertices, light, overlay);
        base.render(matrices, vertices, light, overlay);
    }

    @Override
    public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int light, int overlay) {
        World world = entity.getWorld();
        boolean bl = world != null;

        if(entity instanceof ChestLootCrateBlockEntity chestLootCrateBlockEntity) {
            BlockState blockState = bl ? entity.getCachedState() : Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.SOUTH);
            Block block = blockState.getBlock();

            if(block instanceof ChestLootCrateBlock) {
                SpriteIdentifier spriteIdentifier = LootCrateAtlas.getChestTexture(chestLootCrateBlockEntity);
                boolean hasTransparency = LootCrateAtlas.hasTransparency(chestLootCrateBlockEntity);
                VertexConsumer vertexConsumer;
                if (hasTransparency) {
                    vertexConsumer = spriteIdentifier.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntityTranslucent);
                } else {
                    vertexConsumer = spriteIdentifier.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntityCutoutNoCull);
                }
                
                matrices.push();
                float f = (blockState.get(ChestBlock.FACING)).asRotation();
                matrices.translate(0.5D, 0.5D, 0.5D);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-f));
                matrices.translate(-0.5D, -0.5D, -0.5D);

                float openFactor = entity.getAnimationProgress(tickDelta);
                openFactor = 1.0F - openFactor;
                openFactor = 1.0F - openFactor * openFactor * openFactor;

                this.render(matrices, vertexConsumer, this.singleChestLid, this.singleChestLatch, this.singleChestBase, openFactor, light, overlay);

                matrices.pop();
            }
        }
    }
}