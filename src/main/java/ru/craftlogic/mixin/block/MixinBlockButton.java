package ru.craftlogic.mixin.block;

import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockButtonWood;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockButton.class)
public class MixinBlockButton extends BlockDirectional {
    public MixinBlockButton(Material material) {
        super(material);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        String id = this.getRegistryName().toString();
        if (id.startsWith("minecraft:") && id.endsWith("button")) {
            if (this == Blocks.WOODEN_BUTTON) {
                items.add(new ItemStack(Blocks.STONE_BUTTON));
                items.add(new ItemStack(Blocks.WOODEN_BUTTON));
            }
        } else {
            items.add(new ItemStack(this));
        }
    }

    @Override
    public Material getMaterial(IBlockState state) {
        return (Object) this instanceof BlockButtonWood ? Material.WOOD : super.getMaterial(state);
    }
}
