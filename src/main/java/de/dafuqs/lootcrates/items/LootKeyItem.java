package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.LootCratesItems;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

import java.util.List;

public class LootKeyItem extends Item {

    public LootKeyItem(FabricItemSettings fabricItemSettings) {
        super(fabricItemSettings);
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        super.appendTooltip(itemStack, world, tooltip, tooltipContext);

        if (LootCratesItems.COMMON_CRATE_KEY.equals(itemStack.getItem())) {
            tooltip.add(new TranslatableText("item.lootcrates.common_crate_key.tooltip"));
        } else if (LootCratesItems.UNCOMMON_CRATE_KEY.equals(itemStack.getItem())) {
            tooltip.add(new TranslatableText("item.lootcrates.uncommon_crate_key.tooltip"));
        } else if (LootCratesItems.RARE_CRATE_KEY.equals(itemStack.getItem())) {
            tooltip.add(new TranslatableText("item.lootcrates.rare_crate_key.tooltip"));
        } else if (LootCratesItems.EPIC_CRATE_KEY.equals(itemStack.getItem())) {
            tooltip.add(new TranslatableText("item.lootcrates.epic_crate_key.tooltip"));
        }
    }

    public static Rarity getKeyRarity(LootKeyItem item) {
        if (item.equals(LootCratesItems.COMMON_CRATE_KEY)) {
            return Rarity.COMMON;
        } else if (item.equals(LootCratesItems.UNCOMMON_CRATE_KEY)) {
            return Rarity.UNCOMMON;
        } else if (item.equals(LootCratesItems.RARE_CRATE_KEY)) {
            return Rarity.RARE;
        } else if (item.equals(LootCratesItems.EPIC_CRATE_KEY)) {
            return Rarity.EPIC;
        } else {
            return Rarity.COMMON;
        }
    }

}