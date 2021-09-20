package de.dafuqs.lootcrates.worldgen;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class WeightedLootCrateEntryList {

    int totalWeight;
    List<LootCrateReplacementEntry> weightedEntries;
    public WeightedLootCrateEntryList(int totalWeight, List<LootCrateReplacementEntry> weightedEntries) {
        this.totalWeight = totalWeight;
        this.weightedEntries = weightedEntries;
    }

    public LootCrateReplacementEntry getWeightedRandom(@NotNull Random random) {
        int r = random.nextInt(totalWeight);
        int c = 0;
        for (LootCrateReplacementEntry weightedEntry : this.weightedEntries) {
            if (r < c) {
                return weightedEntry;
            }
            c += weightedEntry.weight;
        }
        return this.weightedEntries.get(this.weightedEntries.size()-1);
    }

}