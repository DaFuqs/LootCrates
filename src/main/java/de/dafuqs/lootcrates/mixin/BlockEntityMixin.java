package de.dafuqs.lootcrates.mixin;

import de.dafuqs.lootcrates.LootCrates;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {

    @Inject(method = "Lnet/minecraft/block/entity/BlockEntity;setLocation(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", at = @At("RETURN"))
    protected void noteChestForLootCrateConversion(World world, BlockPos pos, CallbackInfo ci) {
        if(LootCrates.CONFIG.VanillaTreasureChestsAreOncePerPlayer && (Object) this instanceof ChestBlockEntity && world instanceof ServerWorld) {
            LootTableAccessor lootTableAccessor = ((LootTableAccessor) this);
            if(lootTableAccessor.getLootTableIdentifier() != null) {
                LootCrates.replacements.add(new LootCrates.LootCrateReplacement(world.getRegistryKey(), pos, lootTableAccessor.getLootTableIdentifier(), lootTableAccessor.getLootTableSeed()));
            }
        }
    }

}