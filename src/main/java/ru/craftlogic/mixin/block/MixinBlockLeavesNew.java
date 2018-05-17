package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockNewLeaf.class)
public class MixinBlockLeavesNew extends Block {
    public MixinBlockLeavesNew(Material material) {
        super(material);
    }

    @Overwrite
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {}
}
