package de.dafuqs.lootcrates.blocks;

import com.mojang.datafixers.types.Type;
import de.dafuqs.lootcrates.LootCratesBlocks;
import de.dafuqs.lootcrates.blocks.lootcrate.LootCrateBlockEntity;
import de.dafuqs.lootcrates.blocks.shulkercrate.ShulkerLootCrateBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

public class LootCratesBlockEntityType<T extends BlockEntity> {

    public static BlockEntityType<LootCrateBlockEntity> LOOT_CRATE_BLOCK_ENTITY;
    public static BlockEntityType<ShulkerLootCrateBlockEntity> SHULKER_LOOT_CRATE_BLOCK_ENTITY;

    private static <T extends BlockEntity> BlockEntityType<T> create(String string, BlockEntityType.Builder<T> builder) {
        Type<?> type = Util.getChoiceType(TypeReferences.BLOCK_ENTITY, string);
        return Registry.register(Registry.BLOCK_ENTITY_TYPE, string, builder.build(type));
    }

    public static void register() {
        LOOT_CRATE_BLOCK_ENTITY = create("lootcrates:loot_crate_block_entity",
                BlockEntityType.Builder.create(
                        LootCrateBlockEntity::new,
                        LootCratesBlocks.COMMON_LOOT_CRATE,
                        LootCratesBlocks.UNCOMMON_LOOT_CRATE,
                        LootCratesBlocks.RARE_LOOT_CRATE,
                        LootCratesBlocks.EPIC_LOOT_CRATE));

        SHULKER_LOOT_CRATE_BLOCK_ENTITY = create("lootcrates:shulker_loot_crate_block_entity",
                BlockEntityType.Builder.create(
                        ShulkerLootCrateBlockEntity::new,
                        LootCratesBlocks.COMMON_SHULKER_LOOT_CRATE,
                        LootCratesBlocks.UNCOMMON_SHULKER_LOOT_CRATE,
                        LootCratesBlocks.RARE_SHULKER_LOOT_CRATE,
                        LootCratesBlocks.EPIC_SHULKER_LOOT_CRATE));
    }

}
