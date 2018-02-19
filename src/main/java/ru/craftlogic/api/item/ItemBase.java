package ru.craftlogic.api.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.ModelAutoReg;
import ru.craftlogic.client.ModelManager;

public class ItemBase extends Item implements ModelAutoReg {
    protected final String name;
    protected final String modid;

    public ItemBase(String name, CreativeTabs tab) {
        this.name = name;
        this.modid = CraftLogic.getActiveModId();
        this.setCreativeTab(tab);
        this.setRegistryName(name);
        this.setUnlocalizedName(name);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        modelManager.registerItemModel(this);
    }
}
