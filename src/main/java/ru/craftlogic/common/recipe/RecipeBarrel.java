package ru.craftlogic.common.recipe;

import ru.craftlogic.api.barrel.BarrelModeType;
import ru.craftlogic.api.recipe.Recipe;

public abstract class RecipeBarrel implements Recipe<RecipeGridBarrel> {
    public abstract BarrelModeType getMode();
}
