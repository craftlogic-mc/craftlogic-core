package ru.craftlogic.api.inventory.manager;

import net.minecraft.item.ItemStack;

public interface InventoryManager {
    ItemStack get(int slot);

    ItemStack split(int slot, int amount);

    void set(int slot, ItemStack item);

    ItemStack remove(int slot);

    int size();

    boolean isEmpty();

    void clear();
}
