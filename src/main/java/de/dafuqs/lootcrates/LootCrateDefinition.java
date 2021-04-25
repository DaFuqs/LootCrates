package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.enums.LootCrateRarity;
import de.dafuqs.lootcrates.enums.ScheduledTickEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.TranslatableText;
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
        public MaterialColor materialColor;
        public boolean hasTransparency;
        public ScheduledTickEvent scheduledTickEvent;
        public boolean fireProof;
        public int luminance;
        public Rarity rarity;
        public TranslatableText lockedTooltip;
        public TranslatableText lockedConsumeTooltip;
        public TranslatableText lootKeyTooltip;
        public TranslatableText keyNeededTooltip;
        SoundEvent customOpenSoundEvent;
        SoundEvent customCloseSoundEvent;

        public LootCrateDefinition(LootCrateRarity lootCrateRarity, Rarity rarity, MaterialColor materialColor, int luminance, boolean hasTransparency, boolean fireProof) {
            this.identifier = lootCrateRarity.toString().toLowerCase();

            this.hasTransparency = hasTransparency;
            this.materialColor = materialColor;
            this.fireProof = fireProof;
            this.luminance = luminance;
            this.rarity = rarity;
            this.lockedTooltip = new TranslatableText("item.lootcrates.loot_crate.tooltip.locked_use_" + this.identifier);
            this.lockedConsumeTooltip = new TranslatableText("item.lootcrates.loot_crate.tooltip.locked_consume_" + this.identifier);
            this.lootKeyTooltip = new TranslatableText("item.lootcrates." + this.identifier + "_crate_key.tooltip");
            this.keyNeededTooltip = new TranslatableText("message.lootcrates." + this.identifier + "_key_needed_to_unlock");

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

        public FabricItemSettings getBlockItemSettings() {
            if(fireProof) {
                return new FabricItemSettings().group(LootCrates.ITEM_GROUP).rarity(rarity).fireproof();
            } else {
                return new FabricItemSettings().group(LootCrates.ITEM_GROUP).rarity(rarity);
            }
        }

        public FabricBlockSettings getChestBlockSettings() {
            FabricBlockSettings blockSettings = FabricBlockSettings.of(Material.METAL, materialColor).requiresTool().strength(-1.0F, 3600000.0F).dropsNothing().luminance(luminance);

            if(hasTransparency) {
                blockSettings = blockSettings.nonOpaque();
            }

            return blockSettings;
        }

        public FabricBlockSettings getShulkerBlockSettings() {
            FabricBlockSettings blockSettings = FabricBlockSettings.of(Material.SHULKER_BOX, materialColor).luminance(luminance);
            // shulker blocks are always opaque
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