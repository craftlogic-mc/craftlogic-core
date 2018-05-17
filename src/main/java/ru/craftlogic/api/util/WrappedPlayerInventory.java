package ru.craftlogic.api.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import ru.craftlogic.api.inventory.InventoryHolder;
import ru.craftlogic.api.inventory.manager.InventoryItemManager;
import ru.craftlogic.api.inventory.manager.SizeCheckedItemManager;
import ru.craftlogic.api.world.OfflinePlayer;
import ru.craftlogic.api.world.Player;

public class WrappedPlayerInventory implements InventoryHolder {
    private final InventoryPlayer inventory;
    private final Player viewer;
    private final OfflinePlayer player;

    public WrappedPlayerInventory(InventoryPlayer inventory, Player viewer, OfflinePlayer player) {
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
    public InventoryItemManager getItemManager() {
        return new SizeCheckedItemManager(this.getInventory(), slot -> this.markDirty());
    }

    private InventoryPlayer getInventory() {
        if (this.player.isOnline()) {
            return this.player.asOnline().getInventory();
        } else {
            return this.inventory;
        }
    }

    @Override
    public int getSizeInventory() {
        return 45;
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
        this.viewer.playSound(SoundEvents.BLOCK_CHEST_OPEN, 1F, 1F);
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (!this.player.isOnline()) {
            this.player.saveData(this.viewer.getWorld(), true);
        }
        this.viewer.playSound(SoundEvents.BLOCK_CHEST_CLOSE, 1F, 1F);
    }
}
