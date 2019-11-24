package ru.craftlogic.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import ru.craftlogic.api.block.Colored;
import ru.craftlogic.api.item.ItemFoodBase;

public class ItemBerry extends ItemFoodBase implements Colored {
    public ItemBerry() {
        super("berry", CreativeTabs.FOOD, 4, 0.4F, false);
    }

    @Override
    public int getItemColor(ItemStack stack, int tint) {
        return tint == 0 ? 0xFF0000 : 0xFFFFFF;
    }
}
