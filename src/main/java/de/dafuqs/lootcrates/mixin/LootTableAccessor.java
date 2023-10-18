package de.dafuqs.lootcrates.mixin;

import net.minecraft.block.entity.*;
import net.minecraft.util.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(LootableContainerBlockEntity.class)
public interface LootTableAccessor {

    @Accessor("lootTableId")
    Identifier getLootTableIdentifier();

    @Accessor("lootTableSeed")
    long getLootTableSeed();

}
