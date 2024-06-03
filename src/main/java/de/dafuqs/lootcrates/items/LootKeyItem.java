package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.*;
import net.fabricmc.fabric.api.item.v1.*;
import net.minecraft.client.item.*;
import net.minecraft.item.*;
import net.minecraft.text.*;
import net.minecraft.world.*;

import java.util.*;

public class LootKeyItem extends Item {

    public LootKeyItem(Item.Settings settings) {
        super(settings);
    }
    
    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        
        tooltip.add(LootCrateAtlas.getLootKeyItemToolTip(stack));
    }

}