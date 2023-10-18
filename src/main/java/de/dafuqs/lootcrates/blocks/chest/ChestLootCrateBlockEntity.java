//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package de.dafuqs.lootcrates.blocks.chest;

import de.dafuqs.lootcrates.blocks.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.screen.*;
import net.minecraft.util.collection.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public class ChestLootCrateBlockEntity extends LootCrateBlockEntity implements LidOpenable {

    private final ViewerCountManager stateManager;
    private final ChestLidAnimator lidAnimator;

    public ChestLootCrateBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);

        this.inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
        this.stateManager = new ViewerCountManager() {
            protected void onContainerOpen(World world, BlockPos pos, BlockState state) {
                playOpenSoundEffect();
            }

            protected void onContainerClose(World world, BlockPos pos, BlockState state) {
                playCloseSoundEffect();
            }

            protected void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
                onInvOpenOrClose(world, pos, state, oldViewerCount, newViewerCount);
            }

            protected boolean isPlayerViewing(PlayerEntity player) {
                if (!(player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
                    return false;
                } else {
                    Inventory inventory = ((GenericContainerScreenHandler)player.currentScreenHandler).getInventory();
                    return inventory == ChestLootCrateBlockEntity.this;
                }
            }
        };
        this.lidAnimator = new ChestLidAnimator();
    }

    public ChestLootCrateBlockEntity(BlockPos pos, BlockState state) {
        this(LootCratesBlockEntityType.CHEST_LOOT_CRATE_BLOCK_ENTITY, pos, state);
    }

    protected void onInvOpenOrClose(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
        super.onInvOpenOrClose(world, pos, state, oldViewerCount, newViewerCount);
        Block block = state.getBlock();
        world.addSyncedBlockEvent(pos, block, 1, newViewerCount);
    }

    @Override
    public int getCurrentLookingPlayers(BlockView world, BlockPos pos) {
        return this.stateManager.getViewerCount();
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, ChestLootCrateBlockEntity blockEntity) {
        blockEntity.lidAnimator.step();
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, this);
    }

    @Override
    public void onOpen(PlayerEntity player) {
        if (!this.removed && !player.isSpectator()) {
            this.stateManager.openContainer(player, this.getWorld(), this.getPos(), this.getCachedState());
        }
    }

    @Override
    public void onClose(PlayerEntity player) {
        if (!this.removed && !player.isSpectator()) {
            this.stateManager.closeContainer(player, this.getWorld(), this.getPos(), this.getCachedState());
        }
    }

    public boolean onSyncedBlockEvent(int type, int data) {
        if (type == 1) {
            this.lidAnimator.setOpen(data > 0);
            return true;
        } else {
            return super.onSyncedBlockEvent(type, data);
        }
    }

    @Override
    public float getAnimationProgress(float tickDelta) {
        return this.lidAnimator.getProgress(tickDelta);
    }
}
