package de.dafuqs.lootcrates.config;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import net.minecraft.util.math.random.Random;

public class WeightedLootCrateEntryList {

    int totalWeight;
    List<LootCrateReplacementEntry> weightedEntries;
    public WeightedLootCrateEntryList(int totalWeight, List<LootCrateReplacementEntry> weightedEntries) {
        this.totalWeight = totalWeight;
        this.weightedEntries = weightedEntries;
    }

    public LootCrateReplacementEntry getWeightedRandom(@NotNull Random random) {
        int idx = 0;
        for (double r = random.nextDouble() * totalWeight; idx < this.weightedEntries.size() - 1; ++idx) {
            r -= this.weightedEntries.get(idx).weight;
            if (r <= 0.0) break;
        }
        return this.weightedEntries.get(idx);
    }

}