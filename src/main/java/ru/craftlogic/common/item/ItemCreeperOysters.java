package ru.craftlogic.common.item;

import net.minecraft.creativetab.CreativeTabs;
import ru.craftlogic.api.item.ItemFoodBase;

public class ItemCreeperOysters extends ItemFoodBase {
    public ItemCreeperOysters() {
        super("creeper_oysters", CreativeTabs.MATERIALS, 5, 0.2F, true);
    }
}
