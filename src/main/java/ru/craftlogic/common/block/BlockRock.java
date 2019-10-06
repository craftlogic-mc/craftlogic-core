package ru.craftlogic.common.block;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import ru.craftlogic.api.block.BlockNarrow;
import ru.craftlogic.api.util.Nameable;
import ru.craftlogic.api.world.Location;

import javax.annotation.Nullable;

public class BlockRock extends BlockNarrow {
    private static final AxisAlignedBB BOUNDING = new AxisAlignedBB(2.0 / 16.0, 0.0, 2.0 / 16.0, 14.0 / 16.0, 4.0 / 16.0, 14.0 / 16.0);
    public static final PropertyEnum<Type> VARIANT = PropertyEnum.create("variant", Type.class);

    public BlockRock() {
        super(Material.GROUND, "rock", 0F, CreativeTabs.DECORATIONS);
        this.setSoundType(SoundType.STONE);
        this.setDefaultState(getBlockState().getBaseState().withProperty(VARIANT, Type.PLAIN));
    }

    @Override
    public void neighborChanged(Location selfLocation, Location neighborLocation, Block neighborBlock) {
        if (!canPlaceBlockAt(selfLocation)) {
            selfLocation.setBlockToAir(true);
        }
    }

    @Override
    protected boolean canPlaceBlockAt(Location location) {
        Location ground = location.offset(EnumFacing.DOWN);
        Material groundMaterial = ground.getBlockMaterial();
        return (groundMaterial == Material.GRASS || groundMaterial == Material.SAND || ground.getBlock() instanceof BlockDirt
            || ground.getBlock() instanceof BlockStone || ground.getBlock() instanceof BlockGravel)
            && ground.isSideSolid(EnumFacing.UP) && (location.isAir() || location.getBlock() instanceof BlockSnow);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(Location location) {
        return NULL_AABB;
    }

    @Override
    public EnumOffsetType getOffsetType() {
        return EnumOffsetType.XZ;
    }

    @Override
    protected AxisAlignedBB getBoundingBox(Location location) {
        return BOUNDING;
    }

    @Override
    public boolean isPassable(Location location) {
        return true;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(VARIANT, Type.values()[meta & 3]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    @Override
    protected IProperty[] getProperties() {
        return new IProperty[]{VARIANT};
    }

    public enum Type implements Nameable {
        PLAIN, SNOWY, SANDY
    }
}
