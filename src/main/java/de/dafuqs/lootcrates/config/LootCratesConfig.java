package de.dafuqs.lootcrates.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "LootCrates")
public class LootCratesConfig implements ConfigData {

    public boolean lootCrateChestsAreIndestructible = true;

}
