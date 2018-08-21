package ru.craftlogic.common.block;

import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import ru.craftlogic.api.block.Growable;
import ru.craftlogic.api.world.Location;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockGourdStem extends BlockBush implements Growable {
    private static final PropertyInteger AGE = BlockStem.AGE;
    private static final PropertyBool NORTH = PropertyBool.create("north");
    private static final PropertyBool SOUTH = PropertyBool.create("south");
    private static final PropertyBool WEST = PropertyBool.create("west");
    private static final PropertyBool EAST = PropertyBool.create("east");
    private static final AxisAlignedBB[] BOUNDS = new AxisAlignedBB[]{
        new AxisAlignedBB(0.375, 0, 0.375, 0.625, 0.125, 0.625),
        new AxisAlignedBB(0.375, 0, 0.375, 0.625, 0.25, 0.625),
        new AxisAlignedBB(0.375, 0, 0.375, 0.625, 0.375, 0.625),
        new AxisAlignedBB(0.375, 0, 0.375, 0.625, 0.5, 0.625),
        new AxisAlignedBB(0.375, 0, 0.375, 0.625, 0.625, 0.625),
        new AxisAlignedBB(0.375, 0, 0.375, 0.625, 0.75, 0.625),
        new AxisAlignedBB(0.375, 0, 0.375, 0.625, 0.875, 0.625),
        new AxisAlignedBB(0.375, 0, 0.375, 0.625, 1, 0.625)
    };
    private BlockGourd.GourdVariant variant;

    public BlockGourdStem(BlockGourd.GourdVariant variant) {
        this.variant = variant;
        this.setDefaultState(this.blockState.getBaseState()
            .withProperty(AGE, 0)
            .withProperty(NORTH, false)
            .withProperty(SOUTH, false)
            .withProperty(WEST, false)
            .withProperty(EAST, false)
        );
        this.setTickRandomly(true);
        this.setSoundType(SoundType.WOOD);
        this.setCreativeTab(null);
    }

    private BlockGourd getCrop() {
        return this.variant.crop.get();
    }

    protected static float getGrowthChance(Block block, World world, BlockPos pos) {
        float f = 1F;
        BlockPos soilPos = pos.down();

        for(int dx = -1; dx <= 1; ++dx) {
            for(int dy = -1; dy <= 1; ++dy) {
                float f1 = 0F;
                IBlockState s = world.getBlockState(soilPos.add(dx, 0, dy));
                if (s.isSideSolid(world, soilPos.add(dx, 0, dy), EnumFacing.UP)) {
                    f1 = 1F;
                    if (s.getBlock().isFertile(world, soilPos.add(dx, 0, dy))) {
                        f1 = 3F;
                    }
                }

                if (dx != 0 || dy != 0) {
                    f1 /= 4F;
                }

                f += f1;
            }
        }

        BlockPos northPos = pos.north();
        BlockPos southPos = pos.south();
        BlockPos westPos = pos.west();
        BlockPos eastPos = pos.east();
        boolean xLine = block == world.getBlockState(westPos).getBlock() || block == world.getBlockState(eastPos).getBlock();
        boolean zLine = block == world.getBlockState(northPos).getBlock() || block == world.getBlockState(southPos).getBlock();
        if (xLine && zLine) {
            f /= 2F;
        } else {
            boolean flag2 = block == world.getBlockState(westPos.north()).getBlock() || block == world.getBlockState(eastPos.north()).getBlock() || block == world.getBlockState(eastPos.south()).getBlock() || block == world.getBlockState(westPos.south()).getBlock();
            if (flag2) {
                f /= 2F;
            }
        }

        return f;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(world, pos, state, rand);
        if (world.isAreaLoaded(pos, 1)) {
            if (world.getLightFromNeighbors(pos.up()) >= 9) {
                float f = getGrowthChance(this, world, pos);
                if (ForgeHooks.onCropsGrowPre(world, pos, state, rand.nextInt((int)(25F / f) + 1) == 0)) {
                    int i = state.getValue(AGE);
                    IBlockState soil;
                    if (i < 7) {
                        soil = state.withProperty(AGE, i + 1);
                        world.setBlockState(pos, soil, 2);
                    } else {
                        EnumFacing s = EnumFacing.Plane.HORIZONTAL.random(rand);
                        pos = pos.offset(s);
                        soil = world.getBlockState(pos.down());
                        Block block = soil.getBlock();
                        if (world.isAirBlock(pos) && (block.canSustainPlant(soil, world, pos.down(), EnumFacing.UP, this) || block == Blocks.DIRT || block == Blocks.GRASS)) {
                            world.setBlockState(pos, this.getCrop().getDefaultState().withProperty(BlockHorizontal.FACING, s.getOpposite()));
                        }
                    }

                    ForgeHooks.onCropsGrowPost(world, pos, state, world.getBlockState(pos));
                }
            }
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        state = this.getActualState(state, blockAccessor, pos);
        boolean up = !state.getValue(NORTH) && !state.getValue(SOUTH) && !state.getValue(WEST) && !state.getValue(EAST);
        return BOUNDS[up ? state.getValue(AGE) : 3];
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        int age = state.getValue(AGE);

        for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
            BlockPos offsetPos = pos.offset(side);
            IBlockState offsetState = blockAccessor.getBlockState(offsetPos);
            if (age == 7 && this.canConnectTo(side, offsetState)) {
                switch (side) {
                    case NORTH:
                        state = state.withProperty(NORTH, true);
                        break;
                    case SOUTH:
                        state = state.withProperty(SOUTH, true);
                        break;
                    case WEST:
                        state = state.withProperty(WEST, true);
                        break;
                    case EAST:
                        state = state.withProperty(EAST, true);
                        break;
                }
            }
        }

        return state;
    }

    private boolean canConnectTo(EnumFacing facing, IBlockState other) {
        EnumFacing oppositeFacing = facing.getOpposite();
        return other.getBlock() == this.getCrop() && other.getValue(BlockGourd.FACING) == oppositeFacing
                || (other.getBlock() == Blocks.MELON_BLOCK  && this.variant == BlockGourd.GourdVariant.MELON
                    || other.getBlock() == Blocks.PUMPKIN && this.variant == BlockGourd.GourdVariant.PUMPKIN)
                && other.getValue(BlockPumpkin.FACING) == facing;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(this.variant.seed.get());
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return state.getBlock() == Blocks.FARMLAND;
    }

    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        super.dropBlockAsItemWithChance(world, pos, state, chance, fortune);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> items, IBlockAccess blockAccessor, BlockPos pos, IBlockState state, int fortune) {
        Item item = this.getSeedItem();
        if (item != null) {
            int i = state.getValue(AGE);

            for(int j = 0; j < 3; ++j) {
                if (RANDOM.nextInt(15) <= i) {
                    items.add(new ItemStack(item));
                }
            }
        }

    }

    @Nullable
    protected Item getSeedItem() {
        return this.variant.seed.get();
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        Item item = this.getSeedItem();
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    @Override
    public boolean canGrow(Location location, boolean remoteWorld) {
        return location.getBlockProperty(AGE) != 7;
    }

    @Override
    public boolean canUseBonemeal(Location location, Random random) {
        return true;
    }

    @Override
    public void grow(Location location, Random random) {
        int i = location.getBlockProperty(AGE) + MathHelper.getInt(random, 2, 5);
        location.setBlockProperty(AGE, Math.min(7, i), 2);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(AGE, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AGE);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE, NORTH, SOUTH, WEST, EAST);
    }
}
