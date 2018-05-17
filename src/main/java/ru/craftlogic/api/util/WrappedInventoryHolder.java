package ru.craftlogic.api.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import ru.craftlogic.api.inventory.InventoryHolder;
import ru.craftlogic.api.inventory.manager.InventoryItemManager;
import ru.craftlogic.api.inventory.manager.WrappedInventoryItemManager;
import ru.craftlogic.api.world.Locatable;
import ru.craftlogic.api.world.Location;

public class WrappedInventoryHolder implements InventoryHolder, Locatable {
    protected final IInventory inventory;
    protected final Location location;

    public WrappedInventoryHolder(IInventory inventory, Location location) {
        this.inventory = inventory;
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public InventoryItemManager getItemManager() {
        return new WrappedInventoryItemManager(this.inventory);
    }

    @Override
    public int getSizeInventory() {
        return inventory.getSizeInventory();
    }

    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        return inventory.decrStackSize(slot, amount);
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        return inventory.removeStackFromSlot(slot);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inventory.setInventorySlotContents(slot, stack);
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public void markDirty() {
        inventory.markDirty();
    }

    @Override
    public ITextComponent getDisplayName() {
        return inventory.getDisplayName();
    }

    @Override
    public int getField(int id) {
        return inventory.getField(id);
    }

    @Override
    public void setField(int id, int value) {
        inventory.setField(id, value);
    }

    @Override
    public int getFieldCount() {
        return inventory.getFieldCount();
    }

    @Override
    public String getName() {
        return inventory.getName();
    }

    @Override
    public boolean hasCustomName() {
        return inventory.hasCustomName();
    }

    @Override
    public void openInventory(EntityPlayer player) {
        inventory.openInventory(player);
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        inventory.closeInventory(player);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return inventory.isItemValidForSlot(slot, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return inventory.getInventoryStackLimit();
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return inventory.isUsableByPlayer(player);
    }
}
