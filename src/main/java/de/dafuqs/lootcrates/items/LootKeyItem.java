package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.*;
import de.dafuqs.lootcrates.enums.*;
import net.minecraft.component.type.*;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.*;
import net.minecraft.text.*;

import java.util.function.*;

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
	public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
		super.appendTooltip(stack, context, displayComponent, textConsumer, type);
		
		textConsumer.accept(LootCrateAtlas.getLootKeyItemToolTip(stack));
	}
}