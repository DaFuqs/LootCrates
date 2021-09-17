package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.enums.ScheduledTickEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class TickingLootCrateItem extends LootCrateItem implements LootCrateItemEffect {

    ScheduledTickEvent scheduledTickEvent;

    public TickingLootCrateItem(Block block, Settings settings, ScheduledTickEvent scheduledTickEvent) {
        super(block, settings);
        this.scheduledTickEvent = scheduledTickEvent;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        doInventoryTick(world, entity, this.scheduledTickEvent);
    }

}
