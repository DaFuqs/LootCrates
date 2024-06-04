package de.dafuqs.lootcrates.enums;

import net.minecraft.util.*;

public enum LootCrateVariant implements StringIdentifiable {
	COMMON("common"),
	UNCOMMON("uncommon"),
	RARE("rare"),
	EPIC("epic"),
	GHOST("ghost"),
	BLAZE("blaze");
	
	private String name;
	
	LootCrateVariant(String name) {
		this.name = name;
	}
	
	public static final EnumCodec<LootCrateVariant> CODEC = StringIdentifiable.createCodec(LootCrateVariant::values);
	
	@Override
	public String asString() {
		return this.name;
	}
}