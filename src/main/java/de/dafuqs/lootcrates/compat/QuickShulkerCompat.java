package de.dafuqs.lootcrates.compat;

import de.dafuqs.lootcrates.LootCrateAtlas;
import de.dafuqs.lootcrates.LootCrates;
import de.dafuqs.lootcrates.blocks.LootCrateBlockEntity;
import de.dafuqs.lootcrates.blocks.PlayerCrateData;
import de.dafuqs.lootcrates.blocks.barrel.LootBarrelBlock;
import de.dafuqs.lootcrates.blocks.chest.ChestLootCrateBlock;
import de.dafuqs.lootcrates.blocks.modes.LockMode;
import de.dafuqs.lootcrates.blocks.modes.ReplenishMode;
import de.dafuqs.lootcrates.blocks.shulker.ShulkerLootCrateBlock;
import de.dafuqs.lootcrates.enums.LootCrateRarity;
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
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class QuickShulkerCompat implements RegisterQuickShulker {
	
	@Override
    public void registerProviders() {
        QuickOpenableRegistry.register(ShulkerLootCrateBlock.class, true, false, ((player, stack) -> {
            if (LootCrates.CONFIG.ShulkerCratesKeepTheirInventory) {
                doLogic(player, stack, "container.lootcrates.shulker_crate");
            }
        }));

        QuickOpenableRegistry.register(ChestLootCrateBlock.class, true, false, ((player, stack) -> {
            if (LootCrates.CONFIG.ChestCratesKeepTheirInventory) {
                doLogic(player, stack, "container.lootcrates.loot_crate");
            }
        }));
        
        QuickOpenableRegistry.register(LootBarrelBlock.class, true, false, ((player, stack) -> {
            if (LootCrates.CONFIG.LootBarrelsKeepTheirInventory) {
                doLogic(player, stack, "container.lootcrates.loot_barrel");
            }
        }));
    }
    
    public static void doLogic(PlayerEntity player, @NotNull ItemStack stack, String titleText) {
        NbtCompound compound = stack.getSubNbt("BlockEntityTag");
        if (compound != null) {
            boolean trackedPerPlayer = LootCrateItem.isTrackedPerPlayer(compound);
            Optional<PlayerCrateData> playerCrateData = LootCrateItem.getPlayerCrateData(compound, player, trackedPerPlayer);
            ReplenishMode replenishMode = LootCrateItem.getReplenishMode(compound);
            long replenishTimeTicks = LootCrateItem.getReplenishTimeTicks(compound);
            LockMode lockMode = LootCrateItem.getLockMode(compound);
    
            boolean unlocked = lockMode.isUnlocked(playerCrateData);
            boolean shouldRelock = false;
            if(unlocked) {
                if(LootCrateBlockEntity.shouldRelock(player.world, replenishMode, replenishTimeTicks, lockMode, playerCrateData)) {
                    shouldRelock = true;
                    unlocked = false;
                }
            }
            
            if (!unlocked) {
                if (LootCrateItem.consumeKey(player, stack)) {
                    LootCrateItem.unlockForPlayer(stack, player, trackedPerPlayer, replenishMode);
                    player.getEntityWorld().playSound(null, player.getBlockPos(), LootCrates.CHEST_UNLOCKS_SOUND_EVENT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                } else {
                    if(shouldRelock) {
                        LootCrateItem.lockForPlayer(stack, player, trackedPerPlayer);
                    }
                    printLockedMessage(player, stack);
                    player.getEntityWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.PLAYERS, 1.0F, 1.0F);
                }
            } else {
                boolean canReplenish = replenishMode.canReplenish(player.world, playerCrateData, replenishTimeTicks);
                if(canReplenish) {
                    doLootInteraction(stack, (ServerPlayerEntity) player);
                    LootCrateItem.setReplenishedForPlayer(stack, player, trackedPerPlayer, replenishMode);
                }
                player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) ->
                        new ShulkerBoxScreenHandler(i, player.getInventory(), ShulkerUtils.getInventoryFromShulker(stack)), stack.hasCustomName() ? stack.getName() : new TranslatableText(titleText)));
            }
        }
    }

    private static void printLockedMessage(PlayerEntity player, @NotNull ItemStack stack) {
        if (stack.getItem() instanceof BlockItem) {
            Block block = ((LootCrateItem) stack.getItem()).getBlock();
            LootCrateRarity rarity = LootCrateAtlas.getCrateRarity(block);
            TranslatableText translatableText = LootCrateAtlas.getKeyNeededTooltip(rarity);
            player.sendMessage(translatableText, false);
        }
    }

    private static void doLootInteraction(@NotNull ItemStack stack, ServerPlayerEntity player) {
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
        if (player != null && lootTableId != null && player.getServer() != null) {
            LootTable lootTable = player.getServer().getLootManager().getTable(lootTableId);

            Criteria.PLAYER_GENERATES_CONTAINER_LOOT.trigger( player, lootTableId);

            LootContext.Builder builder = (new LootContext.Builder((ServerWorld) player.world)).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(player.getBlockPos())).random(lootTableSeed);
            builder.luck(player.getLuck()).parameter(LootContextParameters.THIS_ENTITY, player);
            ItemStackInventory itemStackInventory = ShulkerUtils.getInventoryFromShulker(stack);
            lootTable.supplyInventory(itemStackInventory, builder.build(LootContextTypes.CHEST));
            itemStackInventory.onClose(player);
        }
    }
    
}