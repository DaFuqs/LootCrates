package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.*;
import net.fabricmc.fabric.api.item.v1.*;
import net.minecraft.client.item.*;
import net.minecraft.item.*;
import net.minecraft.text.*;
import net.minecraft.world.*;

import java.util.*;

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