package de.dafuqs.lootcrates.items;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class TickingLootCrateItem extends LootCrateItem implements LootCrateItemEffect {

    public TickingLootCrateItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        doInventoryTick(stack, world, entity);
    }

}
