package ru.craftlogic.mixin.block;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(BlockMushroom.class)
public class MixinBlockMushroom extends BlockBush implements IShearable {
    public MixinBlockMushroom(Material material) {
        super(material);
    }

    @Override
    public SoundType getSoundType() {
        return SoundType.CLOTH;
    }

    /**
     * @author Radviger
     * @reason Only place mushrooms over soil blocks
     */
    @Overwrite
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        if (pos.getY() >= 0 && pos.getY() < 256) {
            IBlockState soil = world.getBlockState(pos.down());
            Block block = soil.getBlock();
            return block == Blocks.MYCELIUM
                || block == Blocks.DIRT && soil.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.PODZOL
                || block == Blocks.GRASS;
        }
        return false;
    }

    @Override
    public boolean isShearable(@Nonnull ItemStack tool, IBlockAccess blockAccessor, BlockPos pos) {
        return true;
    }

    @Nonnull
    @Override
    public List<ItemStack> onSheared(@Nonnull ItemStack tool, IBlockAccess blockAccessor, BlockPos pos, int i) {
        return Collections.singletonList(new ItemStack(Item.getItemFromBlock(this)));
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return null;
    }
}
