package ru.craftlogic.api.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.EnumFacing;
import ru.craftlogic.api.world.Location;

public class BlockNarrow extends BlockBase {
    public BlockNarrow(Material material, String name, float hardness, CreativeTabs tab) {
        super(material, name, hardness, tab);
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isTopSolid(IBlockState state) {
        return false;
    }

    @Override
    protected BlockFaceShape getBlockFaceShape(Location location, EnumFacing side) {
        return BlockFaceShape.UNDEFINED;
    }
}
