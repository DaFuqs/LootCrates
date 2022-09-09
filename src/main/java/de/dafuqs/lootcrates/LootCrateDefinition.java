package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.enums.LootCrateRarity;
import de.dafuqs.lootcrates.enums.ScheduledTickEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import static net.minecraft.client.render.TexturedRenderLayers.CHEST_ATLAS_TEXTURE;
import static net.minecraft.client.render.TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE;

public class LootCrateDefinition {
        public String identifier;
        public Identifier chestTextureIdentifier;
        public SpriteIdentifier chestTexture;
        public Identifier shulkerTextureIdentifier;
        public SpriteIdentifier shulkerTexture;
        public MapColor mapColor;
        public boolean hasTransparency;
        public ScheduledTickEvent scheduledTickEvent;
        public boolean fireProof;
        public int luminance;
        public Rarity rarity;
        public Text lockedTooltip;
        public Text lockedConsumeTooltip;
        public Text lootKeyTooltip;
        public Text lootBagTooltip;
        public Text keyNeededTooltip;
        SoundEvent customOpenSoundEvent;
        SoundEvent customCloseSoundEvent;

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
            if(fireProof) {
                return new FabricItemSettings().group(LootCrates.ITEM_GROUP).maxCount(16).rarity(rarity).fireproof();
            } else {
                return new FabricItemSettings().group(LootCrates.ITEM_GROUP).maxCount(16).rarity(rarity);
            }
        }

    public FabricItemSettings getLootBagItemSettings() {
        if(fireProof) {
            return new FabricItemSettings().group(LootCrates.ITEM_GROUP).maxCount(16).rarity(rarity).fireproof();
        } else {
            return new FabricItemSettings().group(LootCrates.ITEM_GROUP).maxCount(16).rarity(rarity);
        }
    }

        public FabricItemSettings getBlockItemSettings() {
            if(fireProof) {
                return new FabricItemSettings().group(LootCrates.ITEM_GROUP).rarity(rarity).fireproof();
            } else {
                return new FabricItemSettings().group(LootCrates.ITEM_GROUP).rarity(rarity);
            }
        }

        public AbstractBlock.Settings getChestBlockSettings() {
            AbstractBlock.Settings blockSettings = FabricBlockSettings.of(Material.METAL, mapColor).luminance(luminance);

            if(LootCrates.CONFIG.ChestCrateHardness < 0) {
                blockSettings = blockSettings.strength(-1.0F, 3600000.0F).dropsNothing();
            } else {
                blockSettings = blockSettings.strength(LootCrates.CONFIG.ChestCrateHardness);
            }
            if(!LootCrates.CONFIG.ChestCratesDropAsItems) {
                blockSettings = blockSettings.dropsNothing();
            }

            if(hasTransparency) {
                blockSettings = blockSettings.nonOpaque();
            }

            return blockSettings;
        }

        public AbstractBlock.Settings getShulkerBlockSettings() {
            AbstractBlock.Settings blockSettings = FabricBlockSettings.of(Material.SHULKER_BOX, mapColor).luminance(luminance);

            if(LootCrates.CONFIG.ShulkerCrateHardness < 0) {
                blockSettings = blockSettings.strength(-1.0F, 3600000.0F).dropsNothing();
            } else {
                blockSettings = blockSettings.strength(LootCrates.CONFIG.ShulkerCrateHardness);
            }
            if(!LootCrates.CONFIG.ShulkerCratesDropAsItems) {
                blockSettings = blockSettings.dropsNothing();
            }

            // shulker blocks are always opaque
            return blockSettings;
        }

    public AbstractBlock.Settings getLootBarrelBlockSettings() {
        AbstractBlock.Settings blockSettings = FabricBlockSettings.of(Material.WOOD, mapColor).luminance(luminance);

        if(LootCrates.CONFIG.LootBarrelHardness < 0) {
            blockSettings = blockSettings.strength(-1.0F, 3600000.0F).dropsNothing();
        } else {
            blockSettings = blockSettings.strength(LootCrates.CONFIG.LootBarrelHardness);
        }
        if(!LootCrates.CONFIG.LootBarrelsDropAsItems) {
            blockSettings = blockSettings.dropsNothing();
        }

        if(hasTransparency) {
            blockSettings = blockSettings.nonOpaque();
        }

        return blockSettings;
    }

        @Environment(EnvType.CLIENT)
        public void setupTextures() {
            this.chestTextureIdentifier = new Identifier(LootCrates.MOD_ID, "entity/chest/" + this.identifier + "_crate");
            this.chestTexture = new SpriteIdentifier(CHEST_ATLAS_TEXTURE, this.chestTextureIdentifier);
            this.shulkerTextureIdentifier = new Identifier(LootCrates.MOD_ID, "entity/shulker/" + this.identifier + "_shulker");
            this.shulkerTexture = new SpriteIdentifier(SHULKER_BOXES_ATLAS_TEXTURE, this.shulkerTextureIdentifier);
        }

    }