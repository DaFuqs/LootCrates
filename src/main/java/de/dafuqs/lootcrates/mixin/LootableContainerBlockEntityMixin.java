package de.dafuqs.lootcrates.mixin;

import de.dafuqs.lootcrates.LootCrateAtlas;
import de.dafuqs.lootcrates.LootCrates;
import de.dafuqs.lootcrates.blocks.chest.ChestLootCrateBlock;
import de.dafuqs.lootcrates.blocks.chest.ChestLootCrateBlockEntity;
import de.dafuqs.lootcrates.enums.LootCrateRarity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.Structure;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(LootableContainerBlockEntity.class)
public class LootableContainerBlockEntityMixin {

    /**
     *
     */
    @Inject(method = "setLootTable(Lnet/minecraft/world/BlockView;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Identifier;)V", at = @At("TAIL"))
    private static void convertChestToLootCrate(BlockView world, Random random, BlockPos pos, Identifier id, CallbackInfo ci) {
        if(LootCrates.CONFIG.VanillaTreasureChestsAreOncePerPlayer) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ChestBlockEntity && blockEntity.getWorld() != null) {
                LootTableAccessor lootTableAccessor = ((LootTableAccessor) blockEntity);
                LootCrates.replacements.add(new LootCrates.LootCrateReplacement(blockEntity.getWorld().getRegistryKey(), pos, lootTableAccessor.getLootTableIdentifier(), lootTableAccessor.getLootTableSeed()));
            }
        }
    }

}
