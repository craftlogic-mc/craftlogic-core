package ru.craftlogic.api.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.model.ModelAutoReg;
import ru.craftlogic.api.model.ModelManager;

public class ItemFoodBase extends ItemFood implements ModelAutoReg {
    protected final String name;

    public ItemFoodBase(String name, CreativeTabs tab, int heal, boolean petFood) {
        this(name, tab, heal, 0.6F, petFood);
    }

    public ItemFoodBase(String name, CreativeTabs tab, int heal, float saturation, boolean petFood) {
        super(heal, saturation, petFood);
        this.name = name;
        this.setCreativeTab(tab);
        this.setRegistryName(name);
        this.setTranslationKey(name);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        modelManager.registerItemModel(this);
    }
}
