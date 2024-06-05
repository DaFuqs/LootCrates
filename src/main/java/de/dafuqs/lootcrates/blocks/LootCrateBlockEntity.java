package de.dafuqs.lootcrates.blocks;

import de.dafuqs.lootcrates.*;
import de.dafuqs.lootcrates.blocks.chest.*;
import de.dafuqs.lootcrates.components.*;
import de.dafuqs.lootcrates.enums.*;
import de.dafuqs.lootcrates.items.*;
import net.minecraft.advancement.criterion.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.component.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.loot.*;
import net.minecraft.loot.context.*;
import net.minecraft.nbt.*;
import net.minecraft.registry.*;
import net.minecraft.screen.*;
import net.minecraft.server.network.*;
import net.minecraft.server.world.*;
import net.minecraft.sound.*;
import net.minecraft.text.*;
import net.minecraft.util.collection.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

import java.util.*;

public abstract class LootCrateBlockEntity extends LockableContainerBlockEntity implements LootableInventory {
	
	protected DefaultedList<ItemStack> inventory;
	private ScheduledTickEvent scheduledTickEvent;
	
	@Nullable
	private PlayerCrateData defaultCrateData = null;
	private HashMap<UUID, PlayerCrateData> playerCrateData = new HashMap<>();
	
	
	protected LootCrateBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		super(blockEntityType, blockPos, blockState);
		this.inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);
		
		Inventories.writeNbt(nbt, this.inventory, registryLookup);
	}
	
	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		
		this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		if (nbt.contains("Items", 9)) {
			Inventories.readNbt(nbt, this.inventory, registryLookup);
		}
	}
	
	@Override
	public int size() {
		return this.inventory.size();
	}
	
	@Override
	protected DefaultedList<ItemStack> getHeldStacks() {
		return this.inventory;
	}
	
	@Override
	protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
		this.inventory = inventory;
	}
	
	protected void playSound(SoundEvent soundEvent) {
		playSound(soundEvent, 1.0F);
	}
	
	protected void playSound(SoundEvent soundEvent, float volume) {
		if (hasWorld()) {
			double d = (double) this.pos.getX() + 0.5D;
			double e = (double) this.pos.getY() + 0.5D;
			double f = (double) this.pos.getZ() + 0.5D;
			this.world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5F * volume, this.world.random.nextFloat() * 0.1F + 0.9F);
		}
	}
	
	
	
	public Optional<PlayerCrateData> getPlayerCrateData(PlayerEntity playerEntity) {
		@Nullable LootCrateLootComponent lootComponent = getComponents().get(LootCrateDataComponentTypes.LOOT_CRATE_LOOT);
		if(lootComponent == null) {
			return Optional.empty();
		}
		
		if (lootComponent.trackedPerPlayer()) {
			if (playerCrateData.containsKey(playerEntity.getUuid())) {
				return Optional.of(playerCrateData.get(playerEntity.getUuid()));
			}
			return Optional.empty();
		} else {
			if (defaultCrateData == null) {
				return Optional.empty();
			}
			return Optional.of(defaultCrateData);
		}
	}
	
	private void setReplenishTimeToNow(PlayerEntity player) {
		ComponentMap components = getComponents();
		@Nullable LootCratePlayerDataComponent playerDataComponent = components.get(LootCrateDataComponentTypes.LOOT_CRATE_PLAYER_DATA);
		LootCrateLootComponent lootComponent = components.get(LootCrateDataComponentTypes.LOOT_CRATE_LOOT);
		
		LootCratePlayerDataComponent newComponent = LootCratePlayerDataComponent.setReplenishedForPlayer(playerDataComponent, player, lootComponent.trackedPerPlayer(), lootComponent.replenishTimeScale());
		
		ComponentMap.Builder builder = ComponentMap.builder().add(LootCrateDataComponentTypes.LOOT_CRATE_PLAYER_DATA, newComponent);
		addComponents(builder);
		this.markDirty();
	}
	
	public boolean canReplenish(LootCrateLootComponent lootComponent, PlayerEntity player) {
		if (hasWorld()) {
			Optional<PlayerCrateData> playerCrateDataOptional = getPlayerCrateData(player);
			if (playerCrateDataOptional.isEmpty()) {
				return true;
			} else {
				return canReplenish(world, playerCrateDataOptional, lootComponent.replenishTimeScale(), lootComponent.replenishTime());
			}
		}
		return false;
	}
	
	public static boolean canReplenish(World world, Optional<PlayerCrateData> playerCrateData, ReplenishTimeScale replenishTimeScale, long replenishTimeTicks) {
		if (playerCrateData.isEmpty()) {
			return true;
		} else {
			return replenishTimeScale.canReplenish(world, playerCrateData, replenishTimeTicks);
		}
	}
	
	@Override
	public boolean isEmpty() {
		return this.getHeldStacks().stream().allMatch(ItemStack::isEmpty);
	}
	
	@Nullable
	public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
		if (this.checkUnlocked(playerEntity)) {
			this.generateLoot(playerInventory.player);
			return this.createScreenHandler(i, playerInventory);
		} else {
			return null;
		}
	}
	
	@Override
	public void generateLoot(@Nullable PlayerEntity player) {
		@Nullable LootCrateLootComponent lootComponent = getComponents().get(LootCrateDataComponentTypes.LOOT_CRATE_LOOT);
		if(lootComponent == null) {
			return;
		}
		
		boolean inventoryCleared = false;
		if (lootComponent.inventoryDeletionMode() == InventoryDeletionMode.ON_OPEN) {
			this.clear();
			inventoryCleared = true;
		}
		
		boolean canGenerateNewLoot = canReplenish(lootComponent, player);
		if (canGenerateNewLoot) {
			if (!inventoryCleared && lootComponent.inventoryDeletionMode() == InventoryDeletionMode.WHEN_REPLENISHED) {
				this.clear();
			}
			
			setReplenishTimeToNow(player);
		}
		
		World world = this.getWorld();
		BlockPos blockPos = this.getPos();
		RegistryKey<LootTable> registryKey = lootComponent.lootTable();
		if (registryKey != null && world != null && world.getServer() != null) {
			LootTable lootTable = world.getServer().getReloadableRegistries().getLootTable(registryKey);
			if (player instanceof ServerPlayerEntity) {
				Criteria.PLAYER_GENERATES_CONTAINER_LOOT.trigger((ServerPlayerEntity)player, registryKey);
			}
			
			LootContextParameterSet.Builder builder = (new LootContextParameterSet.Builder((ServerWorld)world)).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(blockPos));
			if (player != null) {
				builder.luck(player.getLuck()).add(LootContextParameters.THIS_ENTITY, player);
			}
			
			lootTable.supplyInventory(this, builder.build(LootContextTypes.CHEST), lootComponent.seed());
		}
	}
	
	@Nullable
	@Override
	public RegistryKey<LootTable> getLootTable() {
		@Nullable LootCrateLootComponent lootComponent = getComponents().get(LootCrateDataComponentTypes.LOOT_CRATE_LOOT);
		if(lootComponent == null) {
			return null;
		}
		return lootComponent.lootTable();
	}
	
	@Override
	public long getLootTableSeed() {
		@Nullable LootCrateLootComponent lootComponent = getComponents().get(LootCrateDataComponentTypes.LOOT_CRATE_LOOT);
		if(lootComponent == null) {
			return 0;
		}
		return lootComponent.seed();
	}
	
	@Override
	public void setLootTable(@Nullable RegistryKey<LootTable> lootTable) {
		// no-op
	}
	
	@Override
	public void setLootTableSeed(long lootTableSeed) {
		// no-op
	}
	
	protected void onInvOpenOrClose(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
		if (isTrapped() && oldViewerCount != newViewerCount) {
			Block block = state.getBlock();
			world.updateNeighborsAlways(pos, block);
			world.updateNeighborsAlways(pos.down(), block);
		}
	}
	
	@Override
	protected Text getContainerName() {
		BlockState state = getCachedState();
		Text name = state.getBlock().getName();
		if (name.getString().startsWith("§")) { // THE FUCK
			return Text.literal(name.getString().substring(2));
		} else {
			return name;
		}
	}
	
	public void relockIfNecessary(PlayerEntity player) {
		@Nullable LootCrateLootComponent lootComponent = getComponents().get(LootCrateDataComponentTypes.LOOT_CRATE_LOOT);
		if(lootComponent == null) {
			return;
		}
		
		Optional<PlayerCrateData> playerCrateDataOptional = getPlayerCrateData(player);
		if (shouldRelock(world, lootComponent.replenishTimeScale(), lootComponent.replenishTime(), lootComponent.lockMode(), playerCrateDataOptional)) {
			ComponentMap components = getComponents();
			LootCratePlayerDataComponent playerDataComponent = components.copy(LootCrateDataComponentTypes.LOOT_CRATE_PLAYER_DATA).value();
			LootCratePlayerDataComponent newComponent = LootCratePlayerDataComponent.lockForPlayer(playerDataComponent, player, lootComponent.trackedPerPlayer());
			ComponentMap.Builder builder = ComponentMap.builder().add(LootCrateDataComponentTypes.LOOT_CRATE_PLAYER_DATA, newComponent);
			addComponents(builder);
			
			this.markDirty();
		}
	}
	
	private static boolean shouldRelock(World world, ReplenishTimeScale replenishTimeScale, long replenishTimeTicks, LockMode lockMode, Optional<PlayerCrateData> playerCrateData) {
		if (lockMode.relocks()) {
			if (playerCrateData.isPresent()) {
				return playerCrateData.get().unlockTime() < playerCrateData.get().replenishTime() && LootCrateBlockEntity.canReplenish(world, playerCrateData, replenishTimeScale, replenishTimeTicks);
			}
		}
		return false;
	}
	
	public boolean isUnlocked(PlayerEntity player) {
		@Nullable LootCrateLootComponent lootComponent = getComponents().get(LootCrateDataComponentTypes.LOOT_CRATE_LOOT);
		if(lootComponent == null) {
			return false;
		}
		Optional<PlayerCrateData> optionalPlayerCrateData = getPlayerCrateData(player);
		return lootComponent.isUnlocked(optionalPlayerCrateData);
	}
	
	public boolean doesItemUnlock(Item item) {
		if (world != null && item instanceof LootKeyItem lootKeyItem) {
			Optional<LootCrateBlock> block = getBlock();
			if (block.isPresent()) {
				LootCrateVariant itemVariant = lootKeyItem.getVariant();
				LootCrateVariant blockVariant = block.get().getVariant();
				return itemVariant.equals(blockVariant);
			}
		}
		
		return false;
	}
	
	public void unlockFor(PlayerEntity player) {
		ComponentMap components = getComponents();
		@Nullable LootCrateLootComponent lootComponent = components.get(LootCrateDataComponentTypes.LOOT_CRATE_LOOT);
		if(lootComponent == null) {
			return;
		}
		
		@Nullable LootCratePlayerDataComponent playerDataComponent = components.get(LootCrateDataComponentTypes.LOOT_CRATE_PLAYER_DATA);
		LootCratePlayerDataComponent newComponent = LootCratePlayerDataComponent.unlockForPlayer(playerDataComponent, player, lootComponent.trackedPerPlayer(), lootComponent.replenishTimeScale());
		
		ComponentMap.Builder builder = ComponentMap.builder().add(LootCrateDataComponentTypes.LOOT_CRATE_PLAYER_DATA, newComponent);
		addComponents(builder);
		
		this.markDirty();
		this.playSound(LootCrates.CHEST_UNLOCKS_SOUND_EVENT);
	}
	
	public Optional<LootCrateBlock> getBlock() {
		if (this.world == null) {
			return Optional.empty();
		} else {
			Block block = this.world.getBlockState(this.pos).getBlock();
			if (block instanceof LootCrateBlock lootCrateBlock) {
				return Optional.of(lootCrateBlock);
			} else {
				return Optional.empty();
			}
		}
	}
	
	public ScheduledTickEvent getRandomTickEvent() {
		if (this.scheduledTickEvent == null) {
			Optional<LootCrateBlock> block = getBlock();
			block.ifPresent(lootCrateBlock -> scheduledTickEvent = LootCrateAtlas.getRandomTickEvent(lootCrateBlock));
		}
		return this.scheduledTickEvent;
	}
	
	public void playOpenSoundEffect() {
		// play default sound
		if (this instanceof ChestLootCrateBlockEntity) {
			playSound(SoundEvents.BLOCK_CHEST_OPEN);
		} else {
			playSound(SoundEvents.BLOCK_SHULKER_BOX_OPEN);
		}
		
		// also play custom sound, if set
		Optional<LootCrateBlock> block = getBlock();
		if (block.isPresent()) {
			SoundEvent customSoundEvent = LootCrateAtlas.getCustomOpenSoundEvent(block.get());
			if (customSoundEvent != null) {
				playSound(customSoundEvent, 0.4F);
			}
		}
	}
	
	public void playCloseSoundEffect() {
		// play default sound
		if (this instanceof ChestLootCrateBlockEntity) {
			playSound(SoundEvents.BLOCK_CHEST_CLOSE);
		} else {
			playSound(SoundEvents.BLOCK_SHULKER_BOX_CLOSE);
		}
		
		// also play custom sound, if set
		Optional<LootCrateBlock> block = getBlock();
		if (block.isPresent()) {
			SoundEvent customSoundEvent = LootCrateAtlas.getCustomCloseSoundEvent(block.get());
			if (customSoundEvent != null) {
				playSound(customSoundEvent, 0.4F);
			}
		}
	}
	
	protected static void playSound(@NotNull World world, @NotNull BlockPos pos, BlockState state, SoundEvent soundEvent) {
		double d = (double) pos.getX() + 0.5D;
		double e = (double) pos.getY() + 0.5D;
		double f = (double) pos.getZ() + 0.5D;
		world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
	}
	
	public abstract int getCurrentLookingPlayers(BlockView world, BlockPos pos);
	
	public boolean isTrapped() {
		@Nullable LootCrateLootComponent lootComponent = getComponents().get(LootCrateDataComponentTypes.LOOT_CRATE_LOOT);
		if(lootComponent == null) {
			return false;
		}
		return lootComponent.trapped();
	}
	
	public LockMode getLockType() {
		@Nullable LootCrateLootComponent lootComponent = getComponents().get(LootCrateDataComponentTypes.LOOT_CRATE_LOOT);
		if(lootComponent == null) {
			return LockMode.NOT_LOCKED;
		}
		return lootComponent.lockMode();
	}
	
}
