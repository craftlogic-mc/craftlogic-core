package ru.craftlogic.util;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import ru.craftlogic.api.util.Registrable;

public class DummyRecipe extends Registrable<IRecipe> implements IRecipe {
    @Override
    public boolean matches(InventoryCrafting bench, World world) {
        return false;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting bench) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }
}
