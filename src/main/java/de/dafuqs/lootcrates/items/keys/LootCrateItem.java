package de.dafuqs.lootcrates.items.keys;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class ShulkerCrateItem extends Item {

    public ShulkerCrateItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
        playerEntity.playSound(SoundEvents.BLOCK_WOOL_BREAK, 1.0F, 1.0F);
        return TypedActionResult.success(playerEntity.getStackInHand(hand));
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        super.appendTooltip(itemStack, world, tooltip, tooltipContext);
        CompoundTag compoundTag = itemStack.getSubTag("BlockEntityTag");
        if (compoundTag != null && compoundTag.contains("LootTable", 8)) {
            String lootTableText = compoundTag.getString("LootTable");
            tooltip.add(new LiteralText("Locked: " + lootTableText));
        } else {
            tooltip.add(new LiteralText("Unlocked"));
        }
    }

}