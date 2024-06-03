package de.dafuqs.lootcrates.components;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import de.dafuqs.lootcrates.blocks.modes.*;
import net.minecraft.loot.*;
import net.minecraft.registry.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

public record LootCrateLootComponent(RegistryKey<LootTable> lootTable, long seed) {
    
    public static final Codec<LootCrateLootComponent> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(
                RegistryKey.createCodec(RegistryKeys.LOOT_TABLE).fieldOf("loot_table").forGetter(LootCrateLootComponent::lootTable),
                Codec.LONG.optionalFieldOf("seed", 0L).forGetter(LootCrateLootComponent::seed)
        ).apply(instance, LootCrateLootComponent::new);
    });

    public LootCrateLootComponent(@NotNull Identifier lootTable, LockMode lockMode, ReplenishMode replenishMode, InventoryDeletionMode inventoryDeletionMode, long lootGenerationTimeInTicks, boolean trackedPerPlayer, boolean trapped) {
        this.lootTable = lootTable;
        this.seed = seed;
    }
    
    /*
    public static @NotNull NbtCompound getLootCrateItemCompoundTag(@NotNull Identifier lootTable, LockMode lockMode, ReplenishMode replenishMode, InventoryDeletionMode inventoryDeletionMode, long lootGenerationTimeInTicks, boolean trackedPerPlayer, boolean trapped) {
        NbtCompound compoundTag = new NbtCompound();
        NbtCompound blockEntityTag = new NbtCompound();
        
        blockEntityTag.putString("LootTable", lootTable.toString());
        
        if(lootGenerationTimeInTicks > 0) {
            blockEntityTag.putLong(LootCrateTagNames.ReplenishTimeTicks.toString(), lootGenerationTimeInTicks);
        }
        blockEntityTag.putBoolean(LootCrateTagNames.TrackedPerPlayer.toString(), trackedPerPlayer);
        blockEntityTag.putString(LootCrateTagNames.LockMode.toString(), lockMode.toString());
        blockEntityTag.putString(LootCrateTagNames.ReplenishMode.toString(), replenishMode.toString());
        blockEntityTag.putString(LootCrateTagNames.InventoryDeletionMode.toString(), inventoryDeletionMode.toString());
        blockEntityTag.putBoolean(LootCrateTagNames.Trapped.toString(), trapped);
        
        compoundTag.put("BlockEntityTag", blockEntityTag);
        return compoundTag;
    }*/
    
}
