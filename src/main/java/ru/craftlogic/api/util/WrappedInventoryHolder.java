package ru.craftlogic.api.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import ru.craftlogic.api.inventory.holder.InventoryHolder;
import ru.craftlogic.api.inventory.manager.InventoryItemManager;
import ru.craftlogic.api.inventory.manager.WrappedInventoryItemManager;
import ru.craftlogic.api.world.Locateable;
import ru.craftlogic.api.world.Location;

public class WrappedInventoryHolder implements InventoryHolder, Locateable {
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
    public ItemStack getStackInSlot(int slotId) {
        return inventory.getStackInSlot(slotId);
    }

    @Override
    public ItemStack decrStackSize(int slotId, int amount) {
        return inventory.decrStackSize(slotId, amount);
    }

    @Override
    public ItemStack removeStackFromSlot(int slotId) {
        return inventory.removeStackFromSlot(slotId);
    }

    @Override
    public void setInventorySlotContents(int slotId, ItemStack stack) {
        inventory.setInventorySlotContents(slotId, stack);
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
    public boolean isItemValidForSlot(int slotId, ItemStack stack) {
        return inventory.isItemValidForSlot(slotId, stack);
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
