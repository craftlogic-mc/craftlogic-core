package ru.craftlogic.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStem;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
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
import ru.craftlogic.api.CraftBlocks;
import ru.craftlogic.api.block.BlockNarrow;
import ru.craftlogic.api.block.Colored;
import ru.craftlogic.api.block.Growable;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.WorldNameable;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Supplier;

public class BlockGourd extends BlockNarrow implements Colored, Growable {
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
    public float getBlockHardness(Location location) {
        return 0.2F * (location.getBlockProperty(AGE) + 1);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
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
    protected void randomTick(Location location, Random rand) {
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
                .withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3))
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
        IBlockState stem = location.offset(location.getBlockProperty(FACING)).getBlockState();
        if (location.offset(EnumFacing.DOWN).isSideSolid(EnumFacing.UP) && stem.getBlock() instanceof BlockGourdStem
                && stem.getValue(BlockStem.AGE) == 7) {
            return;
        }

        location.playEvent(2001, Block.getStateId(location.getBlockState()));
        location.setBlockToAir(true);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Items.AIR;
    }

    @Override
    public ItemStack getPickBlock(Location location, RayTraceResult target, EntityPlayer player) {
        return new ItemStack(this.variant.seed.get());
    }

    @Override
    protected IProperty[] getProperties() {
        return new IProperty[] {FACING, AGE};
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
                            .withProperty(BlockHorizontal.FACING, state.getValue(FACING).getOpposite()));
                    break;
                case PUMPKIN:
                    location.setBlockState(Blocks.PUMPKIN.getDefaultState()
                            .withProperty(BlockHorizontal.FACING, state.getValue(FACING).getOpposite()));
                    break;
            }
        }
    }

    public enum GourdVariant implements WorldNameable {
        MELON(() -> CraftBlocks.MELON, () -> Items.MELON_SEEDS),
        PUMPKIN(() -> CraftBlocks.PUMPKIN, () -> Items.PUMPKIN_SEEDS);

        public final Supplier<BlockGourd> crop;
        public final Supplier<Item> seed;

        GourdVariant(Supplier<BlockGourd> crop, Supplier<Item> seed) {
            this.crop = crop;
            this.seed = seed;
        }
    }
}
