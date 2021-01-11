package de.dafuqs.lootcrates.items;

import de.dafuqs.lootcrates.LootCrates;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class LootCratesItems {

    public static final FabricItemSettings crateKeyItemSettings = new FabricItemSettings()
            .group(LootCrates.ITEM_GROUP)
            .maxCount(16);

    // an instance of our new item
    public static final Item COMMON_CRATE_KEY = new Item(crateKeyItemSettings.rarity(Rarity.COMMON));
    public static final Item UNCOMMON_CRATE_KEY = new Item(crateKeyItemSettings.rarity(Rarity.UNCOMMON));
    public static final Item RARE_CRATE_KEY = new Item(crateKeyItemSettings.rarity(Rarity.RARE));
    public static final Item EPIC_CRATE_KEY = new Item(crateKeyItemSettings.rarity(Rarity.EPIC));

    private static void register(String string, Item item) {
        Registry.register(Registry.ITEM, new Identifier(LootCrates.MOD_ID, string), item);
    }

    public static void initialize() {
        register("common_crate_key", COMMON_CRATE_KEY);
        register("uncommon_crate_key", UNCOMMON_CRATE_KEY);
        register("rare_crate_key", RARE_CRATE_KEY);
        register("epic_crate_key", EPIC_CRATE_KEY);
    }
}
