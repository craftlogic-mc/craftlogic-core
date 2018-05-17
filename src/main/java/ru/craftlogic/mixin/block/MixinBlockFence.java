package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockFence.class)
public class MixinBlockFence extends Block {
    public MixinBlockFence(Material material) {
        super(material);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        String id = this.getRegistryName().toString();
        if (id.startsWith("minecraft:") && id.endsWith("fence")) {
            if (this == Blocks.OAK_FENCE) {
                items.add(new ItemStack(Blocks.NETHER_BRICK_FENCE));
                items.add(new ItemStack(Blocks.OAK_FENCE));
                items.add(new ItemStack(Blocks.SPRUCE_FENCE));
                items.add(new ItemStack(Blocks.BIRCH_FENCE));
                items.add(new ItemStack(Blocks.JUNGLE_FENCE));
                items.add(new ItemStack(Blocks.ACACIA_FENCE));
                items.add(new ItemStack(Blocks.DARK_OAK_FENCE));
            }
        } else {
            items.add(new ItemStack(this));
        }
    }
}
