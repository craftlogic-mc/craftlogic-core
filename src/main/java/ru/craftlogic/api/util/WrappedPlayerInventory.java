package ru.craftlogic.api.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import ru.craftlogic.api.inventory.holder.InventoryHolder;
import ru.craftlogic.api.inventory.manager.InventoryItemManager;
import ru.craftlogic.api.inventory.manager.WrappedInventoryItemManager;
import ru.craftlogic.api.world.OnlinePlayer;
import ru.craftlogic.api.world.Player;

import java.util.function.IntConsumer;

public class WrappedPlayerInventory implements InventoryHolder {
    private final InventoryPlayer inventory;
    private final OnlinePlayer viewer;
    private final Player player;

    public WrappedPlayerInventory(InventoryPlayer inventory, OnlinePlayer viewer, Player player) {
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
        return !this.getInventory().player.isDead;
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (!this.player.isOnline()) {
            this.player.saveData(this.viewer.getWorld(), true);
        }
    }

    static class SizeCheckedItemManager extends WrappedInventoryItemManager {
        public SizeCheckedItemManager(IInventory inventory) {
            super(inventory);
        }

        public SizeCheckedItemManager(IInventory inventory, IntConsumer updateListener) {
            super(inventory, updateListener);
        }

        @Override
        public ItemStack get(int slot) {
            if (slot < this.size()) {
                return super.get(slot);
            } else {
                return ItemStack.EMPTY;
            }
        }

        @Override
        public ItemStack split(int slot, int amount) {
            if (slot < this.size()) {
                return super.split(slot, amount);
            } else {
                return ItemStack.EMPTY;
            }
        }

        @Override
        public void set(int slot, ItemStack item) {
            if (slot < this.size()) {
                super.set(slot, item);
            }
        }

        @Override
        public ItemStack remove(int slot) {
            if (slot < this.size()) {
                return super.remove(slot);
            } else {
                return ItemStack.EMPTY;
            }
        }
    }
}
