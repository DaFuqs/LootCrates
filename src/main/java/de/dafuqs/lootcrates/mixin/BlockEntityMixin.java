package de.dafuqs.lootcrates.mixin;

import de.dafuqs.lootcrates.LootCrates;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {

    @Shadow @Nullable protected World world;

    @Inject(method = "setWorld(Lnet/minecraft/world/World;)V", at = @At("RETURN"))
    protected void noteChestForLootCrateConversion(World world, CallbackInfo ci) {
        if(LootCrates.CONFIG.VanillaTreasureChestsAreOncePerPlayer && (Object) this instanceof ChestBlockEntity chestBlockEntity && this.world instanceof ServerWorld) {
            LootTableAccessor lootTableAccessor = ((LootTableAccessor) this);
            if(lootTableAccessor.getLootTableIdentifier() != null) {
                LootCrates.replacements.add(new LootCrates.LootCrateReplacement(world.getRegistryKey(), chestBlockEntity.getPos(), lootTableAccessor.getLootTableIdentifier(), lootTableAccessor.getLootTableSeed()));
            }
        }
    }

}
