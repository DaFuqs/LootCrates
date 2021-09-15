package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.LootCrateAtlas;
import de.dafuqs.lootcrates.enums.LootCrateTagNames;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class LootCrateItem extends BlockItem {

    public LootCrateItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public boolean canBeNested() {
        return false;
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        super.appendTooltip(itemStack, world, tooltip, tooltipContext);

        NbtCompound compound = itemStack.getSubNbt("BlockEntityTag");
        if (compound != null) {

            // lock
            boolean locked = false;
            if (compound.contains(LootCrateTagNames.Locked.toString()) && compound.getBoolean(LootCrateTagNames.Locked.toString())) {
                locked = true;
                tooltip.add(LootCrateAtlas.getItemLockedTooltip(itemStack, compound));
            }

            boolean advanced = tooltipContext.isAdvanced();
            long replenishTimeTicks = 0;
            if (compound.contains(LootCrateTagNames.ReplenishTimeTicks.toString())) {
                replenishTimeTicks = compound.getLong(LootCrateTagNames.ReplenishTimeTicks.toString());
            }

            boolean oncePerPlayer = compound.contains(LootCrateTagNames.OncePerPlayer.toString()) && compound.getBoolean(LootCrateTagNames.OncePerPlayer.toString());
            boolean wasOpened = compound.contains(LootCrateTagNames.LastReplenishTimeTick.toString()) && compound.getLong(LootCrateTagNames.LastReplenishTimeTick.toString()) != 0;

            if (replenishTimeTicks <= 0 && !oncePerPlayer && wasOpened) {
                // cannot generate more loot
                tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.already_looted"));
            } else {
                tooltip.add(getReplenishTimeHumanReadableText(replenishTimeTicks));

                // oncePerPlayer really is only useful when replenish time is positive
                if (oncePerPlayer && replenishTimeTicks > 0) {
                    boolean playerHasAlreadyOpened = false;
                    if(compound.contains(LootCrateTagNames.RegisteredPlayerUUIDs.toString())) {
                        NbtList playerUUIDsTag = compound.getList(LootCrateTagNames.RegisteredPlayerUUIDs.toString(), 11);
                        if(playerUUIDsTag.size() > 0) {
                            UUID playerUUID = world.getPlayers().get(0).getUuid(); // the current player in singleplayer
                            for(NbtElement currentUUID : playerUUIDsTag) {
                                if (playerUUID.equals(NbtHelper.toUuid(currentUUID))) {
                                    playerHasAlreadyOpened = true;
                                    break;
                                }
                            }
                        }
                    }

                    if(playerHasAlreadyOpened) {
                        tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.once_per_player_already_opened_by_you"));
                    } else {
                        tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.once_per_player"));
                    }
                }

                if(advanced) {
                    if (compound.contains("LootTable")) {
                        String lootTableText = compound.getString("LootTable");
                        tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.loot_table", lootTableText));
                    }
                    if (compound.contains("LootTableSeed")) {
                        long lootTableSeed = compound.getLong("LootTableSeed");
                        tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.fixed_seed", lootTableSeed));
                    }
                }

            }

            if(!locked) {
                if (compound.contains("Items", 9)) {
                    DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(27, ItemStack.EMPTY);
                    Inventories.readNbt(compound, defaultedList);
                    int i = 0;
                    int j = 0;

                    for (ItemStack is : defaultedList) {
                        if (!is.isEmpty()) {
                            ++j;
                            if (i <= 4) {
                                ++i;
                                MutableText mutableText = is.getName().shallowCopy();
                                mutableText.append(" x").append(String.valueOf(is.getCount()));
                                tooltip.add(mutableText);
                            }
                        }
                    }

                    if (j - i > 0) {
                        tooltip.add((new TranslatableText("container.shulkerBox.more", j - i)).formatted(Formatting.ITALIC));
                    }
                }
            }

        } else {
            tooltip.add(new TranslatableText("item.lootcrates.loot_crate.tooltip.no_data_set"));
        }
    }

    @Contract("_ -> new")
    private @NotNull TranslatableText getReplenishTimeHumanReadableText(long replenishTime) {
        if(replenishTime >= 1728000) { // 1 day
            return new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_time_days", replenishTime / 1728000F);
        } else if(replenishTime >= 72000) { // 1 hour
            return new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_time_hours", replenishTime / 72000F);
        } else if(replenishTime >= 1200) { // 1 minute
            return new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_time_minutes", replenishTime / 1200F);
        } else if(replenishTime <= 0) {
            // does not replenish
            return new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_time_once");
        } else { // in ticks
            return new TranslatableText("item.lootcrates.loot_crate.tooltip.replenish_time_ticks", replenishTime);
        }
    }

    public static @NotNull NbtCompound getLootCrateItemCompoundTag(@NotNull Identifier lootTable, boolean locked, boolean doNotConsumeKeyOnUnlock, long lootGenerationTimeInTicks, long lootTableSeed, boolean oncePerPlayer) {
        NbtCompound compoundTag = new NbtCompound();
        NbtCompound blockEntityTag = new NbtCompound();

        blockEntityTag.putString("LootTable", lootTable.toString());
        if(locked) {
            blockEntityTag.putBoolean(LootCrateTagNames.Locked.toString(), true);
            if(doNotConsumeKeyOnUnlock) {
                blockEntityTag.putBoolean(LootCrateTagNames.DoNotConsumeKeyOnUnlock.toString(), true);
            }
        }

        if(lootGenerationTimeInTicks > 0) {
            blockEntityTag.putLong(LootCrateTagNames.ReplenishTimeTicks.toString(), lootGenerationTimeInTicks);
        }
        if(lootTableSeed != 0) {
            blockEntityTag.putLong("LootTableSeed", lootTableSeed);
        }
        if(oncePerPlayer) {
            blockEntityTag.putBoolean(LootCrateTagNames.OncePerPlayer.toString(), true);
        }

        compoundTag.put("BlockEntityTag", blockEntityTag);
        return compoundTag;
    }

}
