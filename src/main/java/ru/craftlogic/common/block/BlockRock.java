package ru.craftlogic.common.block;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import ru.craftlogic.api.block.BlockNarrow;
import ru.craftlogic.api.world.Location;

import javax.annotation.Nullable;

public class BlockRock extends BlockNarrow {
    private static final AxisAlignedBB BOUNDING = new AxisAlignedBB(2.0 / 16.0, 0.0, 2.0 / 16.0, 14.0 / 16.0, 4.0 / 16.0, 14.0 / 16.0);

    public BlockRock() {
        super(Material.GROUND, "rock", 0F, CreativeTabs.DECORATIONS);
        this.setSoundType(SoundType.STONE);
    }

    @Override
    protected boolean canPlaceBlockAt(Location location) {
        Location ground = location.offset(EnumFacing.DOWN);
        Material groundMaterial = ground.getBlockMaterial();
        return (groundMaterial == Material.GRASS || ground.getBlock() instanceof BlockDirt
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
}
