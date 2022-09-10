package de.dafuqs.lootcrates.mixin;

import de.dafuqs.lootcrates.LootCrates;
import de.dafuqs.lootcrates.worldgen.LootCrateReplacementPosition;
import de.dafuqs.lootcrates.worldgen.LootCratesWorldgenReplacer;
import net.minecraft.block.entity.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin {
    
    @Inject(method = "setLootTable(Lnet/minecraft/util/Identifier;J)V", at = @At("TAIL"))
    private void lootCrates$setLootTable(Identifier id, long seed, CallbackInfo ci) {
        if(LootCrates.CONFIG.ReplaceVanillaWorldgenChests) {
            LootableContainerBlockEntity container = (LootableContainerBlockEntity)(Object) this;
            World world = container.getWorld();
            BlockPos pos = container.getPos();
            RegistryKey<World> worldRegistryKey = null;
            if(world instanceof ServerWorld serverWorld) {
                worldRegistryKey = serverWorld.getRegistryKey();
            }

            if (worldRegistryKey != null && !LootCrates.CONFIG.ReplaceVanillaWorldgenChestsDimensionsBlacklist.contains(worldRegistryKey.getValue().toString())) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if ((blockEntity instanceof ChestBlockEntity || blockEntity instanceof BarrelBlockEntity || blockEntity instanceof ShulkerBoxBlockEntity)) {
                    LootCratesWorldgenReplacer.replacements.add(new LootCrateReplacementPosition(worldRegistryKey, pos));
                }
            }
        }
    }

}
