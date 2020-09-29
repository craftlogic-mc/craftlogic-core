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
        return target.getProfile().getName();
    }

    @Override
    public ITextComponent getDisplayName() {
        return target.getDisplayName();
    }

    @Override
    public InventoryManager getInventoryManager() {
        return new SizeCheckedInventoryManager(getInventory(), slot -> markDirty());
    }

    private InventoryPlayer getInventory() {
        return target.getInventory();
    }

    @Override
    public int getSizeInventory() {
        return 45;
    }

    @Override
    public void markDirty() {
        getInventory().markDirty();
        if (target.isOnline()) {
            target.getOpenContainer().detectAndSendChanges();
        }
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return !target.isOnline() || !target.isDead();
    }

    @Override
    public void openInventory(EntityPlayer player) {
        viewer.playSound(SoundEvents.BLOCK_CHEST_OPEN, 1F, 1F);
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (!target.isOnline()) {
            target.saveData();
        }
        viewer.playSound(SoundEvents.BLOCK_CHEST_CLOSE, 1F, 1F);
    }

    @Nullable
    @Override
    public InventoryFieldHolder getFieldHolder() {
        return new WrappedInventoryFieldHolder(getInventory());
    }
}
