package de.dafuqs.lootcrates.blocks.chest;

import de.dafuqs.lootcrates.blocks.LootCratesBlockEntityType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class ChestLootCrateBlockEntityRenderer extends BlockEntityRenderer<ChestLootCrateBlockEntity> {

    private final ModelPart singleChestLid;
    private final ModelPart singleChestBase;
    private final ModelPart singleChestLatch;

    public ChestLootCrateBlockEntityRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        super(blockEntityRenderDispatcher);

        this.singleChestBase = new ModelPart(64, 64, 0, 19);
        this.singleChestBase.addCuboid(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F, 0.0F);
        this.singleChestLid = new ModelPart(64, 64, 0, 0);
        this.singleChestLid.addCuboid(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F, 0.0F);
        this.singleChestLid.pivotY = 9.0F;
        this.singleChestLid.pivotZ = 1.0F;
        this.singleChestLatch = new ModelPart(64, 64, 0, 0);
        this.singleChestLatch.addCuboid(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F, 0.0F);
        this.singleChestLatch.pivotY = 8.0F;
    }

    @Override
    public void render(ChestLootCrateBlockEntity chestLootCrateBlockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        World world = chestLootCrateBlockEntity.getWorld();
        boolean bl = world != null;
        BlockState blockState = bl ? chestLootCrateBlockEntity.getCachedState() : Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.SOUTH);
        Block block = blockState.getBlock();

        if (block instanceof ChestLootCrateBlock) {
            matrices.push();

            float f = (blockState.get(ChestBlock.FACING)).asRotation();
            matrices.translate(0.5D, 0.5D, 0.5D);
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-f));
            matrices.translate(-0.5D, -0.5D, -0.5D);

            float openFactor = chestLootCrateBlockEntity.getAnimationProgress(tickDelta);
            openFactor = 1.0F - openFactor;
            openFactor = 1.0F - openFactor * openFactor * openFactor;

            SpriteIdentifier spriteIdentifier = chestLootCrateBlockEntity.getTexture();
            VertexConsumer vertexConsumer = spriteIdentifier.getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutout);
            this.render(matrices, vertexConsumer, this.singleChestLid, this.singleChestLatch, this.singleChestBase, openFactor, light, overlay);

            matrices.pop();
        }
    }

    private void render(MatrixStack matrices, VertexConsumer vertices, ModelPart lid, ModelPart latch, ModelPart base, float openFactor, int light, int overlay) {
        lid.pitch = -(openFactor * 1.5707964F);
        latch.pitch = lid.pitch;
        lid.render(matrices, vertices, light, overlay);
        latch.render(matrices, vertices, light, overlay);
        base.render(matrices, vertices, light, overlay);
    }

}