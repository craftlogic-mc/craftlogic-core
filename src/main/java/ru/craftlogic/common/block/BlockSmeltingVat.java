package ru.craftlogic.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import ru.craftlogic.api.block.BlockBase;
import ru.craftlogic.api.block.holders.TileEntityHolder;
import ru.craftlogic.api.util.TileEntityInfo;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.common.CraftBlocks;
import ru.craftlogic.common.tileentity.TileEntitySmeltingVat;

public class BlockSmeltingVat extends BlockBase implements TileEntityHolder<TileEntitySmeltingVat> {
    public BlockSmeltingVat() {
        super(Material.ROCK, "smelting_vat", 1.5F, CreativeTabs.DECORATIONS);
    }

    @Override
    protected AxisAlignedBB getBoundingBox(Location location) {
        return BlockUnfiredPottery.VAT_BOUNDING;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isTopSolid(IBlockState state) {
        return false;
    }

    @Override
    public boolean canPlaceBlockAt(Location location) {
        return location.offset(EnumFacing.DOWN).isSameBlock(CraftBlocks.FURNACE);
    }

    @Override
    public TileEntityInfo<TileEntitySmeltingVat> getTileEntityInfo(IBlockState state) {
        return new TileEntityInfo<>(TileEntitySmeltingVat.class, state, TileEntitySmeltingVat::new);
    }
}
