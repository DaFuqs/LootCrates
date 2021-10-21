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
import net.kyrptonaught.shulkerutils.ItemStackInventory;
import net.kyrptonaught.shulkerutils.ShulkerUtils;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuickShulkerCompat implements RegisterQuickShulker {
    @Override
    public void registerProviders() {
        QuickOpenableRegistry.register(ShulkerLootCrateBlock.class, true, false, ((player, stack) -> {
            if (LootCrates.CONFIG.ShulkerCratesKeepTheirInventory) {
                if (isLocked(stack)) {
                    if(consumeKey(player, LootCrateAtlas.getCrateItemRarity(stack.getItem()))) {
                        unlock(player, stack);
                    } else {
                        printLockedMessage(player, stack);
                    }
                } else {
                    checkLootInteraction(stack, (ServerPlayerEntity) player);
                    player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) ->
                            new ShulkerBoxScreenHandler(i, player.getInventory(), ShulkerUtils.getInventoryFromShulker(stack)), stack.hasCustomName() ? stack.getName() : new TranslatableText("container.lootcrates.shulker_crate")));
                }
            }
        }));

        QuickOpenableRegistry.register(ChestLootCrateBlock.class, true, false, ((player, stack) -> {
            if (LootCrates.CONFIG.ChestCratesKeepTheirInventory) {
                if (isLocked(stack)) {
                    if(consumeKey(player, LootCrateAtlas.getCrateItemRarity(stack.getItem()))) {
                        unlock(player, stack);
                    } else {
                        printLockedMessage(player, stack);
                    }
                } else {
                    checkLootInteraction(stack, (ServerPlayerEntity) player);
                    player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) ->
                            new ShulkerBoxScreenHandler(i, player.getInventory(), ShulkerUtils.getInventoryFromShulker(stack)), stack.hasCustomName() ? stack.getName() : new TranslatableText("container.lootcrates.loot_crate")));
                }
            }
        }));
    }

    private boolean isLocked(ItemStack stack) {
        NbtCompound tag = stack.getSubNbt("BlockEntityTag");
        if (tag == null || !tag.contains(LootCrateTagNames.Locked.toString())) return false;
        return tag.getBoolean(LootCrateTagNames.Locked.toString());
    }

    private void unlock(PlayerEntity player, ItemStack stack) {
        player.getEntityWorld().playSound(null, player.getBlockPos(), LootCrates.CHEST_UNLOCKS_SOUND_EVENT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        NbtCompound tag = stack.getSubNbt("BlockEntityTag");
        if (tag != null) {
            tag.putBoolean(LootCrateTagNames.Locked.toString(), false);
            tag.putLong(LootCrateTagNames.LastUnlockTimeTick.toString(), player.getEntityWorld().getTime());
        }
    }

    public void checkRelock(PlayerEntity player, ItemStack stack) {
        NbtCompound tag = stack.getSubNbt("BlockEntityTag");
        if(tag.contains(LootCrateTagNames.RelocksWhenNewLoot.toString()) && tag.getBoolean(LootCrateTagNames.RelocksWhenNewLoot.toString())) {
            if(tag.contains(LootCrateTagNames.Locked.toString()) && !tag.getBoolean(LootCrateTagNames.Locked.toString())) {
                long lastUnlockTimeTick = 0;
                long lastReplenishTimeTick = 0;
                if(tag.contains(LootCrateTagNames.LastUnlockTimeTick.toString())) {
                    lastUnlockTimeTick = tag.getLong(LootCrateTagNames.LastUnlockTimeTick.toString());
                }
                if(tag.contains(LootCrateTagNames.LastReplenishTimeTick.toString())) {
                    lastReplenishTimeTick = tag.getLong(LootCrateTagNames.LastReplenishTimeTick.toString());
                }
                if(lastUnlockTimeTick < lastReplenishTimeTick && shouldGenerateNewLoot(stack, player, true)) {
                    tag.putBoolean(LootCrateTagNames.Locked.toString(), true);
                }
            }
        }
    }

    private boolean consumeKey(PlayerEntity player, LootCrateRarity lootCrateRarity) {
        if(player.isCreative()) {
            return true;
        } else {
            ItemStack lootKeyItemStack = new ItemStack(LootCrateAtlas.getLootKeyItem(lootCrateRarity));
            if (player.getInventory().contains(lootKeyItemStack)) {
                int slot = player.getInventory().getSlotWithStack(lootKeyItemStack);
                player.getInventory().getStack(slot).decrement(1);
                return true;
            }
        }
        return false;
    }

    private void printLockedMessage(PlayerEntity player, ItemStack stack) {
        if (stack.getItem() instanceof BlockItem) {
            Block block = ((LootCrateItem) stack.getItem()).getBlock();
            LootCrateRarity rarity = LootCrateAtlas.getCrateRarity(block);
            TranslatableText translatableText = LootCrateAtlas.getKeyNeededTooltip(rarity);
            player.sendMessage(translatableText, false);
        }
    }

    private void checkLootInteraction(ItemStack stack, ServerPlayerEntity player) {
        Identifier lootTableId = null;
        long lootTableSeed = 0;

        NbtCompound tag = stack.getSubNbt("BlockEntityTag");
        if (tag != null) {
            if (tag.contains("LootTable", 8)) {
                lootTableId = new Identifier(tag.getString("LootTable"));
                lootTableSeed = tag.getLong("LootTableSeed");
            }
        }

        // only players can generate container loot
        if (player != null && lootTableId != null && player.getServer() != null && shouldGenerateNewLoot(stack, player, false)) {
            LootTable lootTable = player.getServer().getLootManager().getTable(lootTableId);

            Criteria.PLAYER_GENERATES_CONTAINER_LOOT.test( player, lootTableId);

            LootContext.Builder builder = (new LootContext.Builder((ServerWorld) player.world)).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(player.getBlockPos())).random(lootTableSeed);
            builder.luck(player.getLuck()).parameter(LootContextParameters.THIS_ENTITY, player);
            ItemStackInventory itemStackInventory = ShulkerUtils.getInventoryFromShulker(stack);
            lootTable.supplyInventory(itemStackInventory, builder.build(LootContextTypes.CHEST));
            itemStackInventory.onClose(player);
        }
    }

    public boolean shouldGenerateNewLoot(ItemStack stack, PlayerEntity player, boolean test) {
        long replenishTimeTicks = -1;
        long lastReplenishTimeTick = 0;
        boolean oncePerPlayer = false;
        List<UUID> registeredPlayerUUIDs = new ArrayList<>();
        NbtCompound tag = stack.getSubNbt("BlockEntityTag");
        if (tag != null) {
            if (tag.contains(LootCrateTagNames.ReplenishTimeTicks.toString()))
                replenishTimeTicks = tag.getLong(LootCrateTagNames.ReplenishTimeTicks.toString());

            if (tag.contains(LootCrateTagNames.LastReplenishTimeTick.toString()))
                lastReplenishTimeTick = tag.getLong(LootCrateTagNames.LastReplenishTimeTick.toString());

            if (tag.contains(LootCrateTagNames.OncePerPlayer.toString()) && tag.getBoolean(LootCrateTagNames.OncePerPlayer.toString())) {
                oncePerPlayer = true;
                if(!test) {
                    if (tag.contains(LootCrateTagNames.RegisteredPlayerUUIDs.toString())) {
                        NbtList playerUUIDs = tag.getList(LootCrateTagNames.RegisteredPlayerUUIDs.toString(), 11);
                        for (NbtElement playerUUID : playerUUIDs) {
                            registeredPlayerUUIDs.add(NbtHelper.toUuid(playerUUID));
                        }
                    }
                }
            }
        }

        // if replenish time is set to <=0: just generate loot once
        if (replenishTimeTicks <= 0) {
            if (lastReplenishTimeTick == 0) {
                lastReplenishTimeTick = player.world.getTime();
                if(!test) {
                    saveNbtToStack(stack, lastReplenishTimeTick, oncePerPlayer, registeredPlayerUUIDs);
                }
                return true;
            } else {
                return false;
            }
        } else {
            // check if there was enough time since the last opening
            if (lastReplenishTimeTick == 0 || player.world.getTime() > lastReplenishTimeTick + replenishTimeTicks) {
                if (oncePerPlayer) {
                    if (registeredPlayerUUIDs.contains(player.getUuid())) {
                        return false;
                    } else {
                        if(!test) {
                            lastReplenishTimeTick = player.world.getTime();
                            registeredPlayerUUIDs.add(player.getUuid());
                            saveNbtToStack(stack, lastReplenishTimeTick, oncePerPlayer, registeredPlayerUUIDs);
                        }
                        return true;
                    }
                } else {
                    if(!test) {
                        lastReplenishTimeTick = player.world.getTime();
                        saveNbtToStack(stack, lastReplenishTimeTick, oncePerPlayer, registeredPlayerUUIDs);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void saveNbtToStack(ItemStack stack, long lastReplenishTimeTick, boolean oncePerPlayer, List<UUID> registeredPlayerUUIDs) {
        NbtCompound tag = stack.getSubNbt("BlockEntityTag");
        if (tag != null) {
            if (lastReplenishTimeTick > 0) {
                tag.putLong(LootCrateTagNames.LastReplenishTimeTick.toString(), lastReplenishTimeTick);
            }
            if (oncePerPlayer) {
                tag.putBoolean(LootCrateTagNames.OncePerPlayer.toString(), true);
                if (registeredPlayerUUIDs.size() > 0) {
                    NbtList registeredPlayers = new NbtList();
                    for (UUID uuid : registeredPlayerUUIDs) {
                        registeredPlayers.add(NbtHelper.fromUuid(uuid));
                    }
                    tag.put(LootCrateTagNames.RegisteredPlayerUUIDs.toString(), registeredPlayers);
                }
            }
        }
    }
}