package ru.craftlogic.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.craftlogic.api.block.DiagonalFacing;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockDiagonalWall extends BlockWall {
    public static final PropertyBool NORTH_WEST = PropertyBool.create("north_west");
    public static final PropertyBool NORTH_EAST = PropertyBool.create("north_east");
    public static final PropertyBool SOUTH_WEST = PropertyBool.create("south_west");
    public static final PropertyBool SOUTH_EAST = PropertyBool.create("south_east");

    private static final AxisAlignedBB[] BOUNDING_BOXES = new AxisAlignedBB[]{
        new AxisAlignedBB(0.25, 0, 0.25, 0.75, 1, 0.75), new AxisAlignedBB(0.25, 0, 0.25, 0.75, 1, 1),
        new AxisAlignedBB(0, 0, 0.25, 0.75, 1, 0.75), new AxisAlignedBB(0, 0, 0.25, 0.75, 1, 1),
        new AxisAlignedBB(0.25, 0, 0, 0.75, 1, 0.75), new AxisAlignedBB(0.25, 0, 0, 0.75, 1, 1),
        new AxisAlignedBB(0, 0, 0, 0.75, 1, 0.75), new AxisAlignedBB(0, 0, 0, 0.75, 1, 1),
        new AxisAlignedBB(0.25, 0, 0.25, 1, 1, 0.75), new AxisAlignedBB(0.25, 0, 0.25, 1, 1, 1),
        new AxisAlignedBB(0, 0, 0.25, 1, 1, 0.75), new AxisAlignedBB(0, 0, 0.25, 1, 1, 1),
        new AxisAlignedBB(0.25, 0, 0, 1, 1, 0.75), new AxisAlignedBB(0.25, 0, 0, 1, 1, 1),
        new AxisAlignedBB(0, 0, 0, 1, 1, 0.75), new AxisAlignedBB(0, 0, 0, 1, 1, 1)
    };
    private static final AxisAlignedBB PILLAR_AABB = new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 1.5, 0.75);
    private static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.25, 0.0, 0.75, 0.75, 1.5, 1.0);
    private static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.0, 0.0, 0.25, 0.25, 1.5, 0.75);
    private static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.25, 0.0, 0.0, 0.75, 1.5, 0.25);
    private static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.75, 0.0, 0.25, 1.0, 1.5, 0.75);
    private static final AxisAlignedBB[][] COLLISION_BOXES = new AxisAlignedBB[][]{
        {NORTH_AABB}, {SOUTH_AABB},
        {EAST_AABB}, {WEST_AABB},
        generateDiagonalCollisionBoxes(NORTH_EAST), generateDiagonalCollisionBoxes(NORTH_WEST),
        generateDiagonalCollisionBoxes(SOUTH_EAST), generateDiagonalCollisionBoxes(SOUTH_WEST)
    };
    private static final PropertyBool[] PROPERTIES = {
        NORTH, SOUTH, EAST, WEST, NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST
    };

    public BlockDiagonalWall(Block block) {
        super(block);
        this.setTranslationKey("cobbleWall");
        this.setDefaultState(this.blockState.getBaseState()
            .withProperty(VARIANT, EnumType.NORMAL)
            .withProperty(UP, false)
            .withProperty(NORTH, false).withProperty(SOUTH, false)
            .withProperty(WEST, false).withProperty(EAST, false)
            .withProperty(NORTH_EAST, false).withProperty(NORTH_WEST, false)
            .withProperty(SOUTH_EAST, false).withProperty(SOUTH_WEST, false)
        );
    }

    private static int getAABBIndex(IBlockState state) {
        int boundingIndex = 0;

        boolean n = state.getValue(NORTH);
        boolean s = state.getValue(SOUTH);
        boolean e = state.getValue(EAST);
        boolean w = state.getValue(WEST);
        boolean ne = state.getValue(NORTH_EAST);
        boolean se = state.getValue(SOUTH_EAST);
        boolean sw = state.getValue(SOUTH_WEST);
        boolean nw = state.getValue(NORTH_WEST);

        if (n || ne || nw) {
            boundingIndex |= 1 << EnumFacing.NORTH.getHorizontalIndex();
        }

        if (e || ne || se) {
            boundingIndex |= 1 << EnumFacing.EAST.getHorizontalIndex();
        }

        if (s || se || sw) {
            boundingIndex |= 1 << EnumFacing.SOUTH.getHorizontalIndex();
        }

        if (w || nw || sw) {
            boundingIndex |= 1 << EnumFacing.WEST.getHorizontalIndex();
        }
        return boundingIndex;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        state = this.getActualState(state, blockAccessor, pos);
        return BOUNDING_BOXES[getAABBIndex(state)];
    }

    private static AxisAlignedBB[] generateDiagonalCollisionBoxes(PropertyBool diagonal) {
        float squareRadius = 0.125F;
        float minY = 0.0F;
        float maxY = 1.5F;
        List<AxisAlignedBB> collisions = new ArrayList<>();
        DiagonalFacing facing = DiagonalFacing.getFromProperty(diagonal);
        Vec3i vec = facing.getOffset();
        int x = vec.getX();
        float startX = 0.5F + 0.5F * (float)x;
        float addX = (float)(-x) * squareRadius;
        float spanX = (float)x * squareRadius * 2.0F;
        int z = vec.getZ();
        float startZ = 0.5F + 0.5F * (float)z;
        float addZ = (float)(-z) * squareRadius;
        float spanZ = (float)z * squareRadius * 2.0F;

        for(int walk = 0; walk < 6; ++walk) {
            collisions.add(new AxisAlignedBB(startX, minY, startZ, startX + spanX, maxY, startZ + spanZ));
            startX += addX;
            startZ += addZ;
        }

        return collisions.toArray(new AxisAlignedBB[0]);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, @Nullable Entity entity, boolean p_185477_7_) {
        IBlockState actual = this.getActualState(state, world, pos);
        addCollisionBoxToList(pos, mask, list, PILLAR_AABB);

        for (DiagonalFacing facing : DiagonalFacing.values()) {
            if (actual.getValue(PROPERTIES[facing.ordinal()])) {
                for (AxisAlignedBB eachBox : COLLISION_BOXES[facing.ordinal()]) {
                    addCollisionBoxToList(pos, mask, list, eachBox);
                }
            }
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        List<DiagonalFacing> acceptedFacings = new ArrayList<>();
        List<DiagonalFacing> connectedFacings = new ArrayList<>();

        IBlockState blankState = this.getDefaultState().withProperty(VARIANT, state.getValue(VARIANT));

        for (DiagonalFacing facing : DiagonalFacing.values()) {
            if (!facing.isIncompatible(acceptedFacings) && isConnectedWithoutConflictsTo(blockAccessor, pos, facing)) {
                blankState = blankState.withProperty(PROPERTIES[facing.ordinal()], true);
                connectedFacings.add(facing);
            }
        }

        boolean up = true;

        if (connectedFacings.size() == 2) {
            up = connectedFacings.get(0).getOpposite() != connectedFacings.get(1) || connectedFacings.get(0).isDiagonal();
        }

        IBlockState downBlock = blockAccessor.getBlockState(pos.down());

        return blankState.withProperty(UP, up || downBlock.getBlock() instanceof BlockWall && downBlock.getValue(UP));
    }

    public boolean isConnectedFrom(IBlockAccess world, BlockPos pos, PropertyBool direction) {
        DiagonalFacing from = DiagonalFacing.getFromProperty(direction);

        for (DiagonalFacing f : from.getIncompatibles()) {
            if (canPotentiallyConnectTo(world, pos, f)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_90:
                return state.withProperty(NORTH, state.getValue(EAST)).withProperty(EAST, state.getValue(SOUTH)).withProperty(SOUTH, state.getValue(WEST)).withProperty(WEST, state.getValue(NORTH)).withProperty(NORTH_EAST, state.getValue(SOUTH_EAST)).withProperty(SOUTH_EAST, state.getValue(SOUTH_WEST)).withProperty(SOUTH_WEST, state.getValue(NORTH_WEST)).withProperty(NORTH_WEST, state.getValue(NORTH_EAST));
            case CLOCKWISE_180:
                return state.withProperty(NORTH, state.getValue(SOUTH)).withProperty(EAST, state.getValue(WEST)).withProperty(SOUTH, state.getValue(NORTH)).withProperty(WEST, state.getValue(EAST)).withProperty(NORTH_EAST, state.getValue(SOUTH_WEST)).withProperty(SOUTH_EAST, state.getValue(NORTH_WEST)).withProperty(SOUTH_WEST, state.getValue(NORTH_EAST)).withProperty(NORTH_WEST, state.getValue(SOUTH_EAST));
            case COUNTERCLOCKWISE_90:
                return state.withProperty(NORTH, state.getValue(WEST)).withProperty(EAST, state.getValue(NORTH)).withProperty(SOUTH, state.getValue(EAST)).withProperty(WEST, state.getValue(SOUTH)).withProperty(NORTH_EAST, state.getValue(NORTH_WEST)).withProperty(SOUTH_EAST, state.getValue(NORTH_EAST)).withProperty(SOUTH_WEST, state.getValue(SOUTH_EAST)).withProperty(NORTH_WEST, state.getValue(SOUTH_WEST));
        }
        return state;
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT:
                return state.withProperty(EAST, state.getValue(WEST)).withProperty(WEST, state.getValue(EAST)).withProperty(NORTH_EAST, state.getValue(NORTH_WEST)).withProperty(NORTH_WEST, state.getValue(NORTH_EAST)).withProperty(SOUTH_EAST, state.getValue(SOUTH_WEST)).withProperty(SOUTH_WEST, state.getValue(SOUTH_EAST));
            case FRONT_BACK:
                return state.withProperty(NORTH, state.getValue(SOUTH)).withProperty(SOUTH, state.getValue(NORTH)).withProperty(NORTH_EAST, state.getValue(SOUTH_EAST)).withProperty(SOUTH_EAST, state.getValue(NORTH_EAST)).withProperty(NORTH_WEST, state.getValue(SOUTH_WEST)).withProperty(SOUTH_WEST, state.getValue(NORTH_WEST));
        }
        return state;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT, UP, NORTH, SOUTH, WEST, EAST, NORTH_WEST, NORTH_EAST, SOUTH_WEST, SOUTH_EAST);
    }

    public static boolean isConnectedTo(IBlockAccess blockAccessor, BlockPos pos, DiagonalFacing facing) {
        if (facing.isDiagonal()) {
            for (DiagonalFacing inc : facing.getIncompatibles()) {
                if (canPotentiallyConnectTo(blockAccessor, pos, inc)) {
                    return false;
                }
            }

            BlockPos targetPos = pos.add(facing.getOffset());
            DiagonalFacing fromFacing = facing.getOpposite();
            Block block = blockAccessor.getBlockState(targetPos).getBlock();
            if (block instanceof BlockDiagonalWall) {
                return ((BlockDiagonalWall) block).isConnectedFrom(blockAccessor, targetPos, PROPERTIES[fromFacing.ordinal()]);
            }

            return false;
        } else {
            return canPotentiallyConnectTo(blockAccessor, pos, facing);
        }
    }

    public static boolean isConnectedWithoutConflictsTo(IBlockAccess world, BlockPos pos, DiagonalFacing facing) {
        if (facing.isDiagonal()) {
            BlockPos targetPos = pos.add(facing.getOffset());
            DiagonalFacing fromFacing = facing.getOpposite();
            Block block = world.getBlockState(targetPos).getBlock();
            return block instanceof BlockDiagonalWall && ((BlockDiagonalWall) block).isConnectedFrom(world, targetPos, PROPERTIES[fromFacing.ordinal()]);
        } else {
            return canPotentiallyConnectTo(world, pos, facing);
        }
    }

    public static boolean canPotentiallyConnectTo(IBlockAccess world, BlockPos ownPos, DiagonalFacing facing) {
        BlockPos pos = ownPos.add(facing.getOffset());
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block == Blocks.BARRIER) {
            return false;
        } else {
            Material mat;
            if (facing.isDiagonal()) {
                if (block instanceof BlockDiagonalWall) {
                    return true;
                } else {
                    mat = block.getMaterial(state);
                    return mat.isOpaque() && block.isFullCube(state) && mat != Material.GOURD;
                }
            } else if (block instanceof BlockWall || block instanceof BlockFenceGate) {
                return true;
            } else {
                mat = block.getMaterial(state);
                return mat.isOpaque() && block.isFullCube(state) && mat != Material.GOURD;
            }
        }
    }
}
