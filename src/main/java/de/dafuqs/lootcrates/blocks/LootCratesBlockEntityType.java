package de.dafuqs.lootcrates.blocks;

import com.mojang.datafixers.types.Type;
import de.dafuqs.lootcrates.LootCrateAtlas;
import de.dafuqs.lootcrates.blocks.chest.ChestLootCrateBlockEntity;
import de.dafuqs.lootcrates.blocks.shulker.ShulkerLootCrateBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

public class LootCratesBlockEntityType {

    public static BlockEntityType<ChestLootCrateBlockEntity> CHEST_LOOT_CRATE_BLOCK_ENTITY;
    public static BlockEntityType<ShulkerLootCrateBlockEntity> SHULKER_LOOT_CRATE_BLOCK_ENTITY;

    private static <T extends BlockEntity> BlockEntityType<T> create(String string, BlockEntityType.Builder<T> builder) {
        Type<?> type = Util.getChoiceType(TypeReferences.BLOCK_ENTITY, string);
        return Registry.register(Registry.BLOCK_ENTITY_TYPE, string, builder.build(type));
    }

    public static void register() {
        CHEST_LOOT_CRATE_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                "lootcrates:chest_loot_crate_block_entity",
                FabricBlockEntityTypeBuilder.create(ChestLootCrateBlockEntity::new, LootCrateAtlas.getChestCrates())
                .build(null));

        SHULKER_LOOT_CRATE_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                "lootcrates:shulker_loot_crate_block_entity",
                FabricBlockEntityTypeBuilder.create(ShulkerLootCrateBlockEntity::new, LootCrateAtlas.getShulkerCrates())
                        .build(null));
    }

}
