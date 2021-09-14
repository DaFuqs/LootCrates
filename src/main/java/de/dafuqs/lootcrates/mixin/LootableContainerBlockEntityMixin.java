package de.dafuqs.lootcrates.mixin;

import de.dafuqs.lootcrates.LootCrates;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(LootableContainerBlockEntity.class)
public class LootableContainerBlockEntityMixin {

    @Inject(method = "setLootTable(Lnet/minecraft/world/BlockView;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Identifier;)V", at = @At("TAIL"))
    private static void convertChestToLootCrate(BlockView world, Random random, BlockPos pos, Identifier id, CallbackInfo ci) {
        if(LootCrates.CONFIG.VanillaTreasureChestsAreOncePerPlayer && world instanceof ChunkRegion) {
            ChunkRegion region = (ChunkRegion) world;
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ChestBlockEntity) {
                LootTableAccessor lootTableAccessor = ((LootTableAccessor) blockEntity);
                LootCrates.replacements.add(new LootCrates.LootCrateReplacement(region.toServerWorld().getRegistryKey(), pos, lootTableAccessor.getLootTableIdentifier(), lootTableAccessor.getLootTableSeed()));
            }
        }
    }

}