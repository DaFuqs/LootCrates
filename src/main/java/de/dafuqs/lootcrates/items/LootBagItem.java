package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.*;
import net.minecraft.block.entity.*;
import net.minecraft.client.item.*;
import net.minecraft.component.*;
import net.minecraft.component.type.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.loot.*;
import net.minecraft.loot.context.*;
import net.minecraft.nbt.*;
import net.minecraft.registry.*;
import net.minecraft.server.network.*;
import net.minecraft.server.world.*;
import net.minecraft.sound.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class LootBagItem extends Item {

    public LootBagItem(Item.Settings fabricItemSettings) {
        super(fabricItemSettings);
    }
    
    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        
        @Nullable ContainerLootComponent lootComponent = getLootComponent(stack);
        if(lootComponent == null) {
            tooltip.add(Text.translatable("item.lootcrates.loot_crate.tooltip.no_loot_table"));
        } else {
            tooltip.add(LootCrateAtlas.getLootBagItemToolTip(stack));
            tooltip.add(Text.translatable("item.lootcrates.loot_crate.tooltip.loot_table", lootComponent.toString()));
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

    public @Nullable ContainerLootComponent getLootComponent(@NotNull ItemStack stack) {
        return stack.getComponents().get(DataComponentTypes.CONTAINER_LOOT);
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

    public List<ItemStack> getLootTableContents(ServerPlayerEntity player, ItemStack stack) {
        @Nullable ContainerLootComponent lootComponent = getLootComponent(stack);
        if(lootComponent != null) {
            ServerWorld serverWorld = player.getServerWorld();
            
            RegistryKey<LootTable> lootTableKey = lootComponent.lootTable();
            LootTable lootTable = serverWorld.getServer().getReloadableRegistries().getLootTable(lootTableKey);
            LootContextParameterSet.Builder builder = (new LootContextParameterSet.Builder(serverWorld)).add(LootContextParameters.ORIGIN, player.getPos());
			builder.luck(player.getLuck()).add(LootContextParameters.THIS_ENTITY, player);
   
			lootTable.generateLoot(builder.build(LootContextTypes.CHEST), lootComponent.seed());
        }
        return List.of();
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

            serverPlayerEntity.getWorld().playSound(null, serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((serverPlayerEntity.getRandom().nextFloat() - serverPlayerEntity.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
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