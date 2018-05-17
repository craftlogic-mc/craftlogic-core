package ru.craftlogic.api.inventory.manager;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.function.IntConsumer;

public class SizeCheckedItemManager extends WrappedInventoryItemManager {
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