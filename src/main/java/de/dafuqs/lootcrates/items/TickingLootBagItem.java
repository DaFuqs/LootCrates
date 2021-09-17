package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.enums.ScheduledTickEvent;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

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
