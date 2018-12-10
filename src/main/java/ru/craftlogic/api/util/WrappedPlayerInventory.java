package ru.craftlogic.api.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import ru.craftlogic.api.inventory.InventoryFieldHolder;
import ru.craftlogic.api.inventory.InventoryHolder;
import ru.craftlogic.api.inventory.WrappedInventoryFieldHolder;
import ru.craftlogic.api.inventory.manager.InventoryManager;
import ru.craftlogic.api.inventory.manager.SizeCheckedInventoryManager;
import ru.craftlogic.api.world.PhantomPlayer;
import ru.craftlogic.api.world.Player;

import javax.annotation.Nullable;

public class WrappedPlayerInventory implements InventoryHolder {
    private final Player viewer;
    private final PhantomPlayer target;

    public WrappedPlayerInventory(Player viewer, PhantomPlayer target) {
        this.viewer = viewer;
        this.target = target;
    }

    @Override
    public String getName() {
        return this.target.getProfile().getName();
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.target.getDisplayName();
    }

    @Override
    public InventoryManager getInventoryManager() {
        return new SizeCheckedInventoryManager(this.getInventory(), slot -> this.markDirty());
    }

    private InventoryPlayer getInventory() {
        return this.target.getInventory();
    }

    @Override
    public int getSizeInventory() {
        return 45;
    }

    @Override
    public void markDirty() {
        this.getInventory().markDirty();
        if (this.target.isOnline()) {
            this.target.getOpenContainer().detectAndSendChanges();
        }
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return !this.target.isOnline() || !this.target.isDead();
    }

    @Override
    public void openInventory(EntityPlayer player) {
        this.viewer.playSound(SoundEvents.BLOCK_CHEST_OPEN, 1F, 1F);
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (!this.target.isOnline()) {
            this.target.saveData();
        }
        this.viewer.playSound(SoundEvents.BLOCK_CHEST_CLOSE, 1F, 1F);
    }

    @Nullable
    @Override
    public InventoryFieldHolder getFieldHolder() {
        return new WrappedInventoryFieldHolder(this.getInventory());
    }
}
