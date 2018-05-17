package ru.craftlogic.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.block.BlockBase;
import ru.craftlogic.api.block.Colored;
import ru.craftlogic.api.block.Growable;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.WorldNameable;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Supplier;

public class BlockGourd extends BlockBase implements Colored, Growable {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 2);

    public static final AxisAlignedBB[] BOUNDING = {
        new AxisAlignedBB(0.1875, 0, 0.1875, 0.8125, 0.625, 0.8125),
        new AxisAlignedBB(0.125, 0, 0.125, 0.875, 0.75, 0.875),
        new AxisAlignedBB(0.0625, 0, 0.0625, 0.9375, 0.875, 0.9375),
    };

    private final GourdVariant variant;

    public BlockGourd(GourdVariant variant) {
        super(Material.GOURD, variant.getName(), 0.5F, null);
        this.variant = variant;
        this.setSoundType(SoundType.WOOD);
        this.setHardness(0.6F);
        this.setTickRandomly(true);
    }

    @Override
    public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
        return 0.2F * (state.getValue(AGE) + 1);
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
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    protected AxisAlignedBB getBoundingBox(Location location) {
        return BOUNDING[location.getBlockState().getValue(AGE)];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBlockColor(@Nullable Location pos, IBlockState state, int tint) {
        return 0xE0C71C;
    }

    @Override
    protected void updateTick(Location location, Random rand) {
        if (location.isAreaLoaded(1)) {
            if (location.offset(EnumFacing.UP).getLightFromNeighbors() >= 9) {
                BlockPos pos = location.getPos();
                World world = location.getWorld();
                float f = BlockGourdStem.getGrowthChance(this, world, pos);
                IBlockState state = location.getBlockState();
                if (ForgeHooks.onCropsGrowPre(world, pos, state, rand.nextInt((int)(25.0F / f) + 1) == 0)) {
                    this.grow(world, rand, pos, state);
                    ForgeHooks.onCropsGrowPost(world, pos, state, location.getBlockState());
                }
            }
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(FACING, EnumFacing.getHorizontal(meta & 3))
                .withProperty(AGE, meta / 4);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(FACING).getHorizontalIndex();
        meta |= state.getValue(AGE) * 4;
        return meta;
    }

    @Override
    public void neighborChanged(Location selfLocation, Location neighborLocation, Block neighborBlock) {
        super.neighborChanged(selfLocation, neighborLocation, neighborBlock);
        this.checkCanStay(selfLocation);
    }

    private void checkCanStay(Location location) {
        if (!location.offset(EnumFacing.DOWN).isSideSolid(EnumFacing.UP)) {
            location.playEvent(2001, Block.getStateId(location.getBlockState()));
            location.setBlockToAir(true);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        switch (this.variant) {
            case MELON:
                return Items.MELON_SEEDS;
            case PUMPKIN:
                return Items.PUMPKIN_SEEDS;
        }
        return super.getItemDropped(state, random, fortune);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        switch (this.variant) {
            case MELON:
                return new ItemStack(Items.MELON_SEEDS);
            case PUMPKIN:
                return new ItemStack(Items.PUMPKIN_SEEDS);
        }
        return super.getPickBlock(state, target, world, pos, player);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, AGE);
    }

    @Override
    public void grow(Location location, Random rand) {
        IBlockState state = location.getBlockState();
        int i = state.getValue(AGE);
        if (i < 2) {
            location.setBlockProperty(AGE, i + 1, 2);
        } else {
            switch (this.variant) {
                case MELON:
                    location.setBlockState(Blocks.MELON_BLOCK.getDefaultState()
                            .withProperty(BlockPumpkin.FACING, state.getValue(FACING)));
                    break;
                case PUMPKIN:
                    location.setBlockState(Blocks.PUMPKIN.getDefaultState()
                            .withProperty(BlockPumpkin.FACING, state.getValue(FACING)));
                    break;
            }
        }
    }

    public enum GourdVariant implements WorldNameable {
        MELON(() -> (BlockGourd) CraftLogic.BLOCK_MELON),
        PUMPKIN(() -> (BlockGourd) CraftLogic.BLOCK_PUMPKIN);

        public final Supplier<BlockGourd> crop;

        GourdVariant(Supplier<BlockGourd> crop) {
            this.crop = crop;
        }
    }
}
