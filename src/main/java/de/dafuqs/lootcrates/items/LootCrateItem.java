package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.components.*;
import de.dafuqs.lootcrates.enums.*;
import net.fabricmc.api.*;
import net.minecraft.block.*;
import net.minecraft.client.*;
import net.minecraft.component.*;
import net.minecraft.component.type.*;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;

public class LootCrateItem extends BlockItem {
	
	private static final Text UNKNOWN_CONTENTS_TEXT = Text.translatable("container.shulkerBox.unknownContents");
	protected final LootCrateVariant variant;
	
	public LootCrateItem(LootCrateVariant variant, Block block, Settings settings) {
		super(block, settings);
		this.variant = variant;
	}
	
	public LootCrateVariant getVariant() {
		return variant;
	}
	
	@Override
	public boolean canBeNested() {
		return false;
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
		super.appendTooltip(stack, context, displayComponent, textConsumer, type);
		
		boolean isUnlocked = true;
		LootCrateLootComponent lootComponent = stack.get(LootCrateDataComponentTypes.LOOT_CRATE_LOOT);
		
		if (lootComponent != null) {
			@Nullable LootCratePlayerDataComponent playerComponent = stack.get(LootCrateDataComponentTypes.LOOT_CRATE_PLAYER_DATA);
			Optional<PlayerCrateData> playerCrateData = playerComponent == null ? Optional.empty() : playerComponent.get(MinecraftClient.getInstance().player, lootComponent.trackedPerPlayer());
			lootComponent.appendTooltip(variant, playerCrateData, MinecraftClient.getInstance().world, context, textConsumer, type);
			isUnlocked = lootComponent.isUnlocked(playerCrateData);
		} else {
			textConsumer.accept(Text.translatable("item.lootcrates.loot_crate.tooltip.no_data_set"));
		}
		
		// inventory
		if (isUnlocked) {
			if (stack.contains(DataComponentTypes.CONTAINER_LOOT)) {
				textConsumer.accept(UNKNOWN_CONTENTS_TEXT);
			}
			
			int i = 0;
			int j = 0;
			for (ItemStack containedStack : (stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT)).iterateNonEmpty()) {
				++j;
				if (i <= 4) {
					++i;
					textConsumer.accept(Text.translatable("container.shulkerBox.itemCount", containedStack.getName(), containedStack.getCount()));
				}
			}
			
			if (j - i > 0) {
				textConsumer.accept(Text.translatable("container.shulkerBox.more", j - i).formatted(Formatting.ITALIC));
			}
		}
	}
	
}
