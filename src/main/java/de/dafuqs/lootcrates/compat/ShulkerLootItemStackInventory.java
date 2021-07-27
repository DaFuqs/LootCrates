package de.dafuqs.lootcrates.compat;

import net.kyrptonaught.shulkerutils.ItemStackInventory;
import net.minecraft.item.ItemStack;

public class ShulkerLootItemStackInventory extends ItemStackInventory {
    public ShulkerLootItemStackInventory(ItemStack stack, int SIZE) {
        super(stack, SIZE);
    }

    //prevents ShulkeUtils from clearing the BlockEntityTag. ShulkerUtils resets the tag when the stack's items are empty, this doesn't make sense in this scenario. This is only used when the item was "quickly" opened
    @Override
    public boolean isEmpty() {
        return false;
    }
}
