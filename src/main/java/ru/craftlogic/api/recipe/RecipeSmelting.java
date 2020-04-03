package ru.craftlogic.api.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

public class RecipeSmelting extends RecipeProcessingMachine<RecipeGridSmelting> {
    public RecipeSmelting(ResourceLocation name, JsonObject json) {
        super(name, json);
    }

    public RecipeSmelting(ResourceLocation name, int time, float exp, Object input, ItemStack result) {
        super(name, time, exp, input, result);
    }

    @Override
    public boolean matches(RecipeGridSmelting grid) {
        ItemStack input = grid.getInput();
        if (this.input instanceof DictStack) {
            return ((DictStack) this.input).test(input) && input.getCount() >= ((DictStack) this.input).getCount();
        } else if (this.input instanceof ItemStack) {
            ItemStack i = (ItemStack) this.input;
            return i.getItem() == input.getItem() && (i.getMetadata() == OreDictionary.WILDCARD_VALUE || i.getMetadata() == input.getMetadata());
        }
        return false;
    }
}
