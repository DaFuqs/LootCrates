package de.dafuqs.lootcrates.mixin;

import de.dafuqs.lootcrates.LootCrates;
import de.dafuqs.lootcrates.blocks.LootCrateBlockEntity;
import de.dafuqs.lootcrates.worldgen.LootCrateReplacementPosition;
import de.dafuqs.lootcrates.worldgen.LootCratesWorldgenReplacer;
import net.minecraft.block.entity.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public class MixinChunk {
	
	@Inject(method = "updateTicker(Lnet/minecraft/block/entity/BlockEntity;)V", at = @At(value = "HEAD"))
	private <T extends BlockEntity> void updateTicker(T blockEntity, CallbackInfo ci) {
		if (blockEntity instanceof LootableContainerBlockEntity && !(blockEntity instanceof LootCrateBlockEntity)) {
			WorldChunk worldChunk = (WorldChunk) (Object) this;
			if (!worldChunk.getWorld().isClient) {
				if (!LootCrates.CONFIG.ReplaceVanillaWorldgenChestsDimensionsBlacklist.contains(worldChunk.getWorld().getRegistryKey().getValue().toString())) {
					if ((blockEntity instanceof ChestBlockEntity || blockEntity instanceof BarrelBlockEntity || blockEntity instanceof ShulkerBoxBlockEntity)) {
						LootCratesWorldgenReplacer.replacements.add(new LootCrateReplacementPosition((ServerWorld) worldChunk.getWorld(), blockEntity.getPos()));
					}
				}
			}
		}
	}

}