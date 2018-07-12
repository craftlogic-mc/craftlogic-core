package ru.craftlogic.api.recipe;

import net.minecraft.util.ResourceLocation;

public interface Recipe<G extends RecipeGrid> extends Comparable<Recipe> {
    ResourceLocation getName();
    int getTimeRequired();
    boolean matches(G grid);
    void consume(G grid);

    @Override
    default int compareTo(Recipe other) {
        return this.getName().compareTo(other.getName());
    }
}
