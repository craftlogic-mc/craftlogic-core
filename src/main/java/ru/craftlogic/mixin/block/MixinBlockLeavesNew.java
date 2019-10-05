package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockNewLeaf.class)
public class MixinBlockLeavesNew extends Block {
    public MixinBlockLeavesNew(Material material) {
        super(material);
    }

    /**
     * @author Radviger
     * @reason No apple drops from oak. Drop sticks instead
     */
    @Overwrite
    protected void dropApple(World world, BlockPos pos, IBlockState state, int chance) {
        if (world.rand.nextFloat() < 0.15F) {
            spawnAsEntity(world, pos, new ItemStack(Items.STICK));
        }
    }

    /**
     * @author Radviger
     * @reason Sorted CreativeTab items
     */
    @Overwrite
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {}
}
