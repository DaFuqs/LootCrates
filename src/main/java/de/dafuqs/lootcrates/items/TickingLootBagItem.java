package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.enums.*;
import net.fabricmc.fabric.api.item.v1.*;
import net.minecraft.entity.*;
import net.minecraft.item.*;
import net.minecraft.world.*;

public class TickingLootBagItem extends LootBagItem implements LootCrateItemEffect {

    ScheduledTickEvent scheduledTickEvent;

    public TickingLootBagItem(FabricItemSettings fabricItemSettings, ScheduledTickEvent scheduledTickEvent) {
        super(fabricItemSettings);
        this.scheduledTickEvent = scheduledTickEvent;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        doInventoryTick(world, entity, this.scheduledTickEvent);
    }

}
