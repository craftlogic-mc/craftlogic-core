package ru.craftlogic.api.recipe;

import com.google.common.base.MoreObjects;
import com.google.gson.JsonElement;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import ru.craftlogic.api.util.Json2NBT;

public interface Recipe<G extends RecipeGrid> extends Comparable<Recipe<G>> {
    ResourceLocation getName();
    int getTimeRequired();
    boolean matches(G grid);
    void consume(G grid);

    @Override
    default int compareTo(Recipe other) {
        return this.getName().compareTo(other.getName());
    }

    static Object parseItem(JsonElement json) {
        if (json.isJsonPrimitive()) {
            String name = json.getAsString();
            if (name.startsWith("dictionary:")) {
                return new DictStack(name.substring(11));
            } else {
                ResourceLocation id = new ResourceLocation(name);
                return new ItemStack(MoreObjects.firstNonNull(Item.REGISTRY.getObject(id), Items.AIR));
            }
        } else {
            String name = JsonUtils.getString(json.getAsJsonObject(), "name");
            int amount = JsonUtils.getInt(json.getAsJsonObject(), "amount", 1);
            if (name.startsWith("dictionary:")) {
                return new DictStack(name.substring(11), amount);
            } else {
                ResourceLocation id = new ResourceLocation(name);
                int meta = JsonUtils.getInt(json.getAsJsonObject(), "meta", 0);
                Item item = MoreObjects.firstNonNull(Item.REGISTRY.getObject(id), Items.AIR);
                if (item == Items.AIR) {
                    return ItemStack.EMPTY;
                } else {
                    ItemStack result = new ItemStack(item, amount, meta);
                    if (json.getAsJsonObject().has("nbt")) {
                        NBTBase nbt = Json2NBT.jsonToNbt(json.getAsJsonObject().get("nbt"));
                        result.setTagCompound((NBTTagCompound) nbt);
                    }
                    return result;
                }
            }
        }
    }
}
