package de.dafuqs.lootcrates;

import de.dafuqs.lootcrates.blocks.LootCrateBlockEntity;
import de.dafuqs.lootcrates.blocks.LootCratesBlockEntityType;
import de.dafuqs.lootcrates.blocks.chest.ChestLootCrateBlock;
import de.dafuqs.lootcrates.config.LootCratesConfig;
import de.dafuqs.lootcrates.enums.LootCrateRarity;
import de.dafuqs.lootcrates.enums.ScheduledTickEvent;
import de.dafuqs.lootcrates.items.LootBagItem;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class LootCrates implements ModInitializer {

    public static final String MOD_ID = "lootcrates";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static LootCratesConfig CONFIG;

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "loot_crates"),
            () -> new ItemStack(LootCrateAtlas.getLootCrate(LootCrateRarity.COMMON)));

    public static final ItemGroup PREDEFINED_CRATES_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "predefined_loot_crates"),
            () -> new ItemStack(Items.AIR)); // Is set in the tab directly

    public static final ItemGroup PREDEFINED_BAGS_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "predefined_loot_bags"),
            () -> new ItemStack(Items.AIR)); // Is set in the tab directly

    public static final Identifier CHEST_UNLOCKS_SOUND_ID = new Identifier(MOD_ID, "chest_unlocks");
    public static SoundEvent CHEST_UNLOCKS_SOUND_EVENT = new SoundEvent(CHEST_UNLOCKS_SOUND_ID);

    public static DispenserBehavior LOOT_BAG_DISPENSER_BEHAVIOR = (pointer, stack) -> {
        if(stack.getItem() instanceof LootBagItem lootBagItem) {
            Identifier lootTableId = lootBagItem.getLootTableIdentifier(stack);
            if (lootTableId != null) {
                long lootTableSeed = lootBagItem.getLootTableSeed(stack);

                LootTable lootTable = pointer.getWorld().getServer().getLootManager().getTable(lootTableId);
                Vec3d destinationPos = Vec3d.ofCenter(pointer.getPos());
                LootContext.Builder builder = (new LootContext.Builder(pointer.getWorld())).parameter(LootContextParameters.ORIGIN, destinationPos).random(lootTableSeed);

                List<ItemStack> lootStacks = lootTable.generateLoot(builder.build(LootContextTypes.CHEST));
                Position position = DispenserBlock.getOutputLocation(pointer);
                Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);

                for (ItemStack lootStack : lootStacks) {
                    ItemEntity itemEntity = new ItemEntity(pointer.getWorld(), position.getX(), position.getY(), position.getZ(), lootStack);
                    itemEntity.setVelocity(direction.getOffsetX() * 0.2, direction.getOffsetY() * 0.2, direction.getOffsetZ() * 0.2);
                    pointer.getWorld().spawnEntity(itemEntity);
                }
            }
            stack.decrement(1);
            return stack;
        }
        return stack;
    };


    public static class LootCrateReplacement {

        public RegistryKey<World> worldKey;
        public BlockPos blockPos;
        public Identifier lootTable;
        public long lootTableSeed;

        public LootCrateReplacement(RegistryKey<World> worldKey, BlockPos blockPos, Identifier lootTable, long lootTableSeed) {
            this.worldKey = worldKey;
            this.blockPos = blockPos;
            this.lootTable = lootTable;
            this.lootTableSeed = lootTableSeed;
        }
    }

    public static List<LootCrateReplacement> replacements = new ArrayList<>();

    @Override
    public void onInitialize() {

        // Config
        LOGGER.info("[LootCrates] Loading config...");
        AutoConfig.register(LootCratesConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(LootCratesConfig.class).getConfig();

        // All the different types of crates
        LOGGER.info("[LootCrates] Loading crate definitions...");
        LootCrateDefinition commonLootCrate = new LootCrateDefinition(LootCrateRarity.COMMON, Rarity.COMMON, MapColor.DIRT_BROWN, 0, false, false);
        LootCrateDefinition uncommonLootCrate = new LootCrateDefinition(LootCrateRarity.UNCOMMON, Rarity.UNCOMMON, MapColor.YELLOW, 0, false, false);
        LootCrateDefinition rareLootCrate = new LootCrateDefinition(LootCrateRarity.RARE, Rarity.RARE, MapColor.BLUE, 0, false, false);
        LootCrateDefinition epicLootCrate = new LootCrateDefinition(LootCrateRarity.EPIC, Rarity.EPIC, MapColor.PURPLE, 6,false, false);
        LootCrateDefinition ghostLootCrate = new LootCrateDefinition(LootCrateRarity.GHOST, Rarity.EPIC, MapColor.GREEN, 0, true, false);
        LootCrateDefinition blazeLootCrate = new LootCrateDefinition(LootCrateRarity.BLAZE, Rarity.EPIC, MapColor.ORANGE, 15, false, true);

        epicLootCrate.setCustomSounds(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundEvents.ENTITY_ENDER_DRAGON_FLAP);
        ghostLootCrate.setCustomSounds(SoundEvents.ENTITY_GHAST_AMBIENT, SoundEvents.ENTITY_GHAST_AMBIENT);
        blazeLootCrate.setCustomSounds(SoundEvents.ENTITY_BLAZE_AMBIENT, SoundEvents.ENTITY_BLAZE_SHOOT);
        blazeLootCrate.setScheduledTickEvent(ScheduledTickEvent.FIRE);

        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.COMMON, commonLootCrate);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.UNCOMMON, uncommonLootCrate);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.RARE, rareLootCrate);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.EPIC, epicLootCrate);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.GHOST, ghostLootCrate);
        LootCrateAtlas.registerLootCrateDefinition(LootCrateRarity.BLAZE, blazeLootCrate);

        // The block entity type
        LootCratesBlockEntityType.register();

        LOGGER.info("[LootCrates] Registering sounds...");
        Registry.register(Registry.SOUND_EVENT, CHEST_UNLOCKS_SOUND_ID, CHEST_UNLOCKS_SOUND_EVENT);

        if(CONFIG.ReplaceVanillaWorldgenChests) {
            ServerTickEvents.END_SERVER_TICK.register(server -> {
                if (!replacements.isEmpty()) {

                    // Some protection against concurrent modifications
                    List<LootCrateReplacement> list = new ArrayList<>(replacements);
                    replacements.clear();

                    for (LootCrateReplacement replacement : list) {
                        try {
                            ServerWorld serverWorld = server.getWorld(replacement.worldKey);
                            if (serverWorld != null) {
                                serverWorld.removeBlockEntity(replacement.blockPos);
                                BlockState chestBlockState = serverWorld.getBlockState(replacement.blockPos);

                                if (chestBlockState.getBlock() instanceof ChestBlock) {
                                    serverWorld.setBlockState(replacement.blockPos, LootCrateAtlas.getLootCrate(LootCrateRarity.COMMON).getDefaultState().with(ChestLootCrateBlock.FACING, chestBlockState.get(ChestBlock.FACING)), 3);
                                } else {
                                    serverWorld.setBlockState(replacement.blockPos, LootCrateAtlas.getLootCrate(LootCrateRarity.COMMON).getDefaultState().with(ChestLootCrateBlock.FACING, Direction.NORTH), 3);
                                }

                                BlockEntity blockEntity = serverWorld.getBlockEntity(replacement.blockPos);
                                if (blockEntity instanceof LootCrateBlockEntity lootCrateBlockEntity) {
                                    lootCrateBlockEntity.setLootTable(replacement.lootTable, replacement.lootTableSeed);
                                    if(CONFIG.ReplacedWorldgenChestsAreOncePerPlayer) {
                                        lootCrateBlockEntity.setOncePerPlayer(true);
                                    }
                                    if(CONFIG.ReplacedWorldgenChestsRestockEveryXTicks > 0) {
                                        lootCrateBlockEntity.setReplenishTimeTicks(CONFIG.ReplacedWorldgenChestsRestockEveryXTicks);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.ERROR, "[LootCrates] Error while replacing a chest in the world (" + replacement.worldKey + " at " + replacement.blockPos + " with loot table " + replacement.lootTable + ")");
                        }
                    }
                }
            });
        }

        LOGGER.info("[LootCrates] Finished!");
    }
}
