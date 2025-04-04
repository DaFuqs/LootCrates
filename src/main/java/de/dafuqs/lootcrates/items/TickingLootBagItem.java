package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.enums.*;
import net.minecraft.entity.*;
import net.minecraft.item.*;
import net.minecraft.server.world.*;
import org.jetbrains.annotations.*;

public class TickingLootBagItem extends LootBagItem implements LootCrateItemEffect {
	
	protected final ScheduledTickEvent scheduledTickEvent;
	
	public TickingLootBagItem(Item.Settings fabricItemSettings, ScheduledTickEvent scheduledTickEvent) {
		super(fabricItemSettings);
		this.scheduledTickEvent = scheduledTickEvent;
	}
	
	@Override
	public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
		super.inventoryTick(stack, world, entity, slot);
		
		doInventoryTick(world, entity, this.scheduledTickEvent);
	}
	
}
