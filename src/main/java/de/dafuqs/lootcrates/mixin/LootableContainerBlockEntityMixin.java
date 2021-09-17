package de.dafuqs.lootcrates.mixin;

import de.dafuqs.lootcrates.LootCrates;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin {

    @Inject(method = "setLootTable(Lnet/minecraft/world/BlockView;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Identifier;)V", at = @At("TAIL"))
    private static void noteChestForLootCrateConversion(BlockView world, Random random, BlockPos pos, Identifier id, CallbackInfo ci) {
        if(LootCrates.CONFIG.ReplaceVanillaWorldgenChests) {

            RegistryKey<World> worldRegistryKey;
            if (world instanceof ChunkRegion chunkRegion) {
                worldRegistryKey = chunkRegion.toServerWorld().getRegistryKey();
            } else {
                ServerWorld serverWorld = (ServerWorld) world;
                worldRegistryKey = serverWorld.getRegistryKey();
            }

            if (!LootCrates.CONFIG.ReplaceVanillaWorldgenChestsDimensionsBlacklist.contains(worldRegistryKey.getValue().toString())) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof ChestBlockEntity && world instanceof ChunkRegion || world instanceof ServerWorld) {
                    LootTableAccessor lootTableAccessor = ((LootTableAccessor) blockEntity);
                    LootCrates.replacements.add(new LootCrates.LootCrateReplacement(worldRegistryKey, pos, lootTableAccessor.getLootTableIdentifier(), lootTableAccessor.getLootTableSeed()));
                }
            }
        }
    }

}
