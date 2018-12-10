package ru.craftlogic.common.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import ru.craftlogic.api.recipe.DictStack;
import ru.craftlogic.api.recipe.Recipe;

import static ru.craftlogic.api.recipe.Recipe.parseItem;

public class RecipeDrying implements Recipe<RecipeGridDrying> {
    private final ResourceLocation name;
    private final Object input;
    private final ItemStack result;
    private final int time;

    public RecipeDrying(ResourceLocation name, JsonObject json) {
        this(
            name,
            parseItem(json.get("input")),
            (ItemStack) parseItem(json.get("output")),
            JsonUtils.getInt(json, "time")
        );
    }

    public RecipeDrying(ResourceLocation name, Object input, ItemStack result, int time) {
        this.name = name;
        this.input = input;
        this.result = result;
        this.time = time;
    }

    @Override
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public int getTimeRequired() {
        return this.time;
    }

    @Override
    public boolean matches(RecipeGridDrying grid) {
        if (this.input instanceof ItemStack) {
            return ItemStack.areItemsEqual(grid.getIngredient(), (ItemStack) this.input)
                && ItemStack.areItemStackTagsEqual(grid.getIngredient(), (ItemStack) this.input);
        } else if (this.input instanceof DictStack) {
            return ((DictStack) this.input).test(grid.getIngredient());
        }
        return false;
    }

    @Override
    public void consume(RecipeGridDrying grid) {
        grid.getIngredient().shrink(1);
    }

    public ItemStack getResult() {
        return result;
    }
}
