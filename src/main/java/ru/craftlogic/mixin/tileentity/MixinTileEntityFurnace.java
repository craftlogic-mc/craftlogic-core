package ru.craftlogic.mixin.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.CraftRecipes;
import ru.craftlogic.api.inventory.manager.InventoryManager;
import ru.craftlogic.api.inventory.manager.ListInventoryManager;
import ru.craftlogic.api.recipe.Recipe;
import ru.craftlogic.api.recipe.RecipeGridSmelting;

@Mixin(TileEntityFurnace.class)
public class MixinTileEntityFurnace implements RecipeGridSmelting {
    @Shadow private NonNullList<ItemStack> furnaceItemStacks;

    @Override
    public ItemStack getInput() {
        return furnaceItemStacks.get(0);
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
        return new ListInventoryManager(furnaceItemStacks);
    }

    /**
     * @author Radviger
     * @reason Custom ite smelting time
     */
    @Overwrite
    public int getCookTime(ItemStack stack) {
        Recipe<MixinTileEntityFurnace> recipe = CraftRecipes.getMatchingRecipe(this);
        return recipe != null ? recipe.getTimeRequired() : 200;
    }
}
