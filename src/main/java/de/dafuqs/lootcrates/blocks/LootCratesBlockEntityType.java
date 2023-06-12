package de.dafuqs.lootcrates.blocks;

import de.dafuqs.lootcrates.*;
import de.dafuqs.lootcrates.blocks.barrel.*;
import de.dafuqs.lootcrates.blocks.chest.*;
import de.dafuqs.lootcrates.blocks.shulker.*;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.*;
import net.minecraft.block.entity.*;
import net.minecraft.registry.*;
import net.minecraft.util.*;

public class LootCratesBlockEntityType {

    public static Identifier CHEST_LOOT_CRATE_BLOCK_ENTITY_IDENTIFIER = new Identifier(LootCrates.MOD_ID, "chest_loot_crate_block_entity");
    public static Identifier SHULKER_LOOT_CRATE_BLOCK_ENTITY_IDENTIFIER = new Identifier(LootCrates.MOD_ID, "shulker_loot_crate_block_entity");
    public static Identifier LOOT_BARREL_BLOCK_ENTITY_IDENTIFIER = new Identifier(LootCrates.MOD_ID, "loot_barrel_block_entity");

    public static BlockEntityType<ChestLootCrateBlockEntity> CHEST_LOOT_CRATE_BLOCK_ENTITY;
    public static BlockEntityType<ShulkerLootCrateBlockEntity> SHULKER_LOOT_CRATE_BLOCK_ENTITY;
    public static BlockEntityType<LootBarrelBlockEntity> LOOT_BARREL_BLOCK_ENTITY;

    public static void register() {
        CHEST_LOOT_CRATE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, CHEST_LOOT_CRATE_BLOCK_ENTITY_IDENTIFIER,
                FabricBlockEntityTypeBuilder.create(ChestLootCrateBlockEntity::new, LootCrateAtlas.getChestCrates()).build(null));

        SHULKER_LOOT_CRATE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, SHULKER_LOOT_CRATE_BLOCK_ENTITY_IDENTIFIER,
                FabricBlockEntityTypeBuilder.create(ShulkerLootCrateBlockEntity::new, LootCrateAtlas.getShulkerCrates()).build(null));

        LOOT_BARREL_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, LOOT_BARREL_BLOCK_ENTITY_IDENTIFIER,
                FabricBlockEntityTypeBuilder.create(LootBarrelBlockEntity::new, LootCrateAtlas.getLootBarrels()).build(null));
    }

}
