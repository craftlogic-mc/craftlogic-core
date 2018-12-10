package ru.craftlogic.common.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import ru.craftlogic.api.inventory.manager.InventoryManager;
import ru.craftlogic.api.recipe.DictStack;
import ru.craftlogic.api.recipe.Recipe;

import java.util.List;

import static ru.craftlogic.api.recipe.Recipe.parseItem;

public class RecipeAlloying implements Recipe<RecipeGridAlloying> {
    private final ResourceLocation name;
    private final Object[] input;
    private final ItemStack result;
    private final float exp;
    private final int temperature;
    private final int time;

    public RecipeAlloying(ResourceLocation name, JsonObject json) {
        this(
            name,
            (ItemStack)parseItem(json.get("output")),
            JsonUtils.getFloat(json, "exp", 0F),
            JsonUtils.getInt(json, "temperature"),
            JsonUtils.getInt(json, "time"),
            parseInput(json.get("input"))
        );
    }

    public RecipeAlloying(ResourceLocation name, ItemStack result, float exp, int temperature, int time, Object... input) {
        this.name = name;
        this.input = input;
        this.result = result;
        this.exp = exp;
        this.temperature = temperature;
        this.time = time;
    }

    private static Object[] parseInput(JsonElement json) {
        if (json.isJsonArray()) {
            Object[] input = new Object[json.getAsJsonArray().size()];
            int i = 0;
            for (JsonElement e : json.getAsJsonArray()) {
                input[i++] = parseItem(e);
            }
            return input;
        } else {
            return new Object[] { parseItem(json) };
        }
    }

    @Override
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public boolean matches(RecipeGridAlloying grid) {
        float temperature = grid.getTemperature();
        int gridSize = grid.getGridSize();
        List<ItemStack> inventory = grid.getIngredients();
        if (temperature >= this.temperature) {
            Object[] input = this.input;
            if (input.length > gridSize) {
                return false;
            }
            for (Object ingredient : input) {
                if (ingredient instanceof ItemStack) {
                    ItemStack inputStack = (ItemStack) ingredient;
                    int rc = inputStack.getCount();

                    for (int i = 0; i < gridSize; ++i) {
                        ItemStack ic = inventory.get(i);
                        if (!ic.isEmpty()) {
                            if (ic.isItemEqual(inputStack)) {
                                rc -= ic.getCount();
                            }

                            if (rc <= 0) {
                                break;
                            }
                        }
                    }

                    if (rc > 0) {
                        return false;
                    }
                } else if (ingredient instanceof DictStack) {
                    DictStack inputStack = (DictStack) ingredient;
                    int rc = inputStack.getCount();

                    for (int i = 0; i < gridSize; ++i) {
                        ItemStack ic = inventory.get(i);
                        if (!ic.isEmpty()) {
                            if (inputStack.test(ic)) {
                                rc -= ic.getCount();
                            }

                            if (rc <= 0) {
                                break;
                            }
                        }
                    }

                    if (rc > 0) {
                        return false;
                    }
                }
            }

            return true;
        }
        return false;
    }

    @Override
    public void consume(RecipeGridAlloying grid) {
        InventoryManager inventory = grid.getInventoryManager();
        int gridSize = grid.getGridSize();

        for (Object ingredient : input) {
            if (ingredient instanceof ItemStack) {
                ItemStack inputStack = (ItemStack) ingredient;
                int rc = inputStack.getCount();

                for (int i = 0; i < gridSize; ++i) {
                    ItemStack ic = inventory.get(i);
                    if (!ic.isEmpty() && ic.isItemEqual(inputStack)) {
                        rc -= ic.getCount();
                        if (rc < 0) {
                            ic.setCount(-rc);
                        } else if (ic.getItem().hasContainerItem(ic)) {
                            inventory.set(i, ic.getItem().getContainerItem(ic));
                        } else {
                            inventory.set(i, ItemStack.EMPTY);
                        }

                        if (rc <= 0) {
                            break;
                        }
                    }
                }
            } else if (ingredient instanceof DictStack) {
                DictStack inputStack = (DictStack) ingredient;
                int rc = inputStack.getCount();

                for (int i = 0; i < gridSize; ++i) {
                    ItemStack ic = inventory.get(i);
                    if (!ic.isEmpty() && inputStack.test(ic)) {
                        rc -= ic.getCount();
                        if (rc < 0) {
                            ic.setCount(-rc);
                        } else {
                            inventory.set(i, ItemStack.EMPTY);
                        }

                        if (rc <= 0) {
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getTimeRequired() {
        return this.time;
    }

    public int getTemperature() {
        return this.temperature;
    }

    public ItemStack getResult() {
        return this.result;
    }

    public float getExp() {
        return this.exp;
    }
}
