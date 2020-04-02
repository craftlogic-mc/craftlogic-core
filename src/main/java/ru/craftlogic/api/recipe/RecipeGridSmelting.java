package ru.craftlogic.api.recipe;

import net.minecraft.item.ItemStack;
import ru.craftlogic.api.inventory.manager.InventoryManager;
import ru.craftlogic.api.inventory.manager.OneSlotInventoryManager;

public interface RecipeGridSmelting extends RecipeGrid {
    ItemStack getInput();

    class Dummy implements RecipeGridSmelting {
        private final ItemStack input;

        public Dummy(ItemStack input) {
            this.input = input;
        }

        @Override
        public ItemStack getInput() {
            return input;
        }

        @Override
        public int getGridSize() {
            return 1;
        }

        @Override
        public float takeExp(float amount, boolean simulate) {
            return 0;
        }

        @Override
        public InventoryManager getInventoryManager() {
            return new OneSlotInventoryManager(input);
        }
    }
}
