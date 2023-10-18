package de.dafuqs.lootcrates.config;

import de.dafuqs.lootcrates.*;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.annotation.*;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.*;
import net.minecraft.world.*;

import java.util.*;

@Config(name = "LootCrates")
public class LootCratesConfig implements ConfigData {

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.PrefixText
    @Comment(value = """
    How long Loot Crates take to break. <1 makes them indestructible.
    Obsidian has 50. 150 makes them very much breakable, but taking ages.
    Long enough to absolutely not breakable by accident, but still possible to
    remove them if they are in the way somehow.""")
    public float ChestCrateHardness = 150.0F;
    @ConfigEntry.Category("general")
    public float LootBarrelHardness = 150.0F;
    @ConfigEntry.Category("general")
    public float ShulkerCrateHardness = 3.0F;

    @Comment(value = """
    If crates that are mined by players should drop as items
    Otherwise they will be destroyed and do not drop.""")
    @ConfigEntry.Category("general")
    public boolean ChestCratesDropAsItems = false;
    @ConfigEntry.Category("general")
    public boolean LootBarrelsDropAsItems = false;
    @ConfigEntry.Category("general")
    public boolean ShulkerCratesDropAsItems = true;

    @Comment(value = """
            Whether chest and shulker loot crates should keep their inventory when broken.
            Otherwise they will drop their contents just like broken chests""")
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Category("general")
    public boolean ChestCratesKeepTheirInventory = false;
    @ConfigEntry.Category("general")
    public boolean LootBarrelsKeepTheirInventory = false;
    @ConfigEntry.Category("general")
    public boolean ShulkerCratesKeepTheirInventory = true;
    
    @ConfigEntry.Category("general")
    public boolean BlazingCratesCauseFire = true;

    @ConfigEntry.Category("worldgen")
    @Comment(value = """
            If all chests that generate during worldgen should be replaced by loot crates.
            This includes vanilla and modded structures. See the granular configuration in LootCratesWorldgenSettings.json5
            This is especially useful if you want new players to find treasure in structures that were
            raided by players before, or if players should have an incentive to visit those structures again.
            Setting restocking to <= 0 results them functioning like vanilla chests.
            Restocking is only evaluated when players actually open chests, no performance impact besides that.
            So feel free to leave it at 1 if you want loot to be available instantly for every unique player.""")
    public boolean ReplaceVanillaWorldgenChests = true;
    @ConfigEntry.Category("worldgen")
    @Comment(value = """
            Dimension identifiers where chests will not be replaced""")
    public final List<String> ReplaceVanillaWorldgenChestsDimensionsBlacklist = new ArrayList<>();
    
    public boolean isWorldBlacklisted(World world) {
        return LootCrates.CONFIG.ReplaceVanillaWorldgenChestsDimensionsBlacklist.contains(world.getRegistryKey().getValue().toString());
    }
    
}
