package ru.craftlogic.common.item;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import ru.craftlogic.api.block.Colored;
import ru.craftlogic.api.model.ModelAutoReg;
import ru.craftlogic.api.model.ModelManager;

public class ItemWool extends ItemBlock implements Colored, ModelAutoReg {
    public ItemWool(Block block) {
        super(block);
        this.setUnlocalizedName("wool");
        this.setCreativeTab(CreativeTabs.MATERIALS);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public String getUnlocalizedName(ItemStack item) {
        return "item.wool";
    }

    @Override
    public String getUnlocalizedName() {
        return "item.wool";
    }

    @Override
    public int getMetadata(int meta) {
        return meta;
    }

    @Override
    public CreativeTabs getCreativeTab() {
        return CreativeTabs.MATERIALS;
    }

    @Override
    public int getItemColor(ItemStack stack, int tint) {
        return EnumDyeColor.byMetadata(stack.getMetadata()).getColorValue();
    }

    @Override
    public void registerModel(ModelManager modelManager) {
        modelManager.registerItemVariants(this, "minecraft:wool");
        modelManager.registerCustomMeshDefinition(this, item ->
            new ModelResourceLocation("wool", "inventory")
        );
    }
}
