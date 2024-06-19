package de.dafuqs.lootcrates.components;

import de.dafuqs.lootcrates.*;
import net.minecraft.component.*;
import net.minecraft.registry.*;
import net.minecraft.util.*;

import java.util.function.*;

public class LootCrateDataComponentTypes {
	
	public static final ComponentType<LootCrateLootComponent> LOOT_CRATE_LOOT = register("loot_crate_loot", (builder) -> builder.codec(LootCrateLootComponent.CODEC));
	public static final ComponentType<LootCratePlayerDataComponent> LOOT_CRATE_PLAYER_DATA = register("loot_crate_player_data", (builder) -> builder.codec(LootCratePlayerDataComponent.CODEC));
	
	private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
		return Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(LootCrates.MOD_ID, id), builderOperator.apply(ComponentType.builder()).build());
	}
	
	public static void register() {
	
	}
	
}
