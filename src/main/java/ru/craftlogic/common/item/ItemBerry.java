package ru.craftlogic.common.item;

import net.minecraft.creativetab.CreativeTabs;
import ru.craftlogic.api.item.ItemFoodBase;

public class ItemBerry extends ItemFoodBase {
    public ItemBerry(String name) {
        super(name, CreativeTabs.FOOD, 4, 0.4F, false);
    }
}
