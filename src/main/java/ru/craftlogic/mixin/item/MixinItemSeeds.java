package ru.craftlogic.mixin.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemSeeds.class)
public class MixinItemSeeds extends Item {
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            String id = this.getRegistryName().toString();
            if (id.startsWith("minecraft:") && id.endsWith("seeds")) {
                if (this == Items.WHEAT_SEEDS) {
                    items.add(new ItemStack(Items.WHEAT_SEEDS));
                    items.add(new ItemStack(Items.PUMPKIN_SEEDS));
                    items.add(new ItemStack(Items.MELON_SEEDS));
                    items.add(new ItemStack(Items.BEETROOT_SEEDS));
                }
            } else {
                items.add(new ItemStack(this));
            }
        }
    }
}
