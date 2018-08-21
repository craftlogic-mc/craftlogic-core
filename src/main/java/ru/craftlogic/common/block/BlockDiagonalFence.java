package ru.craftlogic.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class BlockDiagonalFence extends BlockFence {
    public static final PropertyBool NORTH_WEST = PropertyBool.create("north_west");
    public static final PropertyBool NORTH_EAST = PropertyBool.create("north_east");
    public static final PropertyBool SOUTH_WEST = PropertyBool.create("south_west");
    public static final PropertyBool SOUTH_EAST = PropertyBool.create("south_east");

    private static final AxisAlignedBB[] BOUNDING_BOXES = new AxisAlignedBB[]{
        new AxisAlignedBB(0.375D, 0, 0.375D, 0.625D, 1, 0.625D), new AxisAlignedBB(0.375D, 0, 0.375D, 0.625D, 1, 1),
        new AxisAlignedBB(0, 0, 0.375D, 0.625D, 1, 0.625D), new AxisAlignedBB(0, 0, 0.375D, 0.625D, 1, 1),
        new AxisAlignedBB(0.375D, 0, 0, 0.625D, 1, 0.625D), new AxisAlignedBB(0.375D, 0, 0, 0.625D, 1, 1),
        new AxisAlignedBB(0, 0, 0, 0.625D, 1, 0.625D), new AxisAlignedBB(0, 0, 0, 0.625D, 1, 1),
        new AxisAlignedBB(0.375D, 0, 0.375D, 1, 1, 0.625D), new AxisAlignedBB(0.375D, 0, 0.375D, 1, 1, 1),
        new AxisAlignedBB(0, 0, 0.375D, 1, 1, 0.625D), new AxisAlignedBB(0, 0, 0.375D, 1, 1, 1),
        new AxisAlignedBB(0.375D, 0, 0, 1, 1, 0.625D), new AxisAlignedBB(0.375D, 0, 0, 1, 1, 1),
        new AxisAlignedBB(0, 0, 0, 1, 1, 0.625D), new AxisAlignedBB(0, 0, 0, 1, 1, 1)
    };
    private static final AxisAlignedBB[][] COLLISION_BOXES = new AxisAlignedBB[][]{
        {NORTH_AABB}, {SOUTH_AABB},
        {EAST_AABB}, {WEST_AABB},
        generateDiagonalCollisionBoxes(NORTH_EAST), generateDiagonalCollisionBoxes(NORTH_WEST),
        generateDiagonalCollisionBoxes(SOUTH_EAST), generateDiagonalCollisionBoxes(SOUTH_WEST)
    };

    public BlockDiagonalFence(Material material, MapColor color) {
        super(material, color);
        this.setDefaultState(this.blockState.getBaseState()
            .withProperty(NORTH, false).withProperty(SOUTH, false)
            .withProperty(WEST, false).withProperty(EAST, false)
            .withProperty(NORTH_EAST, false).withProperty(NORTH_WEST, false)
            .withProperty(SOUTH_EAST, false).withProperty(SOUTH_WEST, false)
        );
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        state = this.getActualState(state, blockAccessor, pos);
        boolean n = state.getValue(NORTH);
        boolean s = state.getValue(SOUTH);
        boolean e = state.getValue(EAST);
        boolean w = state.getValue(WEST);
        boolean ne = state.getValue(NORTH_EAST);
        boolean se = state.getValue(SOUTH_EAST);
        boolean sw = state.getValue(SOUTH_WEST);
        boolean nw = state.getValue(NORTH_WEST);
        int boundingIndex = 0;
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

        return BOUNDING_BOXES[boundingIndex];
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
            collisions.add(new AxisAlignedBB((double)startX, (double)minY, (double)startZ, (double)(startX + spanX), (double)maxY, (double)(startZ + spanZ)));
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
            if (actual.getValue(facing.getProperty())) {
                for (AxisAlignedBB eachBox : COLLISION_BOXES[facing.ordinal()]) {
                    addCollisionBoxToList(pos, mask, list, eachBox);
                }
            }
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        List<DiagonalFacing> acceptedFacings = new ArrayList<>();

        IBlockState blankState = this.getDefaultState();

        for (DiagonalFacing eachFacing : DiagonalFacing.values()) {
            if (!eachFacing.isIncompatible(acceptedFacings) && isConnectedWithoutConflictsTo(blockAccessor, pos, eachFacing)) {
                blankState = blankState.withProperty(eachFacing.getProperty(), true);
            }
        }

        return blankState;
    }

    public boolean isConnectedFrom(IBlockAccess world, BlockPos pos, PropertyBool direction) {
        DiagonalFacing from = DiagonalFacing.getFromProperty(direction);

        for (DiagonalFacing f : from.getIncomptibles()) {
            if (canPotentiallyConnectTo(world, pos, f)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        String id = this.getRegistryName().toString();
        if (id.startsWith("minecraft:") && id.endsWith("fence")) {
            if (this == Blocks.OAK_FENCE) {
                items.add(new ItemStack(Blocks.NETHER_BRICK_FENCE));
                items.add(new ItemStack(Blocks.OAK_FENCE));
                items.add(new ItemStack(Blocks.SPRUCE_FENCE));
                items.add(new ItemStack(Blocks.BIRCH_FENCE));
                items.add(new ItemStack(Blocks.JUNGLE_FENCE));
                items.add(new ItemStack(Blocks.ACACIA_FENCE));
                items.add(new ItemStack(Blocks.DARK_OAK_FENCE));
            }
        } else {
            items.add(new ItemStack(this));
        }
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
        return new BlockStateContainer(this, NORTH, SOUTH, WEST, EAST, NORTH_WEST, NORTH_EAST, SOUTH_WEST, SOUTH_EAST);
    }

    public static boolean isConnectedTo(IBlockAccess blockAccessor, BlockPos pos, DiagonalFacing facing) {
        if (facing.getRequiresOverhaul()) {
            Iterator<DiagonalFacing> var3 = facing.getIncomptibles().iterator();

            DiagonalFacing fromFacing;
            do {
                if (!var3.hasNext()) {
                    BlockPos targetPos = pos.add(facing.getOffset());
                    fromFacing = facing.getReverse();
                    Block block = blockAccessor.getBlockState(targetPos).getBlock();
                    if (block instanceof BlockDiagonalFence) {
                        return ((BlockDiagonalFence) block).isConnectedFrom(blockAccessor, targetPos, fromFacing.getProperty());
                    }

                    return false;
                }

                fromFacing = var3.next();
            } while (!canPotentiallyConnectTo(blockAccessor, pos, fromFacing));

            return false;
        } else {
            return canPotentiallyConnectTo(blockAccessor, pos, facing);
        }
    }

    public static boolean isConnectedWithoutConflictsTo(IBlockAccess world, BlockPos pos, DiagonalFacing facing) {
        if (facing.getRequiresOverhaul()) {
            BlockPos targetPos = pos.add(facing.getOffset());
            DiagonalFacing fromFacing = facing.getReverse();
            Block block = world.getBlockState(targetPos).getBlock();
            return block instanceof BlockDiagonalFence && ((BlockDiagonalFence) block).isConnectedFrom(world, targetPos, fromFacing.getProperty());
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
            if (facing.getRequiresOverhaul()) {
                if (block instanceof BlockDiagonalFence) {
                    return true;
                } else {
                    mat = block.getMaterial(state);
                    return mat.isOpaque() && block.isFullCube(state) && mat != Material.GOURD;
                }
            } else if (!(block instanceof BlockDiagonalFence) && !(block instanceof BlockFence) && !(block instanceof BlockFenceGate)) {
                mat = block.getMaterial(state);
                return mat.isOpaque() && block.isFullCube(state) && mat != Material.GOURD;
            } else {
                return true;
            }
        }
    }

    public enum DiagonalFacing {
        North(NORTH, false, new Vec3i(0, 0, -1), new double[]{0.0D}),
        South(SOUTH, North, false, new Vec3i(0, 0, 1), new double[]{0.5D}),
        East(EAST, false, new Vec3i(1, 0, 0), new double[]{0.25D}),
        West(WEST, East, false, new Vec3i(-1, 0, 0), new double[]{0.75D}),
        NorthEast(NORTH_EAST, true, new Vec3i(1, 0, -1), new double[]{0.125D}, North, East),
        NorthWest(NORTH_WEST, true, new Vec3i(-1, 0, -1), new double[]{0.875D}, North, West),
        SouthEast(SOUTH_EAST, NorthWest, true, new Vec3i(1, 0, 1), new double[]{0.375D}, South, East),
        SouthWest(SOUTH_WEST, NorthEast, true, new Vec3i(-1, 0, 1), new double[]{0.625D}, South, West);

        private DiagonalFacing reverse;
        private final PropertyBool property;
        private final Vec3i offset;
        private final boolean requiresOverhaul;
        private final List<DiagonalFacing> incompatibleFacings;
        private final double[] rotationsFromStandardNorth;
        private static Map<PropertyBool, DiagonalFacing> propToEnum;

        DiagonalFacing(PropertyBool property, DiagonalFacing reverse, boolean requiresOverhaul, Vec3i offset, double[] rotationsFromStandardNorth, DiagonalFacing... incompatiblePrevious) {
            this(property, requiresOverhaul, offset, rotationsFromStandardNorth, incompatiblePrevious);
            this.reverse = reverse;
            reverse.reverse = this;
        }

        DiagonalFacing(PropertyBool property, boolean requiresOverhaul, Vec3i offset, double[] rotationsFromStandardNorth, DiagonalFacing... incompatiblePrevious) {
            this.property = property;
            this.incompatibleFacings = Collections.unmodifiableList(Arrays.asList(incompatiblePrevious));
            this.requiresOverhaul = requiresOverhaul;
            this.offset = offset;
            this.rotationsFromStandardNorth = rotationsFromStandardNorth;
            addToPropMap(property, this);
        }

        public boolean isIncompatible(List<DiagonalFacing> currentFacings) {
            if (!currentFacings.isEmpty() && !this.incompatibleFacings.isEmpty()) {
                return !Collections.disjoint(currentFacings, this.incompatibleFacings);
            } else {
                return false;
            }
        }

        public boolean getRequiresOverhaul() {
            return this.requiresOverhaul;
        }

        public DiagonalFacing getReverse() {
            return this.reverse;
        }

        public PropertyBool getProperty() {
            return this.property;
        }

        public List<DiagonalFacing> getIncomptibles() {
            return this.incompatibleFacings;
        }

        public Vec3i getOffset() {
            return this.offset;
        }

        private static void addToPropMap(PropertyBool prop, DiagonalFacing enumelem) {
            if (propToEnum == null) {
                propToEnum = new HashMap<>();
            }

            propToEnum.put(prop, enumelem);
        }

        public static DiagonalFacing getFromProperty(PropertyBool prop) {
            return propToEnum.get(prop);
        }

        public double[] getRotationsFromStandardNorth() {
            return this.rotationsFromStandardNorth.clone();
        }
    }
}
