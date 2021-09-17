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
    @ConfigEntry.Category("general")
    public boolean LootBarrelsAreIndestructible = true;
    @ConfigEntry.Category("general")
    public boolean ShulkerCratesAreIndestructible = false;

    @Comment(value = """
            Whether  chest and shulker loot crates should heep their inventory"
            when broken. Otherwise they will drop their contents just like broken chests""")
    @ConfigEntry.Category("general")
    public boolean ChestCratesKeepTheirInventory = false;
    @ConfigEntry.Category("general")
    public boolean LootBarrelsKeepTheirInventory = false;
    @ConfigEntry.Category("general")
    public boolean ShulkerCratesKeepTheirInventory = true;

    @ConfigEntry.Category("worldgen")
    @Comment(value = """
            Whether all chests that generate during worldgen should be replaced by loot crates.
            This includes vanilla and modded structures
            This is especially useful if you want new players to find treasure in structures that were
            raided by players before, or if players should have an incentive to visit those structures again.""")
    public boolean ReplaceVanillaWorldgenChests = true;
    @ConfigEntry.Category("worldgen")
    public boolean ReplacedWorldgenChestsAreOncePerPlayer = true;
    @ConfigEntry.Category("worldgen")
    public int ReplacedWorldgenChestsRestockEveryXTicks = 1; // <= 0: disabled
    @ConfigEntry.Category("worldgen")
    public List<String> ReplaceVanillaWorldgenChestsDimensionsBlacklist = List.of("spectrum:deeper_down");

}
