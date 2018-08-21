package ru.craftlogic.api.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.util.text.ITextComponent;
import ru.craftlogic.api.inventory.InventoryFieldHolder;
import ru.craftlogic.api.inventory.InventoryHolder;
import ru.craftlogic.api.inventory.WrappedInventoryFieldHolder;
import ru.craftlogic.api.inventory.manager.InventoryManager;
import ru.craftlogic.api.inventory.manager.SizeCheckedInventoryManager;
import ru.craftlogic.api.world.OfflinePlayer;
import ru.craftlogic.api.world.Player;

import javax.annotation.Nullable;

public class WrappedPlayerEnderchest implements InventoryHolder {
    private final InventoryEnderChest inventory;
    private final Player viewer;
    private final OfflinePlayer player;

    public WrappedPlayerEnderchest(InventoryEnderChest inventory, Player viewer, OfflinePlayer player) {
        this.inventory = inventory;
        this.viewer = viewer;
        this.player = player;
    }

    @Override
    public String getName() {
        return this.player.getProfile().getName();
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.player.getDisplayName();
    }

    @Override
    public InventoryManager getInventoryManager() {
        return new SizeCheckedInventoryManager(this.getInventory(), slot -> this.markDirty());
    }

    private InventoryEnderChest getInventory() {
        return this.player.isOnline() ? this.player.asOnline().getEnderInventory() : this.inventory;
    }

    @Override
    public int getSizeInventory() {
        return 27;
    }

    @Override
    public void markDirty() {
        this.getInventory().markDirty();
        if (!this.player.isOnline()) {
            this.player.saveData(this.viewer.getWorld(), false);
        } else {
            this.player.asOnline().getOpenContainer().detectAndSendChanges();
        }
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return !this.player.isOnline() || !this.player.asOnline().isDead();
    }

    @Override
    public void openInventory(EntityPlayer player) {
        this.viewer.playSound(SoundEvents.BLOCK_ENDERCHEST_OPEN, 1F, 1F);
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (!this.player.isOnline()) {
            this.player.saveData(this.viewer.getWorld(), true);
        }
        this.viewer.playSound(SoundEvents.BLOCK_ENDERCHEST_CLOSE, 1F, 1F);
    }

    @Nullable
    @Override
    public InventoryFieldHolder getFieldHolder() {
        return new WrappedInventoryFieldHolder(inventory);
    }
}
