package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.LootCrateAtlas;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LootBagItem extends Item {

    public LootBagItem(FabricItemSettings fabricItemSettings) {
        super(fabricItemSettings);
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        super.appendTooltip(itemStack, world, tooltip, tooltipContext);
        tooltip.add(LootCrateAtlas.getLootBagItemToolTip(itemStack));

        Identifier lootTableIdentifier = getLootTableIdentifier(itemStack);
        if(lootTableIdentifier == null) {
            tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.no_loot_table"));
        } else {
            tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.loot_table", lootTableIdentifier.toString()));
        }
    }

    public static NbtCompound getItemCompoundTag(Identifier lootTable, int lootTableSeed) {
        NbtCompound compoundTag = new NbtCompound();

        compoundTag.putString("LootTable", lootTable.toString());
        if(lootTableSeed != 0) {
            compoundTag.putLong("LootTableSeed", lootTableSeed);
        }
        return compoundTag;
    }

    private @Nullable Identifier getLootTableIdentifier(@NotNull ItemStack itemStack) {
        NbtCompound nbtCompound = itemStack.getNbt();
        if (nbtCompound != null && nbtCompound.contains("LootTable")) {
            return Identifier.tryParse(nbtCompound.getString("LootTable"));
        } else {
            return null;
        }
    }

    private long getLootTableSeed(@NotNull ItemStack itemStack) {
        NbtCompound nbtCompound = itemStack.getNbt();
        if (nbtCompound != null && nbtCompound.contains("LootTableSeed")) {
            return nbtCompound.getLong("LootTableSeed");
        } else {
            return 0;
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if(world instanceof ServerWorld) {
            ItemStack lootBagItemStack = user.getStackInHand(hand);
            List<ItemStack> lootTableContents = getLootTableContents((ServerPlayerEntity) user, lootBagItemStack);
            for(ItemStack itemStack : lootTableContents) {
                givePlayerStackOrDrop((ServerPlayerEntity) user, itemStack);
            }
            lootBagItemStack.decrement(1);
        }
        return super.use(world, user, hand);
    }

    private List<ItemStack> getLootTableContents(ServerPlayerEntity player, ItemStack lootBagItemStack) {
        Identifier lootTableId = getLootTableIdentifier(lootBagItemStack);
        if(lootTableId != null) {
            LootTable lootTable = player.getServerWorld().getServer().getLootManager().getTable(lootTableId);
            if(lootTable != null) {
                LootContext.Builder builder = (new LootContext.Builder(player.getServerWorld()).parameter(LootContextParameters.ORIGIN, player.getPos())).random(getLootTableSeed(lootBagItemStack));
                builder.luck(player.getLuck()).parameter(LootContextParameters.THIS_ENTITY, player);
                return lootTable.generateLoot(builder.build(LootContextTypes.CHEST));
            }
        }
        return new ArrayList<>();
    }


    private void givePlayerStackOrDrop(@NotNull ServerPlayerEntity serverPlayerEntity, ItemStack itemStack) {
        boolean insertInventorySuccess = serverPlayerEntity.getInventory().insertStack(itemStack);
        ItemEntity itemEntity;
        if (insertInventorySuccess && itemStack.isEmpty()) {
            itemStack.setCount(1);
            itemEntity = serverPlayerEntity.dropItem(itemStack, false);
            if (itemEntity != null) {
                itemEntity.setDespawnImmediately();
            }

            serverPlayerEntity.world.playSound(null, serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((serverPlayerEntity.getRandom().nextFloat() - serverPlayerEntity.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            serverPlayerEntity.currentScreenHandler.sendContentUpdates();
        } else {
            itemEntity = serverPlayerEntity.dropItem(itemStack, false);
            if (itemEntity != null) {
                itemEntity.resetPickupDelay();
                itemEntity.setOwner(serverPlayerEntity.getUuid());
            }
        }

    }


}