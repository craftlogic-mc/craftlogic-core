package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.CraftBlocks;

@Mixin(BlockOldLeaf.class)
public class MixinBlockLeavesOld extends Block {
    public MixinBlockLeavesOld(Material material) {
        super(material);
    }

    /**
     * @author Radviger
     * @reason No apple drops from oak. Drop sticks instead
     */
    @Overwrite
    protected void dropApple(World world, BlockPos pos, IBlockState state, int chance) {
        if (CraftConfig.tweaks.enableSticksFromLeaf && world.rand.nextFloat() < 0.15F) {
            spawnAsEntity(world, pos, new ItemStack(Items.STICK));
        }
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
        items.add(new ItemStack(CraftBlocks.LEAVES3, 1, 0));
        items.add(new ItemStack(CraftBlocks.LEAVES3, 1, 1));
    }
}
