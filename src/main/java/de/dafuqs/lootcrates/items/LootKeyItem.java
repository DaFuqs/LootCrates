package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.LootCratesItems;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
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

}