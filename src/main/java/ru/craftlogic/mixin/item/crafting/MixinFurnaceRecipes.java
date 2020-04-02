package ru.craftlogic.mixin.item.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.CraftRecipes;
import ru.craftlogic.api.recipe.RecipeGridSmelting;
import ru.craftlogic.api.recipe.RecipeSmelting;

import java.util.*;

@Mixin(FurnaceRecipes.class)
public abstract class MixinFurnaceRecipes {
    @Shadow
    public static FurnaceRecipes instance() {
        return null;
    }

    @Shadow @Final private Map<ItemStack, ItemStack> smeltingList;

    @Inject(method = "addSmeltingRecipe", at = @At("TAIL"))
    public void onRecipeAdded(ItemStack input, ItemStack stack, float experience, CallbackInfo info) {
        CraftRecipes.registerSmeltingCompat(input, stack, experience);
    }

    /**
     * @author Radviger
     * @reason Advanced smelting recipes
     */
    @Overwrite
    public ItemStack getSmeltingResult(ItemStack stack) {
        RecipeSmelting recipe = CraftRecipes.getMatchingRecipe(new RecipeGridSmelting.Dummy(stack));
        return recipe == null ? ItemStack.EMPTY : recipe.getResult();
    }

    /**
     * @author Radviger
     * @reason Advanced smelting recipes
     */
    @Overwrite
    public float getSmeltingExperience(ItemStack stack) {
        RecipeSmelting recipe = CraftRecipes.getMatchingRecipe(new RecipeGridSmelting.Dummy(stack));
        return recipe == null ? 0 : recipe.getExp();
    }

    /**
     * @author Radviger
     * @reason Advanced smelting recipes
     */
    @Overwrite
    public Map<ItemStack, ItemStack> getSmeltingList() {
        if (CraftAPI.isPostInit()) {
            Map<ItemStack, ItemStack> result = new HashMap<>();
            Map<ResourceLocation, RecipeSmelting> recipes = CraftRecipes.getAllRecipes(RecipeGridSmelting.class);
            for (RecipeSmelting recipe : recipes.values()) {
                if (recipe.getInput() instanceof ItemStack) {
                    result.put(((ItemStack) recipe.getInput()), recipe.getResult());
                }
            }
            return result;
        } else {
            return Collections.emptyMap();
        }
    }
}
