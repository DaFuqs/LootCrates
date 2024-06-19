package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.*;
import de.dafuqs.lootcrates.enums.*;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.*;
import net.minecraft.text.*;

import java.util.*;

public class LootKeyItem extends Item {
	
	protected final LootCrateVariant variant;
	
	public LootKeyItem(LootCrateVariant variant, Item.Settings settings) {
		super(settings);
		this.variant = variant;
	}
	
	public LootCrateVariant getVariant() {
		return this.variant;
	}
	
	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		super.appendTooltip(stack, context, tooltip, type);
		
		tooltip.add(LootCrateAtlas.getLootKeyItemToolTip(stack));
	}
}