package de.dafuqs.lootcrates.mixin;

import de.dafuqs.lootcrates.*;
import de.dafuqs.lootcrates.worldgen.*;
import net.minecraft.block.entity.*;
import net.minecraft.server.world.*;
import net.minecraft.world.chunk.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

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