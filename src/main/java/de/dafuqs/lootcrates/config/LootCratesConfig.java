package de.dafuqs.lootcrates.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "LootCrates")
public class LootCratesConfig implements ConfigData {

    public boolean ChestCratesAreIndestructible = true;
    public boolean ShulkerCratesAreIndestructible = false;

    public boolean ChestCratesKeepTheirInventory = false;
    public boolean ShulkerCratesKeepTheirInventory = true;

}