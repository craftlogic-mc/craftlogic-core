package ru.craftlogic.api.recipe;

import ru.craftlogic.api.inventory.InventoryHolder;

public interface RecipeGrid extends InventoryHolder {
    int getGridSize();
    float takeExp();
}
