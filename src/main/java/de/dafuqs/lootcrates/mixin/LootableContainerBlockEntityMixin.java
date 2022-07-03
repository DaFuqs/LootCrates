package de.dafuqs.lootcrates.mixin;

import de.dafuqs.lootcrates.LootCrates;
import de.dafuqs.lootcrates.worldgen.LootCrateReplacementPosition;
import de.dafuqs.lootcrates.worldgen.LootCratesWorldgenReplacer;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin {

    @Inject(method = "setLootTable(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Identifier;)V", at = @At("TAIL"))
    private static void noteChestForLootCrateConversion(BlockView world, Random random, BlockPos pos, Identifier id, CallbackInfo ci) {
        if(LootCrates.CONFIG.ReplaceVanillaWorldgenChests) {
            RegistryKey<World> worldRegistryKey;
            if(world instanceof ProtoChunk protoChunk) {
                ChunkAccessor protoChunkAccessor = ((ChunkAccessor) protoChunk);
                HeightLimitView heightLimitView = protoChunkAccessor.getWorld();
                if(heightLimitView instanceof ChunkRegion chunkRegion) {
                    worldRegistryKey = chunkRegion.toServerWorld().getRegistryKey();
                } else if(heightLimitView instanceof ServerWorld serverWorld) {
                    worldRegistryKey = serverWorld.getRegistryKey();
                } else {
                    return;
                }
            } else if (world instanceof ChunkRegion chunkRegion) {
                worldRegistryKey = chunkRegion.toServerWorld().getRegistryKey();
            } else if(world instanceof ServerWorld serverWorld) {
                worldRegistryKey = serverWorld.getRegistryKey();
            } else {
                return;
            }

            if (!LootCrates.CONFIG.ReplaceVanillaWorldgenChestsDimensionsBlacklist.contains(worldRegistryKey.getValue().toString())) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if ((blockEntity instanceof ChestBlockEntity || blockEntity instanceof BarrelBlockEntity) && world instanceof ChunkRegion || world instanceof ServerWorld) {
                    LootTableAccessor lootTableAccessor = ((LootTableAccessor) blockEntity);
                    LootCratesWorldgenReplacer.replacements.add(new LootCrateReplacementPosition(worldRegistryKey, pos, lootTableAccessor.getLootTableIdentifier(), lootTableAccessor.getLootTableSeed()));
                }
            }
        }
    }

}
