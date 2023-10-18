//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package de.dafuqs.lootcrates.blocks.barrel;

import de.dafuqs.lootcrates.blocks.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.screen.*;
import net.minecraft.sound.*;
import net.minecraft.util.collection.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public class LootBarrelBlockEntity extends LootCrateBlockEntity {

    private final ViewerCountManager stateManager;

    public LootBarrelBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);

        this.inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
        this.stateManager = new ViewerCountManager() {
            protected void onContainerOpen(World world, BlockPos pos, BlockState state) {
                playSound(world, pos, state, SoundEvents.BLOCK_BARREL_OPEN);
                setOpen(state, true);
            }

            protected void onContainerClose(World world, BlockPos pos, BlockState state) {
                playSound(world, pos, state, SoundEvents.BLOCK_BARREL_CLOSE);
                setOpen(state, false);
            }

            protected void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
                onInvOpenOrClose(world, pos, state, oldViewerCount, newViewerCount);
            }

            protected boolean isPlayerViewing(PlayerEntity player) {
                if (!(player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
                    return false;
                } else {
                    Inventory inventory = ((GenericContainerScreenHandler)player.currentScreenHandler).getInventory();
                    return inventory == LootBarrelBlockEntity.this;
                }
            }
        };
    }

    protected void onInvOpenOrClose(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
        super.onInvOpenOrClose(world, pos, state, oldViewerCount, newViewerCount);
    }

    @Override
    public int getCurrentLookingPlayers(BlockView world, BlockPos pos) {
        return this.stateManager.getViewerCount();
    }

    public LootBarrelBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(LootCratesBlockEntityType.LOOT_BARREL_BLOCK_ENTITY, blockPos, blockState);
    }

    private void setOpen(BlockState state, boolean open) {
        this.world.setBlockState(this.getPos(), state.with(BarrelBlock.OPEN, open), 3);
    }

    public void tick() {
        if (!this.removed) {
            this.stateManager.updateViewerCount(this.getWorld(), this.getPos(), this.getCachedState());
        }
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

}
