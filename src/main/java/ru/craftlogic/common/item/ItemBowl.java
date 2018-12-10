package ru.craftlogic.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import ru.craftlogic.api.item.ItemBase;

import javax.annotation.Nullable;

public class ItemBowl extends ItemBase {
    public ItemBowl() {
        super("bowl", CreativeTabs.FOOD);
    }

    @Nullable
    @Override
    public CreativeTabs getCreativeTab() {
        return CreativeTabs.FOOD;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            items.add(new ItemStack(this));
            //items.add(new ItemStack(CraftItems.MILK_BOWL));
            //items.add(new ItemStack(CraftItems.WATER_BOWL));
            items.add(new ItemStack(Items.MUSHROOM_STEW));
            items.add(new ItemStack(Items.RABBIT_STEW));
            items.add(new ItemStack(Items.BEETROOT_SOUP));
        }
    }
}
