package de.dafuqs.lootcrates.compat;

import de.dafuqs.lootcrates.LootCrateAtlas;
import de.dafuqs.lootcrates.LootCrates;
import de.dafuqs.lootcrates.blocks.chest.ChestLootCrateBlock;
import de.dafuqs.lootcrates.blocks.shulker.ShulkerLootCrateBlock;
import de.dafuqs.lootcrates.enums.LootCrateRarity;
import de.dafuqs.lootcrates.enums.LootCrateTagNames;
import de.dafuqs.lootcrates.items.LootCrateItem;
import net.kyrptonaught.quickshulker.api.QuickOpenableRegistry;
import net.kyrptonaught.quickshulker.api.RegisterQuickShulker;
import net.kyrptonaught.shulkerutils.ShulkerUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.TranslatableText;

public class QuickShulkerCompat implements RegisterQuickShulker {
    @Override
    public void registerProviders() {
            QuickOpenableRegistry.register(ShulkerLootCrateBlock.class, true, false, ((player, stack) -> {
                if(LootCrates.CONFIG.ShulkerCratesKeepTheirInventory) {
                    if (isLocked(stack)) {
                        printLockedMessage(player, stack);
                    } else
                        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) ->
                                new ShulkerBoxScreenHandler(i, player.getInventory(), new ShulkerLootItemStackInventory(stack, 27)), stack.hasCustomName() ? stack.getName() : new TranslatableText("container.lootcrates.shulker_crate")));
            }}));

            QuickOpenableRegistry.register(ChestLootCrateBlock.class, true, false, ((player, stack) -> {
                if(LootCrates.CONFIG.ChestCratesKeepTheirInventory) {
                    if (isLocked(stack)) {
                        printLockedMessage(player, stack);
                    } else
                        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) ->
                                new ShulkerBoxScreenHandler(i, player.getInventory(), new ShulkerLootItemStackInventory(stack, 27)), stack.hasCustomName() ? stack.getName() : new TranslatableText("container.lootcrates.loot_crate")));
            }}));
    }

    private boolean isLocked(ItemStack stack) {
        NbtCompound tag = stack.getSubNbt("BlockEntityTag");
        if (tag == null || !tag.contains(LootCrateTagNames.Locked.toString())) return false;
        return tag.getBoolean(LootCrateTagNames.Locked.toString());
    }

    private void printLockedMessage(PlayerEntity player, ItemStack stack) {
        if (stack.getItem() instanceof BlockItem) {
            Block block = ((LootCrateItem) stack.getItem()).getBlock();
            LootCrateRarity rarity = LootCrateAtlas.getCrateRarity(block);
            TranslatableText translatableText = LootCrateAtlas.getKeyNeededTooltip(rarity);
            player.sendMessage(translatableText, false);
        }
    }

}