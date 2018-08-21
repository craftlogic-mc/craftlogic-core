package ru.craftlogic.common.recipe;

import net.minecraft.item.ItemStack;
import ru.craftlogic.api.barrel.Barrel;

public class RecipeGridBarrel extends RecipeGridOneSlot {
    private final Barrel barrel;

    public RecipeGridBarrel(ItemStack ingredient, Barrel barrel) {
        super(ingredient);
        this.barrel = barrel;
    }

    public Barrel getBarrel() {
        return barrel;
    }
}
