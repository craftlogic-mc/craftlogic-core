package ru.craftlogic.common.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import ru.craftlogic.api.CraftBarrelModes;
import ru.craftlogic.api.barrel.BarrelModeType;
import ru.craftlogic.api.recipe.DictStack;

import static ru.craftlogic.api.CraftAPI.parseColor;
import static ru.craftlogic.api.recipe.Recipe.parseItem;

public class RecipeBarrelCompost extends RecipeBarrel {
    private final ResourceLocation name;
    private final Object ingredient;
    private final int color;
    private final int time;
    private final int gain;

    public RecipeBarrelCompost(ResourceLocation name, JsonObject json) {
        this(
            name,
            parseItem(json.get("item")),
            parseColor(json.get("color")),
            JsonUtils.getInt(json, "time"),
            JsonUtils.getInt(json, "gain")
        );
    }

    public RecipeBarrelCompost(ResourceLocation name, Object ingredient, int color, int time, int gain) {
        this.name = name;
        this.ingredient = ingredient;
        this.color = color;
        this.time = time;
        this.gain = gain;
    }

    @Override
    public BarrelModeType getMode() {
        return CraftBarrelModes.COMPOST;
    }

    public int getColor() {
        return color;
    }

    public int getGain() {
        return gain;
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
    public boolean matches(RecipeGridBarrel grid) {
        if (this.ingredient instanceof ItemStack) {
            return ItemStack.areItemsEqual(grid.getIngredient(), (ItemStack) this.ingredient)
                && ItemStack.areItemStackTagsEqual(grid.getIngredient(), (ItemStack) this.ingredient);
        } else if (this.ingredient instanceof DictStack) {
            return ((DictStack) this.ingredient).test(grid.getIngredient());
        }
        return false;
    }

    @Override
    public void consume(RecipeGridBarrel grid) {
        grid.getIngredient().shrink(1);
    }
}
