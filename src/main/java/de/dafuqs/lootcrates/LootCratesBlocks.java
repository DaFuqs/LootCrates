package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.blocks.chest.ChestLootCrateBlock;
import de.dafuqs.lootcrates.blocks.shulker.ShulkerLootCrateBlock;
import de.dafuqs.lootcrates.blocks.shulker.ShulkerLootCrateBlockEntity;
import de.dafuqs.lootcrates.items.LootCrateItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class LootCratesBlocks {

    private static ShulkerLootCrateBlock createShulkerLootCrateBlock(AbstractBlock.Settings settings) {
        AbstractBlock.ContextPredicate contextPredicate = (blockState, blockView, blockPos) -> {
            BlockEntity blockEntity = blockView.getBlockEntity(blockPos);
            if (!(blockEntity instanceof ShulkerLootCrateBlockEntity)) {
                return true;
            } else {
                ShulkerLootCrateBlockEntity shulkerBoxBlockEntity = (ShulkerLootCrateBlockEntity)blockEntity;
                return shulkerBoxBlockEntity.suffocates();
            }
        };
        return new ShulkerLootCrateBlock(settings.strength(2.0F).dynamicBounds().nonOpaque().suffocates(contextPredicate).blockVision(contextPredicate));
    }

    private static final FabricBlockSettings blockSettingsChestLootCrate = FabricBlockSettings.of(Material.METAL).requiresTool().strength(-1.0F, 3600000.0F).dropsNothing();
    private static final FabricBlockSettings blockSettingsGhostChestLootCrate = FabricBlockSettings.of(Material.METAL).requiresTool().strength(-1.0F, 3600000.0F).dropsNothing().nonOpaque();
    private static final FabricBlockSettings blockSettingsShulkerLootCrate = FabricBlockSettings.of(Material.SHULKER_BOX);
    private static final FabricBlockSettings blockSettingsGhostShulkerLootCrate = FabricBlockSettings.of(Material.SHULKER_BOX).nonOpaque();

    public static final Block COMMON_CHEST_LOOT_CRATE = new ChestLootCrateBlock(blockSettingsChestLootCrate);
    public static final Block UNCOMMON_CHEST_LOOT_CRATE = new ChestLootCrateBlock(blockSettingsChestLootCrate);
    public static final Block RARE_CHEST_LOOT_CRATE = new ChestLootCrateBlock(blockSettingsChestLootCrate);
    public static final Block EPIC_CHEST_LOOT_CRATE = new ChestLootCrateBlock(blockSettingsChestLootCrate);
    public static final Block COMMON_SHULKER_LOOT_CRATE = createShulkerLootCrateBlock(blockSettingsShulkerLootCrate);
    public static final Block UNCOMMON_SHULKER_LOOT_CRATE = createShulkerLootCrateBlock(blockSettingsShulkerLootCrate);
    public static final Block RARE_SHULKER_LOOT_CRATE = createShulkerLootCrateBlock(blockSettingsShulkerLootCrate);
    public static final Block EPIC_SHULKER_LOOT_CRATE = createShulkerLootCrateBlock(blockSettingsShulkerLootCrate);

    public static final Block GHOST_CHEST_LOOT_CRATE = new ChestLootCrateBlock(blockSettingsGhostChestLootCrate);
    public static final Block GHOST_SHULKER_LOOT_CRATE = createShulkerLootCrateBlock(blockSettingsGhostShulkerLootCrate);

    public static final FabricItemSettings itemSettingsCommon = new FabricItemSettings().group(LootCrates.ITEM_GROUP).rarity(Rarity.COMMON);
    public static final FabricItemSettings itemSettingsUncommon = new FabricItemSettings().group(LootCrates.ITEM_GROUP).rarity(Rarity.UNCOMMON);
    public static final FabricItemSettings itemSettingsRare = new FabricItemSettings().group(LootCrates.ITEM_GROUP).rarity(Rarity.RARE);
    public static final FabricItemSettings itemSettingsEpic = new FabricItemSettings().group(LootCrates.ITEM_GROUP).rarity(Rarity.EPIC);
    public static final FabricItemSettings itemSettingsGhost = new FabricItemSettings().group(LootCrates.ITEM_GROUP).rarity(Rarity.UNCOMMON);

    public static final BlockItem COMMON_CHEST_LOOT_CRATE_ITEM = new LootCrateItem(COMMON_CHEST_LOOT_CRATE, itemSettingsCommon);
    public static final BlockItem UNCOMMON_CHEST_LOOT_CRATE_ITEM = new LootCrateItem(UNCOMMON_CHEST_LOOT_CRATE, itemSettingsUncommon);
    public static final BlockItem RARE_CHEST_LOOT_CRATE_ITEM = new LootCrateItem(RARE_CHEST_LOOT_CRATE, itemSettingsRare);
    public static final BlockItem EPIC_CHEST_LOOT_CRATE_ITEM = new LootCrateItem(EPIC_CHEST_LOOT_CRATE, itemSettingsEpic);

    public static final BlockItem COMMON_SHULKER_LOOT_CRATE_ITEM = new LootCrateItem(COMMON_SHULKER_LOOT_CRATE, itemSettingsCommon);
    public static final BlockItem UNCOMMON_SHULKER_LOOT_CRATE_ITEM = new LootCrateItem(UNCOMMON_SHULKER_LOOT_CRATE, itemSettingsUncommon);
    public static final BlockItem RARE_SHULKER_LOOT_CRATE_ITEM = new LootCrateItem(RARE_SHULKER_LOOT_CRATE, itemSettingsRare);
    public static final BlockItem EPIC_SHULKER_LOOT_CRATE_ITEM = new LootCrateItem(EPIC_SHULKER_LOOT_CRATE, itemSettingsEpic);

    public static final BlockItem GHOST_CHEST_LOOT_CRATE_ITEM = new LootCrateItem(GHOST_CHEST_LOOT_CRATE, itemSettingsGhost);
    public static final BlockItem GHOST_SHULKER_LOOT_CRATE_ITEM = new LootCrateItem(GHOST_SHULKER_LOOT_CRATE, itemSettingsGhost);

    private static void registerLootCrateBlock(String string, Block block, BlockItem blockItem) {
        Identifier identifier = new Identifier(LootCrates.MOD_ID, string);

        Registry.register(Registry.BLOCK, identifier, block);
        Registry.register(Registry.ITEM, identifier, blockItem);
    }

    public static void register() {
        registerLootCrateBlock("common_chest_loot_crate", COMMON_CHEST_LOOT_CRATE, COMMON_CHEST_LOOT_CRATE_ITEM);
        registerLootCrateBlock("uncommon_chest_loot_crate", UNCOMMON_CHEST_LOOT_CRATE, UNCOMMON_CHEST_LOOT_CRATE_ITEM);
        registerLootCrateBlock("rare_chest_loot_crate", RARE_CHEST_LOOT_CRATE, RARE_CHEST_LOOT_CRATE_ITEM);
        registerLootCrateBlock("epic_chest_loot_crate", EPIC_CHEST_LOOT_CRATE, EPIC_CHEST_LOOT_CRATE_ITEM);

        registerLootCrateBlock("common_shulker_loot_crate", COMMON_SHULKER_LOOT_CRATE, COMMON_SHULKER_LOOT_CRATE_ITEM);
        registerLootCrateBlock("uncommon_shulker_loot_crate", UNCOMMON_SHULKER_LOOT_CRATE, UNCOMMON_SHULKER_LOOT_CRATE_ITEM);
        registerLootCrateBlock("rare_shulker_loot_crate", RARE_SHULKER_LOOT_CRATE, RARE_SHULKER_LOOT_CRATE_ITEM);
        registerLootCrateBlock("epic_shulker_loot_crate", EPIC_SHULKER_LOOT_CRATE, EPIC_SHULKER_LOOT_CRATE_ITEM);

        registerLootCrateBlock("ghost_chest_loot_crate", GHOST_CHEST_LOOT_CRATE, GHOST_CHEST_LOOT_CRATE_ITEM);
        registerLootCrateBlock("ghost_shulker_loot_crate", GHOST_SHULKER_LOOT_CRATE, GHOST_SHULKER_LOOT_CRATE_ITEM);
   }

   public static List<Item> getLootCrateItems() {
        ArrayList<Item> lootCrates = new ArrayList<>();
        lootCrates.add(COMMON_CHEST_LOOT_CRATE_ITEM);
        lootCrates.add(UNCOMMON_CHEST_LOOT_CRATE_ITEM);
        lootCrates.add(RARE_CHEST_LOOT_CRATE_ITEM);
        lootCrates.add(EPIC_CHEST_LOOT_CRATE_ITEM);
        lootCrates.add(COMMON_SHULKER_LOOT_CRATE_ITEM);
        lootCrates.add(UNCOMMON_SHULKER_LOOT_CRATE_ITEM);
        lootCrates.add(RARE_SHULKER_LOOT_CRATE_ITEM);
        lootCrates.add(EPIC_SHULKER_LOOT_CRATE_ITEM);

        lootCrates.add(GHOST_CHEST_LOOT_CRATE_ITEM);
        lootCrates.add(GHOST_SHULKER_LOOT_CRATE_ITEM);
        return lootCrates;
   }
   
}
