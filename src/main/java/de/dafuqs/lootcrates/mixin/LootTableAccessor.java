package de.dafuqs.lootcrates.mixin;

import net.minecraft.block.entity.*;
import net.minecraft.loot.*;
import net.minecraft.registry.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(LootableContainerBlockEntity.class)
public interface LootTableAccessor {
	
	@Accessor("lootTable")
	RegistryKey<LootTable> getLootTable();
	
	@Accessor("lootTableSeed")
	long getLootTableSeed();
	
}
