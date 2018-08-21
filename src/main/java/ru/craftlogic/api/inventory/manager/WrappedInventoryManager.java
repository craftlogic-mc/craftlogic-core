package ru.craftlogic.api.inventory.manager;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.function.IntConsumer;

public class WrappedInventoryManager implements InventoryManager {
    private final IInventory inventory;
    private final IntConsumer updateListener;

    public WrappedInventoryManager(IInventory inventory) {
        this(inventory, null);
    }

    public WrappedInventoryManager(IInventory inventory, IntConsumer updateListener) {
        this.inventory = inventory;
        this.updateListener = updateListener;
    }

    @Override
    public ItemStack get(int slot) {
        return this.inventory.getStackInSlot(slot);
    }

    @Override
    public ItemStack split(int slot, int amount) {
        ItemStack result = this.inventory.decrStackSize(slot, amount);
        if (this.updateListener != null) {
            this.updateListener.accept(slot);
        }
        return result;
    }

    @Override
    public void set(int slot, ItemStack item) {
        this.inventory.setInventorySlotContents(slot, item);
        if (this.updateListener != null) {
            this.updateListener.accept(slot);
        }
    }

    @Override
    public ItemStack remove(int slot) {
        ItemStack result = this.inventory.removeStackFromSlot(slot);
        if (this.updateListener != null) {
            this.updateListener.accept(slot);
        }
        return result;
    }

    @Override
    public void clear() {
        this.inventory.clear();
        if (this.updateListener != null) {
            for (int i = 0; i < this.inventory.getSizeInventory(); i++) {
                this.updateListener.accept(i);
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return this.inventory.isEmpty();
    }

    @Override
    public int size() {
        return this.inventory.getSizeInventory();
    }
}
