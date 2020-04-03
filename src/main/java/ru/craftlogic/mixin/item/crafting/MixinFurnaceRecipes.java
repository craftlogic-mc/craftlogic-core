package ru.craftlogic.mixin.item.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.CraftRecipes;
import ru.craftlogic.api.recipe.Recipe;
import ru.craftlogic.api.recipe.RecipeGridSmelting;
import ru.craftlogic.api.recipe.RecipeSmelting;

import java.util.HashMap;
import java.util.Map;

@Mixin(FurnaceRecipes.class)
public abstract class MixinFurnaceRecipes {
    @Shadow
    public static FurnaceRecipes instance() {
        return null;
    }

    @Shadow protected abstract boolean compareItemStacks(ItemStack stack1, ItemStack stack2);

    /**
     * @author Radviger
     * @reason Advanced smelting recipes
     */
    @Overwrite
    public void addSmeltingRecipe(ItemStack input, ItemStack output, float experience) {
        CraftRecipes.registerSmeltingCompat(input.copy(), output.copy(), experience);
    }

    /**
     * @author Radviger
     * @reason Advanced smelting recipes
     */
    @Overwrite
    public ItemStack getSmeltingResult(ItemStack stack) {
        RecipeSmelting recipe = CraftRecipes.getMatchingRecipe(RecipeGridSmelting.class, new RecipeGridSmelting.Dummy(stack));
        return recipe == null ? ItemStack.EMPTY : recipe.getResult();
    }

    /**
     * @author Radviger
     * @reason Advanced smelting recipes
     */
    @Overwrite
    public float getSmeltingExperience(ItemStack stack) {
        float ret = stack.getItem().getSmeltingExperience(stack);
        if (ret != -1) return ret;
        Map<ResourceLocation, RecipeSmelting> recipes = CraftRecipes.getAllRecipes(RecipeGridSmelting.class);
        for (Recipe<RecipeGridSmelting> recipe : recipes.values()) {
            if (compareItemStacks(stack, ((RecipeSmelting) recipe).getResult())) {
                return ((RecipeSmelting) recipe).getExp();
            }
        }
        return 0;
    }

    /**
     * @author Radviger
     * @reason Advanced smelting recipes
     */
    @Overwrite
    public Map<ItemStack, ItemStack> getSmeltingList() {
        Map<ItemStack, ItemStack> result = new HashMap<>();
        Map<ResourceLocation, RecipeSmelting> recipes = CraftRecipes.getAllRecipes(RecipeGridSmelting.class);
        for (RecipeSmelting recipe : recipes.values()) {
            if (recipe.getInput() instanceof ItemStack) {
                result.put(((ItemStack) recipe.getInput()), recipe.getResult());
            }
        }
        return result;
    }
}
