package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.blocks.chest.ChestLootCrateBlock;
import de.dafuqs.lootcrates.blocks.shulker.ShulkerLootCrateBlock;
import de.dafuqs.lootcrates.items.LootCrateItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class LootCratesBlocks {

    private static final FabricBlockSettings blockSettingsChestLootCrate = FabricBlockSettings.of(Material.METAL).requiresTool().strength(-1.0F, 3600000.0F).dropsNothing();
    private static final FabricBlockSettings blockSettingsShulkerLootCrate = FabricBlockSettings.of(Material.SHULKER_BOX).requiresTool().strength(2.0F).nonOpaque();

    public static final Block COMMON_CHEST_LOOT_CRATE = new ChestLootCrateBlock(blockSettingsChestLootCrate);
    public static final Block UNCOMMON_CHEST_LOOT_CRATE = new ChestLootCrateBlock(blockSettingsChestLootCrate);
    public static final Block RARE_CHEST_LOOT_CRATE = new ChestLootCrateBlock(blockSettingsChestLootCrate);
    public static final Block EPIC_CHEST_LOOT_CRATE = new ChestLootCrateBlock(blockSettingsChestLootCrate);
    public static final Block COMMON_SHULKER_LOOT_CRATE = new ShulkerLootCrateBlock(blockSettingsShulkerLootCrate);
    public static final Block UNCOMMON_SHULKER_LOOT_CRATE = new ShulkerLootCrateBlock(blockSettingsShulkerLootCrate);
    public static final Block RARE_SHULKER_LOOT_CRATE = new ShulkerLootCrateBlock(blockSettingsShulkerLootCrate);
    public static final Block EPIC_SHULKER_LOOT_CRATE = new ShulkerLootCrateBlock(blockSettingsShulkerLootCrate);

    private static void registerLootCrateBlock(String string, Block block, FabricItemSettings fabricItemSettings) {
        Identifier identifier = new Identifier(LootCrates.MOD_ID, string);

        Registry.register(Registry.BLOCK, identifier, block);
        Registry.register(Registry.ITEM, identifier, new LootCrateItem(block, fabricItemSettings));
    }

    private static void registerShulkerCrateBlock(String string, Block block, FabricItemSettings fabricItemSettings) {
        Identifier identifier = new Identifier(LootCrates.MOD_ID, string);

        Registry.register(Registry.BLOCK, identifier, block);
        Registry.register(Registry.ITEM, identifier, new LootCrateItem(block, fabricItemSettings));
    }

    public static void register() {
        FabricItemSettings itemSettingsCommon = new FabricItemSettings().group(LootCrates.ITEM_GROUP).rarity(Rarity.COMMON);
        FabricItemSettings itemSettingsUncommon = new FabricItemSettings().group(LootCrates.ITEM_GROUP).rarity(Rarity.UNCOMMON);
        FabricItemSettings itemSettingsRare = new FabricItemSettings().group(LootCrates.ITEM_GROUP).rarity(Rarity.RARE);
        FabricItemSettings itemSettingsEpic = new FabricItemSettings().group(LootCrates.ITEM_GROUP).rarity(Rarity.EPIC);

        registerLootCrateBlock("common_chest_loot_crate", COMMON_CHEST_LOOT_CRATE, itemSettingsCommon);
        registerLootCrateBlock("uncommon_chest_loot_crate", UNCOMMON_CHEST_LOOT_CRATE, itemSettingsUncommon);
        registerLootCrateBlock("rare_chest_loot_crate", RARE_CHEST_LOOT_CRATE, itemSettingsRare);
        registerLootCrateBlock("epic_chest_loot_crate", EPIC_CHEST_LOOT_CRATE, itemSettingsEpic);

        registerShulkerCrateBlock("common_shulker_loot_crate", COMMON_SHULKER_LOOT_CRATE, itemSettingsCommon);
        registerShulkerCrateBlock("uncommon_shulker_loot_crate", UNCOMMON_SHULKER_LOOT_CRATE, itemSettingsUncommon);
        registerShulkerCrateBlock("rare_shulker_loot_crate", RARE_SHULKER_LOOT_CRATE, itemSettingsRare);
        registerShulkerCrateBlock("epic_shulker_loot_crate", EPIC_SHULKER_LOOT_CRATE, itemSettingsEpic);
   }
   
}
