package ru.craftlogic.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import ru.craftlogic.api.block.BlockBase;
import ru.craftlogic.api.block.holders.TileEntityHolder;
import ru.craftlogic.api.util.Nameable;
import ru.craftlogic.api.util.TileEntityInfo;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.common.tileentity.TileEntityChimney;

public class BlockChimney extends BlockBase implements TileEntityHolder<TileEntityChimney> {
    public static final PropertyDirection FACING = PropertyDirection.create("facing", f -> f != EnumFacing.UP);
    public static final PropertyEnum<ChimneyType> VARIANT = PropertyEnum.create("variant", ChimneyType.class);

    public BlockChimney() {
        super(Material.ROCK, "chimney", 1.5F, null);
        this.setDefaultState(this.blockState.getBaseState()
            .withProperty(FACING, EnumFacing.DOWN)
            .withProperty(VARIANT, ChimneyType.STONE)
        );
    }

    @Override
    public IBlockState getStateForPlacement(Location location, RayTraceResult target, int meta, EntityLivingBase placer) {
        EnumFacing facing = target.sideHit.getOpposite();
        if (facing == EnumFacing.UP) {
            facing = EnumFacing.DOWN;
        }

        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public TileEntityInfo<TileEntityChimney> getTileEntityInfo(IBlockState state) {
        return new TileEntityInfo<>(TileEntityChimney.class, state, TileEntityChimney::new);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        ChimneyType type = ChimneyType.values()[meta / 5];
        int f = meta % 5;
        EnumFacing facing = EnumFacing.getFront(f == 0 ? 0 : f + 1);
        return this.getDefaultState().withProperty(VARIANT, type).withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int f = state.getValue(FACING).getIndex();
        return (f == 0 ? 0 : f - 1) + 5 * state.getValue(VARIANT).ordinal();
    }

    @Override
    protected BlockFaceShape getBlockFaceShape(Location location, EnumFacing side) {
        EnumFacing facing = location.getBlockProperty(FACING);
        return side == EnumFacing.UP || side == facing ? BlockFaceShape.BOWL : BlockFaceShape.SOLID;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, VARIANT);
    }

    public enum ChimneyType implements Nameable {
        STONE,
        BRICK
    }
}
