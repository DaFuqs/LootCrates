package de.dafuqs.lootcrates.mixin;

import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootableContainerBlockEntity.class)
public interface LootTableAccessor {

    @Accessor("lootTableId")
    Identifier getLootTableIdentifier();

    @Accessor("lootTableSeed")
    long getLootTableSeed();

}
