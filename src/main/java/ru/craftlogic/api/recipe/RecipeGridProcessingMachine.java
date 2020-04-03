package ru.craftlogic.api.recipe;

import net.minecraft.item.ItemStack;

public interface RecipeGridProcessingMachine extends RecipeGrid {
    ItemStack getInput();

    @Override
    default int getGridSize() {
        return 1;
    }
}
