package de.dafuqs.lootcrates.enums;

import net.minecraft.util.*;

public enum LootCrateVariant implements StringIdentifiable {
	COMMON("common", false),
	UNCOMMON("uncommon", false),
	RARE("rare", false),
	EPIC("epic", false),
	GHOST("ghost", true),
	BLAZE("blaze", false);
	
	private final String name;
	private final boolean hasTransparency;
	
	LootCrateVariant(String name, boolean hasTransparency) {
		this.name = name;
		this.hasTransparency = hasTransparency;
	}
	
	public static final EnumCodec<LootCrateVariant> CODEC = StringIdentifiable.createCodec(LootCrateVariant::values);
	
	public boolean hasTransparency() {
		return hasTransparency;
	}
	
	@Override
	public String asString() {
		return this.name;
	}
}