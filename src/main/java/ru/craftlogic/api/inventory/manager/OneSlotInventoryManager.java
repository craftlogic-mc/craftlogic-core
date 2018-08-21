package ru.craftlogic.api.inventory.manager;

import net.minecraft.item.ItemStack;

public class OneSlotInventoryManager implements InventoryManager {
    private ItemStack item;

    public OneSlotInventoryManager(ItemStack item) {
        this.item = item;
    }

    @Override
    public ItemStack get(int slot) {
        if (slot == 0) {
            return this.item;
        } else {
            throw new ArrayIndexOutOfBoundsException(slot);
        }
    }

    @Override
    public ItemStack split(int slot, int amount) {
        if (slot == 0) {
            return this.item.splitStack(amount);
        } else {
            throw new ArrayIndexOutOfBoundsException(slot);
        }
    }

    @Override
    public void set(int slot, ItemStack item) {
        if (slot == 0) {
            this.item = item;
        } else {
            throw new ArrayIndexOutOfBoundsException(slot);
        }
    }

    @Override
    public ItemStack remove(int slot) {
        if (slot == 0) {
            ItemStack result = this.item;
            this.item = ItemStack.EMPTY;
            return result;
        } else {
            throw new ArrayIndexOutOfBoundsException(slot);
        }
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return !this.item.isEmpty();
    }

    @Override
    public void clear() {
        this.item = ItemStack.EMPTY;
    }
}
