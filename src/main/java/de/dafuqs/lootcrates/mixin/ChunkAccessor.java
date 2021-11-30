package de.dafuqs.lootcrates.mixin;

import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Chunk.class)
public interface ChunkAccessor {

    @Accessor("heightLimitView")
    HeightLimitView getWorld();
    
}
