package ru.craftlogic.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import ru.craftlogic.api.item.ItemFoodBase;

public class ItemMoss extends ItemFoodBase {
    public ItemMoss() {
        super("moss", CreativeTabs.FOOD, 3, 0.5F, false);
        this.setPotionEffect(new PotionEffect(MobEffects.REGENERATION, 100, 0), 0.4F);
    }
}
