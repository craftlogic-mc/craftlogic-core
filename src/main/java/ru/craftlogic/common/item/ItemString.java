package ru.craftlogic.common.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import ru.craftlogic.api.block.Colored;
import ru.craftlogic.api.item.ItemBase;
import ru.craftlogic.api.model.ModelManager;

public class ItemString extends ItemBase implements Colored {
    public ItemString() {
        super("string", CreativeTabs.MATERIALS);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int meta) {
        return meta;
    }

    @Override
    public int getItemColor(ItemStack stack, int tint) {
        return EnumDyeColor.byMetadata(stack.getMetadata()).getColorValue();
    }

    @Override
    public void registerModel(ModelManager modelManager) {
        modelManager.registerCustomMeshDefinition(this, item ->
            new ModelResourceLocation("minecraft:string", "inventory")
        );
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            for (int i = 0; i < 16; i++) {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }
}
