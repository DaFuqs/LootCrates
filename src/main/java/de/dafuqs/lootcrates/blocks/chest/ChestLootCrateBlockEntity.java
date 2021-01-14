//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package de.dafuqs.lootcrates.blocks.chest;

import de.dafuqs.lootcrates.LootCrates;
import de.dafuqs.lootcrates.LootCratesBlocks;
import de.dafuqs.lootcrates.blocks.LootCrateBlockEntity;
import de.dafuqs.lootcrates.blocks.LootCratesBlockEntityType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;

import static net.minecraft.client.render.TexturedRenderLayers.CHEST_ATLAS_TEXTURE;

public class ChestLootCrateBlockEntity extends LootCrateBlockEntity implements Tickable {

    private int viewerCount;
    protected float animationAngle;
    protected float lastAnimationAngle;

    public ChestLootCrateBlockEntity() {
        super(LootCratesBlockEntityType.CHEST_LOOT_CRATE_BLOCK_ENTITY, DefaultedList.ofSize(27, ItemStack.EMPTY));
    }

    @Override
    protected Text getContainerName() {
        return new TranslatableText("container.lootcrates.loot_crate");
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, this);
    }

    @Override
    public void tick() {
        this.lastAnimationAngle = this.animationAngle;
        if (this.viewerCount > 0 && this.animationAngle == 0.0F) {
            playSound(SoundEvents.BLOCK_CHEST_OPEN);
        }

        if (this.viewerCount == 0 && this.animationAngle > 0.0F || this.viewerCount > 0 && this.animationAngle < 1.0F) {
            float g = this.animationAngle;
            if (this.viewerCount > 0) {
                this.animationAngle += 0.1F;
            } else {
                this.animationAngle -= 0.1F;
            }

            if (this.animationAngle > 1.0F) {
                this.animationAngle = 1.0F;
            }

            if (this.animationAngle < 0.5F && g >= 0.5F) {
                playSound(SoundEvents.BLOCK_CHEST_CLOSE);
            }

            if (this.animationAngle < 0.0F) {
                this.animationAngle = 0.0F;
            }
        }
    }

    @Override
    public void onOpen(PlayerEntity player) {
        if (!player.isSpectator()) {
            if (this.viewerCount < 0) {
                this.viewerCount = 0;
            }

            ++this.viewerCount;
            this.onInvOpenOrClose();
        }
    }

    protected void onInvOpenOrClose() {
        Block block = this.getCachedState().getBlock();
        if (block instanceof ChestLootCrateBlock) {
            this.world.addSyncedBlockEvent(this.pos, block, 1, this.viewerCount);
        }
    }

    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        if (type == 1) {
            this.viewerCount = data;
            return true;
        } else {
            return super.onSyncedBlockEvent(type, data);
        }
    }

    @Override
    public void onClose(PlayerEntity player) {
        if (!player.isSpectator()) {
            --this.viewerCount;
        }
        this.onInvOpenOrClose();
    }

    @Environment(EnvType.CLIENT)
    public float getAnimationProgress(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastAnimationAngle, this.animationAngle);
    }

    public SpriteIdentifier getTexture() {
        Block block = world.getBlockState(pos).getBlock();
        if (LootCratesBlocks.COMMON_CHEST_LOOT_CRATE.equals(block)) {
            return new SpriteIdentifier(CHEST_ATLAS_TEXTURE, new Identifier(LootCrates.MOD_ID, "entity/chest/common_crate"));
        } else if (LootCratesBlocks.UNCOMMON_CHEST_LOOT_CRATE.equals(block)) {
            return new SpriteIdentifier(CHEST_ATLAS_TEXTURE, new Identifier(LootCrates.MOD_ID, "entity/chest/uncommon_crate"));
        } else if (LootCratesBlocks.RARE_CHEST_LOOT_CRATE.equals(block)) {
            return new SpriteIdentifier(CHEST_ATLAS_TEXTURE, new Identifier(LootCrates.MOD_ID, "entity/chest/rare_crate"));
        } else if (LootCratesBlocks.EPIC_CHEST_LOOT_CRATE.equals(block)) {
            return new SpriteIdentifier(CHEST_ATLAS_TEXTURE, new Identifier(LootCrates.MOD_ID, "entity/chest/epic_crate"));
        }
        return new SpriteIdentifier(CHEST_ATLAS_TEXTURE, new Identifier(LootCrates.MOD_ID, "entity/chest/common_crate"));
    }

}
