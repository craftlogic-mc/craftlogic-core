package ru.craftlogic.common.item;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

public class ItemMushroom extends ItemFood {
    private Block block;

    public ItemMushroom(Block block) {
        super(2, 0.4F, false);
        this.block = block;
        this.setCreativeTab(CreativeTabs.FOOD);
        this.setPotionEffect(new PotionEffect(MobEffects.HUNGER, 600, 0), 0.8F);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return this.block.getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedName() {
        return this.block.getUnlocalizedName();
    }
}
