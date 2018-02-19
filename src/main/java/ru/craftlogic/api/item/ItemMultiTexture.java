package ru.craftlogic.api.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.client.ModelManager;

public class ItemMultiTexture extends ItemBase {
    private final String prefix;
    private final String[] names;

    public ItemMultiTexture(String prefix, CreativeTabs tab, String... names) {
        super(prefix.replace("/", "_"), tab);
        this.prefix = prefix;
        this.names = names;
        this.setHasSubtypes(true);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        if (stack.getMetadata() < this.names.length) {
            return ("item." + this.prefix + "." + this.names[stack.getMetadata()]).replaceAll("/", ".");
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        for (int i = 0; i < this.names.length; i++) {
            modelManager.registerItemModel(this, i, this.modid + ":" + this.prefix + "/" + this.names[i]);
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            for (int i = 0; i < this.names.length; i++) {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }
}
