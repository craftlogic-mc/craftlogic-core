package ru.craftlogic.api.recipe;


import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;
import java.util.function.Predicate;

public class OreStack implements Predicate<ItemStack> {
    public String material;
    public int amount;

    public OreStack(String material, int amount) {
        this.material = material;
        this.amount = amount;
    }

    public OreStack(String material) {
        this.material = material;
        this.amount = 1;
    }

    @Override
    public boolean test(ItemStack stack) {
        List<ItemStack> ores = OreDictionary.getOres(this.material);
        for (ItemStack s : ores) {
            if (s.isItemEqual(stack)) {
                return true;
            }
        }
        return false;
    }
}
