package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockOldLeaf.class)
public class MixinBlockLeavesOld extends Block {
    public MixinBlockLeavesOld(Material material) {
        super(material);
    }

    /**
     * @author Radviger
     * @reason Sorted CreativeTab items
     */
    @Overwrite
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        items.add(new ItemStack(Blocks.LEAVES, 1, 0));
        items.add(new ItemStack(Blocks.LEAVES, 1, 1));
        items.add(new ItemStack(Blocks.LEAVES, 1, 2));
        items.add(new ItemStack(Blocks.LEAVES, 1, 3));
        items.add(new ItemStack(Blocks.LEAVES2, 1, 0));
        items.add(new ItemStack(Blocks.LEAVES2, 1, 1));
    }
}
