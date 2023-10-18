package de.dafuqs.lootcrates.config;

import com.terraformersmc.modmenu.api.*;
import me.shedaniel.autoconfig.*;
import net.fabricmc.api.*;

@Environment(EnvType.CLIENT)
public class ModMenuConfig implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(LootCratesConfig.class, parent).get();
    }

}