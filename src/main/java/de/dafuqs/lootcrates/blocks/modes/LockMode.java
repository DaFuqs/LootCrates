package de.dafuqs.lootcrates.blocks.modes;

public enum LockMode {
	NONE(false, false, false),
	REQUIRE_KEY(true, false, false),
	REQUIRE_KEY_RELOCK(true, false, true),
	CONSUME_KEY(true, true, false),
	CONSUME_KEY_RELOCK(true, true, true);

	private final boolean requiresKey;
	private final boolean consumesKey;
	private final boolean relocks;
	
	LockMode(boolean requiresKey, boolean consumesKey, boolean relocks) {
		this.requiresKey = requiresKey;
		this.consumesKey = consumesKey;
		this.relocks = relocks;
	}
	
	public boolean requiresKey() {
		return this.requiresKey;
	}
    
    public boolean consumesKey() {
		return this.consumesKey;
    }
	
    public boolean relocks() {
		return this.relocks;
    }
    
}