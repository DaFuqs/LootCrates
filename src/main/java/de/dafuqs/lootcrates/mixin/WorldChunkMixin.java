package de.dafuqs.lootcrates.mixin;

import de.dafuqs.lootcrates.LootCrates;
import de.dafuqs.lootcrates.worldgen.LootCrateReplacementPosition;
import de.dafuqs.lootcrates.worldgen.LootCratesWorldgenReplacer;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {
	
	@Inject(method = "setBlockEntity(Lnet/minecraft/block/entity/BlockEntity;)V",
			at = @At(target = "Lnet/minecraft/block/entity/BlockEntity;setWorld(Lnet/minecraft/world/World;)V", value = "INVOKE", shift = At.Shift.AFTER)
	)
	protected void lootCrates$addBlockEntity(BlockEntity blockEntity, CallbackInfo ci) {
		WorldChunk worldChunk = (WorldChunk) (Object) this;
		
		if (LootCrates.CONFIG.ReplaceVanillaWorldgenChests
				&& !worldChunk.getWorld().isClient
				&& ((blockEntity instanceof ChestBlockEntity || blockEntity instanceof BarrelBlockEntity || blockEntity instanceof ShulkerBoxBlockEntity))
				&& ((LootTableAccessor) blockEntity).getLootTableIdentifier() != null
				&& !LootCrates.CONFIG.isWorldBlacklisted(worldChunk.getWorld())) {
			
			LootCratesWorldgenReplacer.replacements.add(new LootCrateReplacementPosition((ServerWorld) worldChunk.getWorld(), blockEntity.getPos()));
			
		}
		
	}
}