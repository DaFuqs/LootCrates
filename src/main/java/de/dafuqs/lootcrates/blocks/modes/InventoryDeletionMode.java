package de.dafuqs.lootcrates.blocks.modes;

public enum InventoryDeletionMode {
	NEVER, // the crate never deletes its inventory. Useful when the crate drops on a per-player basis, like on mob kill
	ON_OPEN, // the crate deletes its inventory before it gets opened by the player, giving the player a fresh inventory every time
	WHEN_REPLENISHED // the crate deletes its inventory each time it generates new loot
}