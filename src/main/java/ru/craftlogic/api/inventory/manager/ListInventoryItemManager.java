package ru.craftlogic.api.inventory.manager;

import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.function.IntConsumer;

public class ListInventoryItemManager implements InventoryItemManager {
    private final NonNullList<ItemStack> inventory;
    private final IntConsumer updateListener;

    public ListInventoryItemManager(NonNullList<ItemStack> inventory) {
        this(inventory, null);
    }

    public ListInventoryItemManager(NonNullList<ItemStack> inventory, IntConsumer updateListener) {
        this.inventory = inventory;
        this.updateListener = updateListener;
    }

    @Override
    public ItemStack get(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack split(int slot, int amount) {
        ItemStack result = ItemStackHelper.getAndSplit(this.inventory, slot, amount);
        if (this.updateListener != null) {
            this.updateListener.accept(slot);
        }
        return result;
    }

    @Override
    public void set(int slot, ItemStack item) {
        this.inventory.set(slot, item);
        if (this.updateListener != null) {
            this.updateListener.accept(slot);
        }
    }

    @Override
    public ItemStack remove(int slot) {
        ItemStack result = ItemStackHelper.getAndRemove(this.inventory, slot);
        if (this.updateListener != null) {
            this.updateListener.accept(slot);
        }
        return result;
    }

    @Override
    public void clear() {
        this.inventory.clear();
        if (this.updateListener != null) {
            for (int i = 0; i < this.inventory.size(); i++) {
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
        return this.inventory.size();
    }
}
