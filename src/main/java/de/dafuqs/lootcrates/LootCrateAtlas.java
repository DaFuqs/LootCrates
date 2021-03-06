package de.dafuqs.lootcrates;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import de.dafuqs.lootcrates.blocks.LootCrateBlock;
import de.dafuqs.lootcrates.blocks.LootCrateBlockEntity;
import de.dafuqs.lootcrates.blocks.chest.ChestLootCrateBlock;
import de.dafuqs.lootcrates.blocks.shulker.ShulkerLootCrateBlock;
import de.dafuqs.lootcrates.blocks.shulker.ShulkerLootCrateBlockEntity;
import de.dafuqs.lootcrates.enums.LootCrateRarity;
import de.dafuqs.lootcrates.enums.LootCrateTagNames;
import de.dafuqs.lootcrates.enums.ScheduledTickEvent;
import de.dafuqs.lootcrates.items.LootCrateItem;
import de.dafuqs.lootcrates.items.LootKeyItem;
import de.dafuqs.lootcrates.items.TickingLootCrateItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.client.render.TexturedRenderLayers.CHEST_ATLAS_TEXTURE;
import static net.minecraft.client.render.TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE;

public class LootCrateAtlas {

    private static final HashMap<LootCrateRarity, LootCrateDefinition> lootCrateDefinitions = new HashMap<>();
    private static final BiMap<LootCrateRarity, Item> lootCrateKeys = EnumHashBiMap.create(LootCrateRarity.class);
    private static final BiMap<LootCrateRarity, BlockItem> chestCrateItems = EnumHashBiMap.create(LootCrateRarity.class);
    private static final BiMap<LootCrateRarity, BlockItem> shulkerCrateItems = EnumHashBiMap.create(LootCrateRarity.class);
    private static final BiMap<LootCrateRarity, Block> lootCrateBlocks = EnumHashBiMap.create(LootCrateRarity.class);
    private static final BiMap<LootCrateRarity, Block> shulkerCrateBlocks = EnumHashBiMap.create(LootCrateRarity.class);

    private static ShulkerLootCrateBlock createShulkerLootCrateBlock(AbstractBlock.Settings settings) {
        AbstractBlock.ContextPredicate contextPredicate = (blockState, blockView, blockPos) -> {
            BlockEntity blockEntity = blockView.getBlockEntity(blockPos);
            if (!(blockEntity instanceof ShulkerLootCrateBlockEntity)) {
                return true;
            } else {
                ShulkerLootCrateBlockEntity shulkerBoxBlockEntity = (ShulkerLootCrateBlockEntity)blockEntity;
                return shulkerBoxBlockEntity.suffocates();
            }
        };
        return new ShulkerLootCrateBlock(settings.dynamicBounds().nonOpaque().suffocates(contextPredicate).blockVision(contextPredicate));
    }

    public static void registerLootCrateDefinition(LootCrateRarity lootCrateRarity, LootCrateDefinition lootCrateDefinition) {
        lootCrateDefinitions.put(lootCrateRarity, lootCrateDefinition);

        // create & register key item
        Identifier key_item_identifier = new Identifier(LootCrates.MOD_ID, lootCrateDefinition.identifier + "_crate_key");

        Item key_item = new LootKeyItem(lootCrateDefinition.getKeyItemSettings());
        Registry.register(Registry.ITEM, key_item_identifier, key_item);

        lootCrateKeys.put(lootCrateRarity, key_item);

        // create & register blocks
        Identifier loot_crate_identifier = new Identifier(LootCrates.MOD_ID, lootCrateDefinition.identifier + "_chest_loot_crate");
        Identifier shulker_crate_identifier = new Identifier(LootCrates.MOD_ID, lootCrateDefinition.identifier + "_shulker_loot_crate");

        Block loot_crate_block = new ChestLootCrateBlock(lootCrateDefinition.getChestBlockSettings());
        Block shulker_crate_block = createShulkerLootCrateBlock(lootCrateDefinition.getShulkerBlockSettings());
        Registry.register(Registry.BLOCK, loot_crate_identifier, loot_crate_block);
        Registry.register(Registry.BLOCK, shulker_crate_identifier, shulker_crate_block);

        lootCrateBlocks.put(lootCrateRarity, loot_crate_block);
        shulkerCrateBlocks.put(lootCrateRarity, shulker_crate_block);

        // create & register block items
        FabricItemSettings blockItemSettings = lootCrateDefinition.getBlockItemSettings();

        BlockItem loot_crate_block_item;
        BlockItem shulker_crate_block_item;
        if(lootCrateDefinition.scheduledTickEvent == ScheduledTickEvent.NONE) {
            loot_crate_block_item = new LootCrateItem(loot_crate_block, blockItemSettings);
            shulker_crate_block_item = new LootCrateItem(shulker_crate_block, blockItemSettings);
        } else {
            loot_crate_block_item = new TickingLootCrateItem(loot_crate_block, blockItemSettings);
            shulker_crate_block_item = new TickingLootCrateItem(shulker_crate_block, blockItemSettings);
        }
        Registry.register(Registry.ITEM, loot_crate_identifier, loot_crate_block_item);
        Registry.register(Registry.ITEM, shulker_crate_identifier, shulker_crate_block_item);

        chestCrateItems.put(lootCrateRarity, loot_crate_block_item);
        shulkerCrateItems.put(lootCrateRarity, shulker_crate_block_item);
    }

    public static SpriteIdentifier getChestTexture(LootCrateBlockEntity lootCrateBlockEntity) {
        if(lootCrateBlockEntity.hasWorld()) {
            Block block = lootCrateBlockEntity.getWorld().getBlockState(lootCrateBlockEntity.getPos()).getBlock();
            LootCrateDefinition lootCrateDefinition = lootCrateDefinitions.get(getCrateRarity(block));
            if(lootCrateDefinition != null) {
                return lootCrateDefinition.chestTexture;
            }
        }
        return new SpriteIdentifier(CHEST_ATLAS_TEXTURE, new Identifier(LootCrates.MOD_ID, "entity/chest/common_crate"));
    }

    public static SpriteIdentifier getShulkerTexture(LootCrateBlockEntity lootCrateBlockEntity) {
        if(lootCrateBlockEntity.hasWorld()) {
            Block block = lootCrateBlockEntity.getWorld().getBlockState(lootCrateBlockEntity.getPos()).getBlock();
            LootCrateDefinition lootCrateDefinition = lootCrateDefinitions.get(getShulkerRarity(block));
            if(lootCrateDefinition != null) {
                return lootCrateDefinition.shulkerTexture;
            }
        }
        return new SpriteIdentifier(SHULKER_BOXES_ATLAS_TEXTURE, new Identifier(LootCrates.MOD_ID, "entity/chest/common_crate"));
    }

    public static Block getLootCrate(LootCrateRarity lootCrateRarity) {
        return lootCrateBlocks.get(lootCrateRarity);
    }

    public static Block getShulkerCrate(LootCrateRarity lootCrateRarity) {
        return shulkerCrateBlocks.get(lootCrateRarity);
    }

    public static boolean hasTransparency(LootCrateBlockEntity lootCrateBlockEntity) {
        if(lootCrateBlockEntity == null) {
            return false;
        }
        
        BlockState blockState = lootCrateBlockEntity.getWorld().getBlockState(lootCrateBlockEntity.getPos());
        LootCrateRarity lootCrateRarity = getCrateRarity(blockState.getBlock());

        LootCrateDefinition lootCrateDefinition = lootCrateDefinitions.get(lootCrateRarity);
        if(lootCrateDefinition == null) {
            return false;
        } else {
            return lootCrateDefinition.hasTransparency;
        }
    }

    public static Text getItemLockedTooltip(ItemStack itemStack, NbtCompound compound) {
        LootCrateRarity itemRarity = getCrateItemRarity((LootCrateItem) itemStack.getItem());

        if (compound.contains(LootCrateTagNames.DoNotConsumeKeyOnUnlock.toString()) && compound.getBoolean(LootCrateTagNames.DoNotConsumeKeyOnUnlock.toString())) {
            return lootCrateDefinitions.get(itemRarity).lockedTooltip;
        } else {
            return lootCrateDefinitions.get(itemRarity).lockedConsumeTooltip;
        }
    }

    public static Block[] getChestCrates() {
        return lootCrateBlocks.values().toArray(new Block[0]);
    }

    public static Block[] getShulkerCrates() {
        return shulkerCrateBlocks.values().toArray(new Block[0]);
    }

    public static void registerTransparentBlocks() {
        for(Map.Entry<LootCrateRarity, LootCrateDefinition> entry : lootCrateDefinitions.entrySet()) {
            if(entry.getValue().hasTransparency) {
                LootCrateRarity lootCrateRarity = entry.getKey();
                BlockRenderLayerMap.INSTANCE.putBlock(lootCrateBlocks.get(lootCrateRarity), RenderLayer.getTranslucent());
                BlockRenderLayerMap.INSTANCE.putBlock(shulkerCrateBlocks.get(lootCrateRarity), RenderLayer.getTranslucent());
            }
        }
    }

    public static List<Item> getAllCrateItems() {
        ArrayList<Item> arrayList = new ArrayList<>(chestCrateItems.values());
        arrayList.addAll(shulkerCrateItems.values());
        return arrayList;
    }

    public static void registerTextureAtlas() {
        //Register textures in chest atlas
        ClientSpriteRegistryCallback.event(TexturedRenderLayers.CHEST_ATLAS_TEXTURE).register((texture, registry) -> {
            for(LootCrateDefinition lootCrateDefinition : lootCrateDefinitions.values()) {
                registry.register(lootCrateDefinition.chestTextureIdentifier);
            }
        });

        ClientSpriteRegistryCallback.event(TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE).register((texture, registry) -> {
            for(LootCrateDefinition lootCrateDefinition : lootCrateDefinitions.values()) {
                registry.register(lootCrateDefinition.shulkerTextureIdentifier);
            }
        });
    }

    public static TranslatableText getKeyNeededTooltip(LootCrateRarity rarity) {
        return lootCrateDefinitions.get(rarity).keyNeededTooltip;
    }

    public static LootCrateRarity getCrateRarity(Block block) {
        if(lootCrateBlocks.containsValue(block)) {
            return lootCrateBlocks.inverse().get(block);
        } else {
            return shulkerCrateBlocks.inverse().get(block);
        }
    }

    private static LootCrateRarity getShulkerRarity(Block block) {
        return shulkerCrateBlocks.inverse().get(block);
    }

    public static LootCrateRarity getKeyRarity(LootKeyItem item) {
        return lootCrateKeys.inverse().get(item);
    }

    protected static LootCrateRarity getCrateItemRarity(LootCrateItem item) {
        if(chestCrateItems.containsValue(item)) {
            return chestCrateItems.inverse().get(item);
        } else {
            return shulkerCrateItems.inverse().get(item);
        }
    }

    public static TranslatableText getLootKeyItemToolTip(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return lootCrateDefinitions.get(lootCrateKeys.inverse().get(item)).lootKeyTooltip;
    }

    public static ScheduledTickEvent getRandomTickEvent(LootCrateBlock lootCrateBlock) {
        LootCrateRarity lootCrateRarity = getCrateRarity(lootCrateBlock);
        return lootCrateDefinitions.get(lootCrateRarity).scheduledTickEvent;
    }

    public static SoundEvent getCustomOpenSoundEvent(LootCrateBlock lootCrateBlock) {
        LootCrateRarity lootCrateRarity;
        if(lootCrateBlock instanceof ChestLootCrateBlock) {
            lootCrateRarity = getCrateRarity(lootCrateBlock);
        } else {
            lootCrateRarity = getShulkerRarity(lootCrateBlock);
        }
        return lootCrateDefinitions.get(lootCrateRarity).customOpenSoundEvent;
    }

    public static SoundEvent getCustomCloseSoundEvent(LootCrateBlock lootCrateBlock) {
        LootCrateRarity lootCrateRarity;
        if(lootCrateBlock instanceof ChestLootCrateBlock) {
            lootCrateRarity = getCrateRarity(lootCrateBlock);
        } else {
            lootCrateRarity = getShulkerRarity(lootCrateBlock);
        }
        return lootCrateDefinitions.get(lootCrateRarity).customCloseSoundEvent;
    }

    @Environment(EnvType.CLIENT)
    public static void setupTextures() {
        for(LootCrateDefinition lootCrateDefinition : lootCrateDefinitions.values()) {
            lootCrateDefinition.setupTextures();
        }
    }

}
