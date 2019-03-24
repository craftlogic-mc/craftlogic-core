package ru.craftlogic.api.util;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import java.util.function.Function;

public class CreativeTab extends CreativeTabs {
    private final Function<CreativeTab, ItemStack> iconFactory;
    private final boolean iconCaching;
    private ItemStack cachedIcon;

    public static CreativeTab registerTab(String unlocalizedName, Function<CreativeTab, ItemStack> iconFactory) {
        return registerTab(unlocalizedName, iconFactory, true);
    }

    public static CreativeTab registerTab(String unlocalizedName, Function<CreativeTab, ItemStack> iconFactory, boolean iconCaching) {
        return new CreativeTab(unlocalizedName, iconFactory, iconCaching);
    }

    private CreativeTab(String unlocalizedName, Function<CreativeTab, ItemStack> iconFactory, boolean iconCaching) {
        super(unlocalizedName);
        this.iconFactory = iconFactory;
        this.iconCaching = iconCaching;
    }

    @Override
    public ItemStack createIcon() {
        return iconFactory.apply(this);
    }

    @Override
    public ItemStack getIcon() {
        if (this.iconCaching) {
            if (this.cachedIcon == null) {
                this.cachedIcon = this.createIcon();
            }
            return this.cachedIcon;
        } else {
            return this.createIcon();
        }
    }
}
