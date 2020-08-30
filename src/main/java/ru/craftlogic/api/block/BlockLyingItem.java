package ru.craftlogic.api.block;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.craftlogic.api.world.Location;

import javax.annotation.Nullable;

public class BlockLyingItem extends BlockNarrow {
    private static final AxisAlignedBB BOUNDING = new AxisAlignedBB(2.0 / 16.0, 0.0, 2.0 / 16.0, 14.0 / 16.0, 1.0 / 16.0, 14.0 / 16.0);

    public BlockLyingItem(String name, float hardness, CreativeTabs tab) {
        super(Material.GROUND, name, hardness, tab);
    }

    @Override
    public void neighborChanged(Location selfLocation, Location neighborLocation, Block neighborBlock) {
        if (!selfLocation.offset(EnumFacing.DOWN).isSideSolid(EnumFacing.UP)) {
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

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(Location location) {
        return NULL_AABB;
    }

    protected double getMaxOffset() {
        return 0.5;
    }

    @Override
    public Vec3d getOffset(Location location) {
        EnumOffsetType offset = getOffsetType();

        if (offset == EnumOffsetType.NONE) {
            return new Vec3d(0, -0.5 + 0.5 / 16.0, 0);
        } else {
            long seed = MathHelper.getCoordinateRandom(location.getBlockX(), 0, location.getBlockZ());
            double maxOffset = getMaxOffset();
            return new Vec3d(((double) ((float) (seed >> 16 & 15L) / 15.0F) - 0.5D) * maxOffset, -0.5 + 0.5 / 16.0, ((double) ((float) (seed >> 24 & 15L) / 15.0F) - 0.5D) * maxOffset);
        }
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
}
