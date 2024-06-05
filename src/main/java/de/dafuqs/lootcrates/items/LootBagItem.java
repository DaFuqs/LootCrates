package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.*;
import net.minecraft.client.item.*;
import net.minecraft.component.*;
import net.minecraft.component.type.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.loot.*;
import net.minecraft.loot.context.*;
import net.minecraft.registry.*;
import net.minecraft.server.network.*;
import net.minecraft.server.world.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class LootBagItem extends Item {
	
	public LootBagItem(Item.Settings fabricItemSettings) {
		super(fabricItemSettings);
	}
	
	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		super.appendTooltip(stack, context, tooltip, type);
		
		@Nullable ContainerLootComponent lootComponent = getLootComponent(stack);
		if (lootComponent == null) {
			tooltip.add(Text.translatable("item.lootcrates.loot_crate.tooltip.no_loot_table"));
		} else {
			tooltip.add(LootCrateAtlas.getLootBagItemToolTip(stack));
			tooltip.add(Text.translatable("item.lootcrates.loot_crate.tooltip.loot_table", lootComponent.lootTable().getValue().toString()));
		}
	}
	
	public @Nullable ContainerLootComponent getLootComponent(@NotNull ItemStack stack) {
		return stack.getComponents().get(DataComponentTypes.CONTAINER_LOOT);
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		if (user instanceof ServerPlayerEntity serverPlayerEntity) {
			ItemStack lootBagItemStack = user.getStackInHand(hand);
			List<ItemStack> loot = generateLoot(serverPlayerEntity, lootBagItemStack);
			for (ItemStack lootStack : loot) {
				serverPlayerEntity.getInventory().offerOrDrop(lootStack);
			}
			if (!serverPlayerEntity.isCreative()) {
				lootBagItemStack.decrement(1);
			}
		}
		return super.use(world, user, hand);
	}
	
	public List<ItemStack> generateLoot(ServerPlayerEntity player, ItemStack stack) {
		@Nullable ContainerLootComponent lootComponent = getLootComponent(stack);
		if (lootComponent != null) {
			ServerWorld serverWorld = player.getServerWorld();
			
			RegistryKey<LootTable> lootTableKey = lootComponent.lootTable();
			LootTable lootTable = serverWorld.getServer().getReloadableRegistries().getLootTable(lootTableKey);
			LootContextParameterSet.Builder builder = (new LootContextParameterSet.Builder(serverWorld)).add(LootContextParameters.ORIGIN, player.getPos());
			builder.luck(player.getLuck()).add(LootContextParameters.THIS_ENTITY, player);
			
			return lootTable.generateLoot(builder.build(LootContextTypes.CHEST), lootComponent.seed());
		}
		return List.of();
	}
	
}