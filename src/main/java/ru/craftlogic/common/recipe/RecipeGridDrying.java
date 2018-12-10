package ru.craftlogic.common.recipe;

import net.minecraft.item.ItemStack;
import ru.craftlogic.api.inventory.manager.InventoryManager;
import ru.craftlogic.api.inventory.manager.OneSlotInventoryManager;
import ru.craftlogic.api.recipe.RecipeGrid;

public interface RecipeGridDrying extends RecipeGrid {
    ItemStack getIngredient();

    @Override
    default int getGridSize() {
        return 1;
    }

    @Override
    default float takeExp(float amount, boolean simulate) {
        return 0;
    }

    @Override
    default InventoryManager getInventoryManager() {
        return new OneSlotInventoryManager(this.getIngredient());
    }
}
