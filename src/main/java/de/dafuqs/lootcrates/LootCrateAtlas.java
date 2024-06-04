package de.dafuqs.lootcrates;

import com.google.common.collect.*;
import de.dafuqs.lootcrates.blocks.*;
import de.dafuqs.lootcrates.blocks.barrel.*;
import de.dafuqs.lootcrates.blocks.chest.*;
import de.dafuqs.lootcrates.blocks.shulker.*;
import de.dafuqs.lootcrates.components.*;
import de.dafuqs.lootcrates.enums.*;
import de.dafuqs.lootcrates.items.*;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.blockrenderlayer.v1.*;
import net.minecraft.block.*;
import net.minecraft.block.dispenser.*;
import net.minecraft.block.entity.*;
import net.minecraft.client.render.*;
import net.minecraft.client.util.*;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.sound.*;
import net.minecraft.text.*;
import net.minecraft.util.*;

import java.util.*;

import static net.minecraft.client.render.TexturedRenderLayers.*;

public class LootCrateAtlas {
	
	private static final HashMap<LootCrateVariant, LootCrateDefinition> lootCrateDefinitions = new HashMap<>();
	private static final BiMap<LootCrateVariant, LootKeyItem> lootCrateKeys = EnumHashBiMap.create(LootCrateVariant.class);
	private static final BiMap<LootCrateVariant, BlockItem> chestCrateItems = EnumHashBiMap.create(LootCrateVariant.class);
	private static final BiMap<LootCrateVariant, BlockItem> shulkerCrateItems = EnumHashBiMap.create(LootCrateVariant.class);
	private static final BiMap<LootCrateVariant, BlockItem> lootBarrelItems = EnumHashBiMap.create(LootCrateVariant.class);
	private static final BiMap<LootCrateVariant, LootBagItem> lootBagItems = EnumHashBiMap.create(LootCrateVariant.class);
	private static final BiMap<LootCrateVariant, ChestLootCrateBlock> lootCrateBlocks = EnumHashBiMap.create(LootCrateVariant.class);
	private static final BiMap<LootCrateVariant, ShulkerLootCrateBlock> shulkerCrateBlocks = EnumHashBiMap.create(LootCrateVariant.class);
	private static final BiMap<LootCrateVariant, LootBarrelBlock> lootBarrelBlocks = EnumHashBiMap.create(LootCrateVariant.class);
	
	private static ShulkerLootCrateBlock createShulkerLootCrateBlock(LootCrateVariant variant, AbstractBlock.Settings settings) {
		AbstractBlock.ContextPredicate contextPredicate = (blockState, blockView, blockPos) -> {
			BlockEntity blockEntity = blockView.getBlockEntity(blockPos);
			if (!(blockEntity instanceof ShulkerLootCrateBlockEntity shulkerBoxBlockEntity)) {
				return true;
			} else {
				return shulkerBoxBlockEntity.suffocates();
			}
		};
		return new ShulkerLootCrateBlock(variant, settings.dynamicBounds().nonOpaque().suffocates(contextPredicate).blockVision(contextPredicate));
	}
	
	public static void registerLootCrateDefinition(LootCrateVariant variant, LootCrateDefinition lootCrateDefinition) {
		lootCrateDefinitions.put(variant, lootCrateDefinition);
		
		// create & register key item
		Identifier keyItemIdentifier = new Identifier(LootCrates.MOD_ID, lootCrateDefinition.identifier + "_crate_key");
		
		LootKeyItem keyItem = new LootKeyItem(variant, lootCrateDefinition.getKeyItemSettings());
		Registry.register(Registries.ITEM, keyItemIdentifier, keyItem);
		
		lootCrateKeys.put(variant, keyItem);
		
		// create & register blocks
		Identifier lootCrateIdentifier = new Identifier(LootCrates.MOD_ID, lootCrateDefinition.identifier + "_chest_loot_crate");
		Identifier shulkerCrateIdentifier = new Identifier(LootCrates.MOD_ID, lootCrateDefinition.identifier + "_shulker_loot_crate");
		Identifier lootBarrelIdentifier = new Identifier(LootCrates.MOD_ID, lootCrateDefinition.identifier + "_loot_barrel");
		
		ChestLootCrateBlock lootCrateBlock = new ChestLootCrateBlock(variant, lootCrateDefinition.getChestBlockSettings());
		ShulkerLootCrateBlock shulkerLootCrateBlock = createShulkerLootCrateBlock(variant, lootCrateDefinition.getShulkerBlockSettings());
		LootBarrelBlock lootBarrelBlock = new LootBarrelBlock(variant, lootCrateDefinition.getLootBarrelBlockSettings());
		Registry.register(Registries.BLOCK, lootCrateIdentifier, lootCrateBlock);
		Registry.register(Registries.BLOCK, shulkerCrateIdentifier, shulkerLootCrateBlock);
		Registry.register(Registries.BLOCK, lootBarrelIdentifier, lootBarrelBlock);
		
		lootCrateBlocks.put(variant, lootCrateBlock);
		shulkerCrateBlocks.put(variant, shulkerLootCrateBlock);
		lootBarrelBlocks.put(variant, lootBarrelBlock);
		
		// create & register block items
		Item.Settings blockItemSettings = lootCrateDefinition.getBlockItemSettings();
		
		BlockItem lootCrateBlockItem;
		BlockItem shulkerCrateBlockItem;
		BlockItem lootBarrelBlockItem;
		if (lootCrateDefinition.scheduledTickEvent == ScheduledTickEvent.NONE) {
			lootCrateBlockItem = new LootCrateItem(variant, lootCrateBlock, blockItemSettings);
			shulkerCrateBlockItem = new LootCrateItem(variant, shulkerLootCrateBlock, blockItemSettings);
			lootBarrelBlockItem = new LootCrateItem(variant, lootBarrelBlock, blockItemSettings);
		} else {
			lootCrateBlockItem = new TickingLootCrateItem(variant, lootCrateBlock, blockItemSettings, lootCrateDefinition.scheduledTickEvent);
			shulkerCrateBlockItem = new TickingLootCrateItem(variant, shulkerLootCrateBlock, blockItemSettings, lootCrateDefinition.scheduledTickEvent);
			lootBarrelBlockItem = new TickingLootCrateItem(variant, lootBarrelBlock, blockItemSettings, lootCrateDefinition.scheduledTickEvent);
		}
		Registry.register(Registries.ITEM, lootCrateIdentifier, lootCrateBlockItem);
		Registry.register(Registries.ITEM, shulkerCrateIdentifier, shulkerCrateBlockItem);
		Registry.register(Registries.ITEM, lootBarrelIdentifier, lootBarrelBlockItem);
		
		DispenserBlock.registerBehavior(lootCrateBlockItem, new BlockPlacementDispenserBehavior());
		DispenserBlock.registerBehavior(shulkerCrateBlockItem, new BlockPlacementDispenserBehavior());
		DispenserBlock.registerBehavior(lootBarrelBlockItem, new BlockPlacementDispenserBehavior());
		
		chestCrateItems.put(variant, lootCrateBlockItem);
		shulkerCrateItems.put(variant, shulkerCrateBlockItem);
		lootBarrelItems.put(variant, lootBarrelBlockItem);
		
		// create & register loot bag item
		Identifier lootBagIdentifier = new Identifier(LootCrates.MOD_ID, lootCrateDefinition.identifier + "_loot_bag");
		
		LootBagItem lootBagItem;
		if (lootCrateDefinition.scheduledTickEvent == ScheduledTickEvent.NONE) {
			lootBagItem = new LootBagItem(lootCrateDefinition.getLootBagItemSettings());
		} else {
			lootBagItem = new TickingLootBagItem(lootCrateDefinition.getLootBagItemSettings(), lootCrateDefinition.scheduledTickEvent);
		}
		
		Registry.register(Registries.ITEM, lootBagIdentifier, lootBagItem);
		
		lootBagItems.put(variant, lootBagItem);
		DispenserBlock.registerBehavior(lootBagItem, LootCrates.LOOT_BAG_DISPENSER_BEHAVIOR);
	}
	
	public static SpriteIdentifier getChestTexture(LootCrateBlockEntity lootCrateBlockEntity) {
		if (lootCrateBlockEntity.hasWorld()) {
			Block block = lootCrateBlockEntity.getWorld().getBlockState(lootCrateBlockEntity.getPos()).getBlock();
			LootCrateDefinition lootCrateDefinition = lootCrateDefinitions.get(getCrateVariant(block));
			if (lootCrateDefinition != null) {
				return lootCrateDefinition.chestTexture;
			}
		}
		return new SpriteIdentifier(CHEST_ATLAS_TEXTURE, new Identifier(LootCrates.MOD_ID, "block/common_crate"));
	}
	
	public static SpriteIdentifier getShulkerTexture(LootCrateBlockEntity lootCrateBlockEntity) {
		if (lootCrateBlockEntity.hasWorld()) {
			Block block = lootCrateBlockEntity.getWorld().getBlockState(lootCrateBlockEntity.getPos()).getBlock();
			LootCrateDefinition lootCrateDefinition = lootCrateDefinitions.get(getShulkerRarity(block));
			if (lootCrateDefinition != null) {
				return lootCrateDefinition.shulkerTexture;
			}
		}
		return new SpriteIdentifier(SHULKER_BOXES_ATLAS_TEXTURE, new Identifier(LootCrates.MOD_ID, "block/common_crate"));
	}
	
	public static Block getLootCrate(LootCrateVariant lootCrateVariant) {
		return lootCrateBlocks.get(lootCrateVariant);
	}
	
	public static Block getShulkerCrate(LootCrateVariant lootCrateVariant) {
		return shulkerCrateBlocks.get(lootCrateVariant);
	}
	
	public static Block getLootBarrel(LootCrateVariant lootCrateVariant) {
		return lootBarrelBlocks.get(lootCrateVariant);
	}
	
	public static boolean hasTransparency(LootCrateBlockEntity lootCrateBlockEntity) {
		if (lootCrateBlockEntity == null) {
			return false;
		}
		
		BlockState blockState = lootCrateBlockEntity.getWorld().getBlockState(lootCrateBlockEntity.getPos());
		LootCrateVariant lootCrateVariant = getCrateVariant(blockState.getBlock());
		
		LootCrateDefinition lootCrateDefinition = lootCrateDefinitions.get(lootCrateVariant);
		if (lootCrateDefinition == null) {
			return false;
		} else {
			return lootCrateDefinition.hasTransparency;
		}
	}
	
	public static Text getItemLockedTooltip(LootCrateVariant variant, LockMode lockMode) {
		if (lockMode.consumesKey()) {
			return lootCrateDefinitions.get(variant).lockedConsumeTooltip;
		} else {
			return lootCrateDefinitions.get(variant).lockedTooltip;
		}
	}
	
	public static Block[] getChestCrates() {
		return lootCrateBlocks.values().toArray(new Block[0]);
	}
	
	public static Block[] getShulkerCrates() {
		return shulkerCrateBlocks.values().toArray(new Block[0]);
	}
	
	public static Block[] getLootBarrels() {
		return lootBarrelBlocks.values().toArray(new Block[0]);
	}
	
	public static void registerTransparentBlocks() {
		for (Map.Entry<LootCrateVariant, LootCrateDefinition> entry : lootCrateDefinitions.entrySet()) {
			if (entry.getValue().hasTransparency) {
				LootCrateVariant lootCrateVariant = entry.getKey();
				BlockRenderLayerMap.INSTANCE.putBlock(lootCrateBlocks.get(lootCrateVariant), RenderLayer.getTranslucent());
				BlockRenderLayerMap.INSTANCE.putBlock(shulkerCrateBlocks.get(lootCrateVariant), RenderLayer.getTranslucent());
				BlockRenderLayerMap.INSTANCE.putBlock(lootBarrelBlocks.get(lootCrateVariant), RenderLayer.getTranslucent());
			}
		}
	}
	
	public static List<Item> getAllCrateItems() {
		ArrayList<Item> arrayList = new ArrayList<>(chestCrateItems.values());
		arrayList.addAll(shulkerCrateItems.values());
		arrayList.addAll(lootBarrelItems.values());
		return arrayList;
	}
	
	public static List<Item> getAllLootBagItems() {
		return new ArrayList<>(lootBagItems.values());
	}
	
	public static Text getKeyNeededTooltip(LootCrateVariant rarity) {
		return lootCrateDefinitions.get(rarity).keyNeededTooltip;
	}
	
	public static LootCrateVariant getCrateVariant(Block block) {
		if (block instanceof ChestLootCrateBlock) {
			return lootCrateBlocks.inverse().get(block);
		} else if (block instanceof ShulkerLootCrateBlock) {
			return shulkerCrateBlocks.inverse().get(block);
		} else {
			return lootBarrelBlocks.inverse().get(block);
		}
	}
	
	public static LootCrateVariant getCrateItemVariant(Item item) {
		BlockItem blockItem = (BlockItem) item;
		
		if (blockItem.getBlock() instanceof LootCrateBlock lootCrateBlock) {
			return getCrateVariant(lootCrateBlock);
		}
		return LootCrateVariant.COMMON;
	}
	
	private static LootCrateVariant getShulkerRarity(Block block) {
		return shulkerCrateBlocks.inverse().get(block);
	}
	
	private static LootCrateVariant getBarrelRarity(Block block) {
		return lootBarrelBlocks.inverse().get(block);
	}
	
	public static LootCrateVariant getKeyRarity(LootKeyItem item) {
		return lootCrateKeys.inverse().get(item);
	}
	
	public static LootKeyItem getLootKeyItem(LootCrateVariant lootCrateVariant) {
		return lootCrateKeys.get(lootCrateVariant);
	}
	
	protected static LootCrateVariant getCrateItemVariant(LootCrateItem item) {
		if (item.getBlock() instanceof ChestLootCrateBlock) {
			return chestCrateItems.inverse().get(item);
		} else if (item.getBlock() instanceof ShulkerLootCrateBlock) {
			return shulkerCrateItems.inverse().get(item);
		} else {
			return lootBarrelItems.inverse().get(item);
		}
	}
	
	public static Text getLootKeyItemToolTip(ItemStack itemStack) {
		Item item = itemStack.getItem();
		return lootCrateDefinitions.get(lootCrateKeys.inverse().get(item)).lootKeyTooltip;
	}
	
	public static Text getLootBagItemToolTip(ItemStack itemStack) {
		Item item = itemStack.getItem();
		return lootCrateDefinitions.get(lootBagItems.inverse().get(item)).lootBagTooltip;
	}
	
	public static ScheduledTickEvent getRandomTickEvent(LootCrateBlock lootCrateBlock) {
		LootCrateVariant lootCrateVariant = getCrateVariant(lootCrateBlock);
		return lootCrateDefinitions.get(lootCrateVariant).scheduledTickEvent;
	}
	
	public static SoundEvent getCustomOpenSoundEvent(LootCrateBlock lootCrateBlock) {
		LootCrateVariant lootCrateVariant;
		if (lootCrateBlock instanceof ChestLootCrateBlock) {
			lootCrateVariant = getCrateVariant(lootCrateBlock);
		} else if (lootCrateBlock instanceof ShulkerLootCrateBlock) {
			lootCrateVariant = getShulkerRarity(lootCrateBlock);
		} else {
			lootCrateVariant = getBarrelRarity(lootCrateBlock);
		}
		return lootCrateDefinitions.get(lootCrateVariant).customOpenSoundEvent;
	}
	
	public static SoundEvent getCustomCloseSoundEvent(LootCrateBlock lootCrateBlock) {
		LootCrateVariant lootCrateVariant;
		if (lootCrateBlock instanceof ChestLootCrateBlock) {
			lootCrateVariant = getCrateVariant(lootCrateBlock);
		} else {
			lootCrateVariant = getShulkerRarity(lootCrateBlock);
		}
		return lootCrateDefinitions.get(lootCrateVariant).customCloseSoundEvent;
	}
	
	@Environment(EnvType.CLIENT)
	public static void setupTextures() {
		for (LootCrateDefinition lootCrateDefinition : lootCrateDefinitions.values()) {
			lootCrateDefinition.setupTextures();
		}
	}
	
	public static LootBagItem getLootBagItem(LootCrateVariant rarity) {
		return lootBagItems.get(rarity);
	}
	
}
