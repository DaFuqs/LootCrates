package de.dafuqs.lootcrates.components;

import net.minecraft.util.*;

public enum InventoryDeletionMode implements StringIdentifiable {
	NEVER("never"), // the crate never deletes its inventory. Useful when the crate drops on a per-player basis, like on mob kill
	ON_OPEN("on_open"), // the crate deletes its inventory before it gets opened by the player, giving the player a fresh inventory every time
	WHEN_REPLENISHED("when_replenished"); // the crate deletes its inventory each time it generates new loot
	
	public static final EnumCodec<InventoryDeletionMode> CODEC = StringIdentifiable.createCodec(InventoryDeletionMode::values);
	
	private final String name;
	
	InventoryDeletionMode(String name) {
		this.name = name;
	}
	
	@Override
	public String asString() {
		return this.name;
	}
	
}
