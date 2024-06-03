package de.dafuqs.lootcrates.components;

import de.dafuqs.lootcrates.*;
import net.minecraft.component.*;
import net.minecraft.registry.*;
import net.minecraft.util.*;

import java.util.function.*;

public class LootCrateDataComponentTypes {
    
    public static final DataComponentType<LootCrateLootComponent> LOOT_CRATE_LOOT = register("loot_crate_loot", (builder) -> builder.codec(LootCrateLootComponent.CODEC));
    
    private static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, new Identifier(LootCrates.MOD_ID, id), builderOperator.apply(DataComponentType.builder()).build());
    }
    
}
