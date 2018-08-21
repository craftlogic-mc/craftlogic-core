package ru.craftlogic.api.recipe;

import ru.craftlogic.api.inventory.manager.InventoryManager;

public interface RecipeGrid {
    int getGridSize();
    float takeExp(float amount, boolean simulate);
    InventoryManager getInventoryManager();
}
