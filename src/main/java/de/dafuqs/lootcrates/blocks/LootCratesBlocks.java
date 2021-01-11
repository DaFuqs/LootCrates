package de.dafuqs.lootcrates.blocks;

import de.dafuqs.lootcrates.LootCrates;
import de.dafuqs.lootcrates.items.keys.LootCrateItem;
import de.dafuqs.lootcrates.items.keys.ShulkerCrateItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class LootCratesBlocks {

    private static final FabricBlockSettings blockSettingsLootCrate = FabricBlockSettings.of(Material.METAL).requiresTool().strength(-1.0F, 3600000.0F).dropsNothing();
    private static final FabricBlockSettings blockSettingsShulkerCrate = FabricBlockSettings.of(Material.METAL).requiresTool().strength(2.0F).nonOpaque();

    public static final Block COMMON_LOOT_CRATE = new Block(blockSettingsLootCrate);
    public static final Block UNCOMMON_LOOT_CRATE = new Block(blockSettingsLootCrate);
    public static final Block RARE_LOOT_CRATE = new Block(blockSettingsLootCrate);
    public static final Block EPIC_LOOT_CRATE = new Block(blockSettingsLootCrate);
    public static final Block COMMON_LOOT_SHULKER_CRATE = new Block(blockSettingsShulkerCrate);
    public static final Block UNCOMMON_LOOT_SHULKER_CRATE = new Block(blockSettingsShulkerCrate);
    public static final Block RARE_LOOT_SHULKER_CRATE = new Block(blockSettingsShulkerCrate);
    public static final Block EPIC_LOOT_SHULKER_CRATE = new Block(blockSettingsShulkerCrate);

    private static void registerLootCrateBlock(String identifier, Block block, FabricItemSettings fabricItemSettings) {
        Identifier ident = new Identifier(LootCrates.MOD_ID, identifier);

        Registry.register(Registry.BLOCK, ident, block);
        Registry.register(Registry.ITEM, ident, new LootCrateItem(block, fabricItemSettings));
    }

    private static void registerShulkerCrateBlock(String identifier, Block block, FabricItemSettings fabricItemSettings) {
        Identifier ident = new Identifier(LootCrates.MOD_ID, identifier);

        Registry.register(Registry.BLOCK, ident, block);
        Registry.register(Registry.ITEM, ident, new ShulkerCrateItem(block, fabricItemSettings));
    }

    public static void initialize() {
        FabricItemSettings itemSettings = new FabricItemSettings().group(LootCrates.ITEM_GROUP);
        FabricItemSettings itemSettingsCommon = itemSettings.rarity(Rarity.COMMON);
        FabricItemSettings itemSettingsUncommon = itemSettings.rarity(Rarity.UNCOMMON);
        FabricItemSettings itemSettingsRare = itemSettings.rarity(Rarity.RARE);
        FabricItemSettings itemSettingsEpic = itemSettings.rarity(Rarity.EPIC);

        registerLootCrateBlock("common_loot_crate", COMMON_LOOT_CRATE, itemSettingsCommon);
        registerLootCrateBlock("uncommon_loot_crate", UNCOMMON_LOOT_CRATE, itemSettingsUncommon);
        registerLootCrateBlock("rare_loot_crate", RARE_LOOT_CRATE, itemSettingsRare);
        registerLootCrateBlock("epic_loot_crate", EPIC_LOOT_CRATE, itemSettingsEpic);

        registerShulkerCrateBlock("common_loot_shulker_crate", COMMON_LOOT_SHULKER_CRATE, itemSettingsCommon);
        registerShulkerCrateBlock("uncommon_loot_shulker_crate", UNCOMMON_LOOT_SHULKER_CRATE, itemSettingsUncommon);
        registerShulkerCrateBlock("rare_loot_shulker_crate", RARE_LOOT_SHULKER_CRATE, itemSettingsRare);
        registerShulkerCrateBlock("epic_loot_shulker_crate", EPIC_LOOT_SHULKER_CRATE, itemSettingsEpic);
   }
   
}
