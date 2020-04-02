package ru.craftlogic.api.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import static ru.craftlogic.api.recipe.Recipe.parseItem;

public class RecipeSmelting implements Recipe<RecipeGridSmelting> {
    private final ResourceLocation name;
    private final int time;
    private final float exp;
    private final Object input;
    private final ItemStack result;

    public RecipeSmelting(ResourceLocation name, JsonObject json) {
        this(
            name,
            JsonUtils.getInt(json, "time", 200),
            JsonUtils.getFloat(json, "exp", 0F),
            parseItem(json.get("input")),
            (ItemStack)parseItem(json.get("output"))
        );
    }

    public RecipeSmelting(ResourceLocation name, int time, float exp, Object input, ItemStack result) {
        this.name = name;
        this.time = time;
        this.exp = exp;
        this.input = input;
        this.result = result;
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
    public boolean matches(RecipeGridSmelting grid) {
        ItemStack input = grid.getInput();
        if (this.input instanceof DictStack) {
            return ((DictStack) this.input).test(input) && input.getCount() >= ((DictStack) this.input).getCount();
        } else if (this.input instanceof ItemStack) {
            return ((ItemStack) this.input).isItemEqual(input);
        }
        return false;
    }

    @Override
    public void consume(RecipeGridSmelting grid) {
        ItemStack input = grid.getInput();
        if (this.input instanceof DictStack) {
            input.shrink(((DictStack) this.input).getCount());
        } else if (this.input instanceof ItemStack) {
            input.shrink(((ItemStack) this.input).getCount());
        }
    }

    public Object getInput() {
        return input;
    }

    public ItemStack getResult() {
        return this.result;
    }

    public float getExp() {
        return this.exp;
    }
}
