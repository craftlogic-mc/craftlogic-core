package ru.craftlogic.common.item;

import net.minecraft.creativetab.CreativeTabs;
import ru.craftlogic.api.item.ItemFoodBase;

public class ItemBerry extends ItemFoodBase {
    public ItemBerry(String name, int heal, float saturation) {
        super(name, CreativeTabs.FOOD, heal, saturation, false);
    }
}
