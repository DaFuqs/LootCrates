package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.enums.*;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.item.v1.*;
import net.fabricmc.fabric.api.object.builder.v1.block.*;
import net.minecraft.block.*;
import net.minecraft.block.piston.*;
import net.minecraft.client.texture.*;
import net.minecraft.client.util.*;
import net.minecraft.sound.*;
import net.minecraft.text.*;
import net.minecraft.util.*;

public class LootCrateDefinition {
	public final String identifier;
	public final MapColor mapColor;
	public final boolean hasTransparency;
	public final boolean fireProof;
	public final int luminance;
	public final Rarity rarity;
	public final Text lockedTooltip;
	public final Text lockedConsumeTooltip;
	public final Text lootKeyTooltip;
	public final Text lootBagTooltip;
	public final Text keyNeededTooltip;
	public Identifier chestTextureIdentifier;
	public SpriteIdentifier chestTexture;
	public Identifier shulkerTextureIdentifier;
	public SpriteIdentifier shulkerTexture;
	public SoundEvent customOpenSoundEvent;
	public SoundEvent customCloseSoundEvent;
	public ScheduledTickEvent scheduledTickEvent;
	
	public LootCrateDefinition(LootCrateRarity lootCrateRarity, Rarity rarity, MapColor mapColor, int luminance, boolean hasTransparency, boolean fireProof) {
		this.identifier = lootCrateRarity.toString().toLowerCase();
		
		this.hasTransparency = hasTransparency;
		this.mapColor = mapColor;
		this.fireProof = fireProof;
		this.luminance = luminance;
		this.rarity = rarity;
		this.lockedTooltip = Text.translatable("item.lootcrates.loot_crate.tooltip.locked_use_" + this.identifier);
		this.lockedConsumeTooltip = Text.translatable("item.lootcrates.loot_crate.tooltip.locked_consume_" + this.identifier);
		this.lootKeyTooltip = Text.translatable("item.lootcrates." + this.identifier + "_crate_key.tooltip");
		this.lootBagTooltip = Text.translatable("item.lootcrates." + this.identifier + "_loot_bag.tooltip");
		this.keyNeededTooltip = Text.translatable("message.lootcrates." + this.identifier + "_key_needed_to_unlock");
		
		this.customOpenSoundEvent = null;
		this.customCloseSoundEvent = null;
		this.scheduledTickEvent = ScheduledTickEvent.NONE;
	}
	
	public void setCustomSounds(SoundEvent customOpenSoundEvent, SoundEvent customCloseSoundEvent) {
		this.customOpenSoundEvent = customOpenSoundEvent;
		this.customCloseSoundEvent = customCloseSoundEvent;
	}
	
	public void setScheduledTickEvent(ScheduledTickEvent scheduledTickEvent) {
		this.scheduledTickEvent = scheduledTickEvent;
	}
	
	public FabricItemSettings getKeyItemSettings() {
		if (fireProof) {
			return new FabricItemSettings().maxCount(16).rarity(rarity).fireproof();
		} else {
			return new FabricItemSettings().maxCount(16).rarity(rarity);
		}
	}
	
	public FabricItemSettings getLootBagItemSettings() {
		if (fireProof) {
			return new FabricItemSettings().maxCount(16).rarity(rarity).fireproof();
		} else {
			return new FabricItemSettings().maxCount(16).rarity(rarity);
		}
	}
	
	public FabricItemSettings getBlockItemSettings() {
		if (fireProof) {
			return new FabricItemSettings().rarity(rarity).fireproof();
		} else {
			return new FabricItemSettings().rarity(rarity);
		}
	}
	
	public AbstractBlock.Settings getChestBlockSettings() {
		AbstractBlock.Settings blockSettings = FabricBlockSettings.copyOf(Blocks.CHEST).mapColor(mapColor).luminance(luminance).pistonBehavior(LootCrates.CONFIG.ChestCrateHardness < 0 ? PistonBehavior.BLOCK : PistonBehavior.DESTROY);
		
		if (LootCrates.CONFIG.ChestCrateHardness < 0) {
			blockSettings = blockSettings.strength(-1.0F, 3600000.0F).dropsNothing();
		} else {
			blockSettings = blockSettings.strength(LootCrates.CONFIG.ChestCrateHardness);
		}
		if (!LootCrates.CONFIG.ChestCratesDropAsItems) {
			blockSettings = blockSettings.dropsNothing();
		}
		
		if (hasTransparency) {
			blockSettings = blockSettings.nonOpaque();
		}
		
		return blockSettings;
	}
	
	public AbstractBlock.Settings getShulkerBlockSettings() {
		AbstractBlock.Settings blockSettings = FabricBlockSettings.copyOf(Blocks.SHULKER_BOX).mapColor(mapColor).luminance(luminance).pistonBehavior(LootCrates.CONFIG.ShulkerCrateHardness < 0 ? PistonBehavior.BLOCK : PistonBehavior.DESTROY);
		
		if (LootCrates.CONFIG.ShulkerCrateHardness < 0) {
			blockSettings = blockSettings.strength(-1.0F, 3600000.0F).dropsNothing();
		} else {
			blockSettings = blockSettings.strength(LootCrates.CONFIG.ShulkerCrateHardness);
		}
		if (!LootCrates.CONFIG.ShulkerCratesDropAsItems) {
			blockSettings = blockSettings.dropsNothing();
		}
		
		// shulker blocks are always opaque
		return blockSettings;
	}
	
	public AbstractBlock.Settings getLootBarrelBlockSettings() {
		AbstractBlock.Settings blockSettings = FabricBlockSettings.copyOf(Blocks.BARREL).mapColor(mapColor).luminance(luminance).pistonBehavior(LootCrates.CONFIG.LootBarrelHardness < 0 ? PistonBehavior.BLOCK : PistonBehavior.DESTROY);
		
		if (LootCrates.CONFIG.LootBarrelHardness < 0) {
			blockSettings = blockSettings.strength(-1.0F, 3600000.0F).dropsNothing();
		} else {
			blockSettings = blockSettings.strength(LootCrates.CONFIG.LootBarrelHardness);
		}
		if (!LootCrates.CONFIG.LootBarrelsDropAsItems) {
			blockSettings = blockSettings.dropsNothing();
		}
		
		if (hasTransparency) {
			blockSettings = blockSettings.nonOpaque();
		}
		
		return blockSettings;
	}
	
	@Environment(EnvType.CLIENT)
	public void setupTextures() {
		this.chestTextureIdentifier = new Identifier(LootCrates.MOD_ID, "block/" + this.identifier + "_crate");
		this.chestTexture = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, this.chestTextureIdentifier);
		this.shulkerTextureIdentifier = new Identifier(LootCrates.MOD_ID, "block/" + this.identifier + "_shulker");
		this.shulkerTexture = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, this.shulkerTextureIdentifier);
	}
	
}