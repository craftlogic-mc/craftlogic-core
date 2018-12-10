package ru.craftlogic.api.recipe;


import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.function.Predicate;

public class DictStack implements Predicate<ItemStack> {
    private String material;
    private int count;

    public DictStack(String material, int count) {
        this.material = material;
        this.count = count;
    }

    public DictStack(String material) {
        this.material = material;
        this.count = 1;
    }

    public String getMaterial() {
        return material;
    }

    public int getCount() {
        return count;
    }

    public NonNullList<ItemStack> getAllVariants() {
        return OreDictionary.getOres(this.material);
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!stack.isEmpty()) {
            int id = OreDictionary.getOreID(this.material);
            for (int oreID : OreDictionary.getOreIDs(stack)) {
                if (oreID == id) {
                    return true;
                }
            }
        }
        return false;
    }
}
