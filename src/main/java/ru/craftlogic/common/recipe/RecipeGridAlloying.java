package ru.craftlogic.common.recipe;

import net.minecraft.item.ItemStack;
import ru.craftlogic.api.recipe.RecipeGrid;

import java.util.List;

public interface RecipeGridAlloying extends RecipeGrid {
    int getTemperature();
    List<ItemStack> getIngredients();
}
