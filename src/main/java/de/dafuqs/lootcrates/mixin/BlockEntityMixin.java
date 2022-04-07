package de.dafuqs.lootcrates.mixin;

import de.dafuqs.lootcrates.LootCrates;
import de.dafuqs.lootcrates.worldgen.LootCrateReplacementPosition;
import de.dafuqs.lootcrates.worldgen.LootCratesWorldgenReplacer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {

    @Inject(method = "setWorld(Lnet/minecraft/world/World;)V", at = @At("RETURN"))
    protected void noteContainerForLootCrateConversion(World world, CallbackInfo ci) {
        if(LootCrates.CONFIG.ReplaceVanillaWorldgenChests && world instanceof ServerWorld && ((Object) this instanceof LootableContainerBlockEntity lootableContainerBlockEntity)) {
            RegistryKey<World> worldRegistryKey = world.getRegistryKey();
            if (!LootCrates.CONFIG.ReplaceVanillaWorldgenChestsDimensionsBlacklist.contains(worldRegistryKey.getValue().toString())) {
                LootTableAccessor lootTableAccessor = ((LootTableAccessor) this);
                if (lootTableAccessor.getLootTableIdentifier() != null) {
                    LootCratesWorldgenReplacer.replacements.add(new LootCrateReplacementPosition(worldRegistryKey, lootableContainerBlockEntity.getPos()));
                }
            }
        }
    }

}
