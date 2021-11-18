package de.dafuqs.lootcrates.mixin;

import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ProtoChunk.class)
public interface ProtoChunkAccessor {

    @Accessor("world")
    HeightLimitView getWorld();
    
}
