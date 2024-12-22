package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.enums.*;
import net.fabricmc.api.*;
import net.minecraft.block.*;
import net.minecraft.block.piston.*;
import net.minecraft.client.texture.*;
import net.minecraft.client.util.*;
import net.minecraft.item.*;
import net.minecraft.registry.*;
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
	
	public LootCrateDefinition(LootCrateVariant lootCrateVariant, Rarity rarity, MapColor mapColor, int luminance, boolean hasTransparency, boolean fireProof) {
		this.identifier = lootCrateVariant.toString().toLowerCase();
		
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
	
	public Item.Settings getKeyItemSettings(Identifier id) {
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
		Item.Settings settings = new Item.Settings().registryKey(key).maxCount(16).rarity(rarity);
		if (fireProof) {
			settings = settings.fireproof();
		}
		return settings;
	}
	
	public Item.Settings getLootBagItemSettings(Identifier id) {
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
		Item.Settings settings = new Item.Settings().registryKey(key).maxCount(16).rarity(rarity);
		if (fireProof) {
			settings = settings.fireproof();
		}
		return settings;
	}
	
	public Item.Settings getBlockItemSettings(Identifier id) {
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
		Item.Settings settings = new Item.Settings().registryKey(key).useBlockPrefixedTranslationKey().rarity(rarity);
		if (fireProof) {
			settings = settings.fireproof();
		}
		return settings;
	}
	
	public AbstractBlock.Settings getChestBlockSettings(Identifier id) {
		RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);
		AbstractBlock.Settings settings = AbstractBlock.Settings.copy(Blocks.CHEST).registryKey(key).mapColor(mapColor).luminance(value -> luminance).pistonBehavior(LootCrates.CONFIG.ChestCrateHardness < 0 ? PistonBehavior.BLOCK : PistonBehavior.DESTROY);
		
		if (LootCrates.CONFIG.ChestCrateHardness < 0) {
			settings = settings.strength(-1.0F, 3600000.0F).dropsNothing();
		} else {
			settings = settings.strength(LootCrates.CONFIG.ChestCrateHardness);
		}
		if (!LootCrates.CONFIG.ChestCratesDropAsItems) {
			settings = settings.dropsNothing();
		}
		
		if (hasTransparency) {
			settings = settings.nonOpaque();
		}
		
		return settings;
	}
	
	public AbstractBlock.Settings getShulkerBlockSettings(Identifier id) {
		RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);
		AbstractBlock.Settings settings = AbstractBlock.Settings.copy(Blocks.SHULKER_BOX).registryKey(key).mapColor(mapColor).luminance(value -> luminance).pistonBehavior(LootCrates.CONFIG.ShulkerCrateHardness < 0 ? PistonBehavior.BLOCK : PistonBehavior.DESTROY);
		
		if (LootCrates.CONFIG.ShulkerCrateHardness < 0) {
			settings = settings.strength(-1.0F, 3600000.0F).dropsNothing();
		} else {
			settings = settings.strength(LootCrates.CONFIG.ShulkerCrateHardness);
		}
		if (!LootCrates.CONFIG.ShulkerCratesDropAsItems) {
			settings = settings.dropsNothing();
		}
		
		// shulker blocks are always opaque
		return settings;
	}
	
	public AbstractBlock.Settings getLootBarrelBlockSettings(Identifier id) {
		RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);
		AbstractBlock.Settings settings = AbstractBlock.Settings.copy(Blocks.BARREL).registryKey(key).mapColor(mapColor).luminance(value -> luminance).pistonBehavior(LootCrates.CONFIG.LootBarrelHardness < 0 ? PistonBehavior.BLOCK : PistonBehavior.DESTROY);
		
		if (LootCrates.CONFIG.LootBarrelHardness < 0) {
			settings = settings.strength(-1.0F, 3600000.0F).dropsNothing();
		} else {
			settings = settings.strength(LootCrates.CONFIG.LootBarrelHardness);
		}
		if (!LootCrates.CONFIG.LootBarrelsDropAsItems) {
			settings = settings.dropsNothing();
		}
		
		if (hasTransparency) {
			settings = settings.nonOpaque();
		}
		
		return settings;
	}
	
	@Environment(EnvType.CLIENT)
	public void setupTextures() {
		this.chestTextureIdentifier = Identifier.of(LootCrates.MOD_ID, "block/" + this.identifier + "_crate");
		this.chestTexture = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, this.chestTextureIdentifier);
		this.shulkerTextureIdentifier = Identifier.of(LootCrates.MOD_ID, "block/" + this.identifier + "_shulker");
		this.shulkerTexture = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, this.shulkerTextureIdentifier);
	}
	
}