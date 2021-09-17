package de.dafuqs.lootcrates.items;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class TickingLootBagItem extends LootBagItem implements LootCrateItemEffect {

    public TickingLootBagItem(FabricItemSettings fabricItemSettings) {
        super(fabricItemSettings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        doInventoryTick(stack, world, entity);
    }

}
