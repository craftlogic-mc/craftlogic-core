package ru.craftlogic.api.recipe;


import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;
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
