package ru.craftlogic.common.recipe;

import net.minecraft.item.ItemStack;
import ru.craftlogic.api.inventory.manager.InventoryManager;
import ru.craftlogic.api.inventory.manager.OneSlotInventoryManager;
import ru.craftlogic.api.recipe.RecipeGrid;

public class RecipeGridOneSlot implements RecipeGrid {
    private ItemStack ingredient;

    public RecipeGridOneSlot(ItemStack ingredient) {
        this.ingredient = ingredient;
    }

    public ItemStack getIngredient() {
        return this.ingredient;
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
        return new OneSlotInventoryManager(this.getIngredient());
    }
}
