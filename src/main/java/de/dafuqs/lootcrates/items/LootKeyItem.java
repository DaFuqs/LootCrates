package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.LootCrateAtlas;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

public class LootKeyItem extends Item {

    public LootKeyItem(FabricItemSettings fabricItemSettings) {
        super(fabricItemSettings);
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        super.appendTooltip(itemStack, world, tooltip, tooltipContext);

        tooltip.add(LootCrateAtlas.getLootKeyItemToolTip(itemStack));
    }

}