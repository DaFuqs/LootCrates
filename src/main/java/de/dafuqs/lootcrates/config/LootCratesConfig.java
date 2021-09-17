package de.dafuqs.lootcrates.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.Arrays;
import java.util.List;

@Config(name = "LootCrates")
public class LootCratesConfig implements ConfigData {

    @ConfigEntry.Category("general")
    @Comment(value = "Whether chest and shulker loot crates are indestructible")
    public boolean ChestCratesAreIndestructible = true;
    @ConfigEntry.Category("GENERAL")
    public boolean ShulkerCratesAreIndestructible = false;

    @Comment(value = """
            Whether  chest and shulker loot crates should heep their inventory"
            when broken. Otherwise they will drop their contents just like broken chests""")
    public boolean ChestCratesKeepTheirInventory = false;
    public boolean ShulkerCratesKeepTheirInventory = true;

    @ConfigEntry.Category("worldgen")
    @Comment(value = """
            Whether all chests that generate during worldgen should be replaced by loot crates.
            This includes vanilla and modded structures
            This is especially useful if you want new players to find treasure in structures that were
            raided by players before, or if players should have an incentive to visit those structures again.""")
    public boolean ReplaceVanillaWorldgenChests = true;
    @ConfigEntry.Category("VANILLA TREASURE CHEST REPLACEMENT")
    public boolean ReplacedWorldgenChestsAreOncePerPlayer = true;
    @ConfigEntry.Category("VANILLA TREASURE CHEST REPLACEMENT")
    public int ReplacedWorldgenChestsRestockEveryXTicks = 1; // <= 0: disabled
    public List<String> ReplaceVanillaWorldgenChestsDimensionsBlacklist = List.of("spectrum:deeper_down");

}
