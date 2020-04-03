package ru.craftlogic.api.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import static ru.craftlogic.api.recipe.Recipe.parseItem;

public abstract class RecipeProcessingMachine<G extends RecipeGridProcessingMachine> implements Recipe<G> {
    protected final ResourceLocation name;
    protected final int time;
    protected final float exp;
    protected final Object input;
    protected final ItemStack result;

    protected RecipeProcessingMachine(ResourceLocation name, JsonObject json) {
        this(
            name,
            JsonUtils.getInt(json, "time", 200),
            JsonUtils.getFloat(json, "exp", 0F),
            parseItem(json.get("input")),
            (ItemStack)parseItem(json.get("output"))
        );
    }

    protected RecipeProcessingMachine(ResourceLocation name, int time, float exp, Object input, ItemStack result) {
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
    public boolean matches(G grid) {
        ItemStack input = grid.getInput();
        if (this.input instanceof DictStack) {
            return ((DictStack) this.input).test(input) && input.getCount() >= ((DictStack) this.input).getCount();
        } else if (this.input instanceof ItemStack) {
            return ((ItemStack) this.input).isItemEqual(input);
        }
        return false;
    }

    @Override
    public void consume(G grid) {
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
