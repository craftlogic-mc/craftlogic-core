package ru.craftlogic.api.recipe;

import net.minecraft.util.ResourceLocation;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class Recipes {
    private static final Map<Class<? extends RecipeGrid>, Map<ResourceLocation, Recipe>> RECIPES = new HashMap<>();

    public static <M extends RecipeGrid> void registerRecipe(Class<M> matrixType, Recipe<M> recipe) {
        if (RECIPES.computeIfAbsent(matrixType, k -> new HashMap<>()).put(recipe.getName(), recipe) != null) {
            throw new IllegalStateException("Recipe with name " + recipe.getName() + " already registered!");
        }
    }

    public static <M extends RecipeGrid, R extends Recipe<M>> R getMatchingRecipe(M matrix) {
        for (Class<?> itf : matrix.getClass().getInterfaces()) {
            if (RecipeGrid.class.isAssignableFrom(itf)) {
                Class<? extends RecipeGrid> matrixType = (Class<? extends RecipeGrid>) itf;
                if (RECIPES.containsKey(matrixType)) {
                    TreeSet<Recipe<M>> result = new TreeSet<>(Comparator.naturalOrder());
                    for (Recipe<M> recipe : RECIPES.get(matrixType).values()) {
                        if (recipe.matches(matrix)) {
                            result.add(recipe);
                        }
                    }
                    if (!result.isEmpty()) {
                        return (R) result.first();
                    }
                }
            }
        }
        return null;
    }

    public static <M extends RecipeGrid, R extends Recipe<M>> R getByName(Class<M> matrixType, ResourceLocation name) {
        if (RECIPES.containsKey(matrixType)) {
            return (R) RECIPES.get(matrixType).get(name);
        }
        return null;
    }
}
