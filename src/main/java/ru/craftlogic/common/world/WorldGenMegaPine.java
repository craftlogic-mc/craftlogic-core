package ru.craftlogic.common.world;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenHugeTrees;
import ru.craftlogic.api.CraftBlocks;
import ru.craftlogic.common.block.BlockLog3;
import ru.craftlogic.common.block.BlockPlanks2;

import java.util.Random;

public class WorldGenMegaPine extends WorldGenHugeTrees {
    private static final IBlockState PODZOL = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);

    public WorldGenMegaPine(boolean notify) {
        super(notify, 13, 15,
            CraftBlocks.LOG3.getDefaultState().withProperty(BlockLog3.VARIANT, BlockPlanks2.PlanksType2.PINE),
            CraftBlocks.LEAVES3.getDefaultState().withProperty(BlockLog3.VARIANT, BlockPlanks2.PlanksType2.PINE)
                .withProperty(BlockLeaves.CHECK_DECAY, Boolean.FALSE)
        );
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos pos) {
        int i = getHeight(rand);

        if (!ensureGrowable(world, rand, pos, i)) {
            return false;
        } else {
            createCrown(world, pos.getX(), pos.getZ(), pos.getY() + i, rand);

            for (int j = 0; j < i; ++j) {
                if (isAirLeaves(world, pos.up(j))) {
                    this.setBlockAndNotifyAdequately(world, pos.up(j), woodMetadata);
                }

                if (j < i - 1) {
                    if (isAirLeaves(world, pos.add(1, j, 0))) {
                        this.setBlockAndNotifyAdequately(world, pos.add(1, j, 0), woodMetadata);
                    }

                    if (isAirLeaves(world, pos.add(1, j, 1))) {
                        this.setBlockAndNotifyAdequately(world, pos.add(1, j, 1), woodMetadata);
                    }


                    if (isAirLeaves(world, pos.add(0, j, 1))) {
                        this.setBlockAndNotifyAdequately(world, pos.add(0, j, 1), woodMetadata);
                    }
                }
            }

            return true;
        }
    }

    private void createCrown(World world, int x, int z, int y, Random rand) {
        int i = rand.nextInt(5) + 4;
        int j = 0;

        for (int k = y - i; k <= y; ++k) {
            int l = y - k;
            int i1 = MathHelper.floor((float) l / (float) i * 3.5F);
            this.growLeavesLayerStrict(world, new BlockPos(x, k, z), i1 + (l > 0 && i1 == j && (k & 1) == 0 ? 1 : 0));
            j = i1;
        }
    }

    public void generateSaplings(World worldIn, Random random, BlockPos pos) {
        this.placePodzolCircle(worldIn, pos.west().north());
        this.placePodzolCircle(worldIn, pos.east(2).north());
        this.placePodzolCircle(worldIn, pos.west().south(2));
        this.placePodzolCircle(worldIn, pos.east(2).south(2));

        for (int i = 0; i < 5; ++i) {
            int j = random.nextInt(64);
            int k = j % 8;
            int l = j / 8;

            if (k == 0 || k == 7 || l == 0 || l == 7) {
                this.placePodzolCircle(worldIn, pos.add(-3 + k, 0, -3 + l));
            }
        }
    }

    private void placePodzolCircle(World worldIn, BlockPos center) {
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                if (Math.abs(i) != 2 || Math.abs(j) != 2) {
                    this.placePodzolAt(worldIn, center.add(i, 0, j));
                }
            }
        }
    }

    private void placePodzolAt(World world, BlockPos pos) {
        for (int i = 2; i >= -3; --i) {
            BlockPos p = pos.up(i);
            IBlockState s = world.getBlockState(p);
            Block b = s.getBlock();

            if (b.canSustainPlant(s, world, p, EnumFacing.UP, ((net.minecraft.block.BlockSapling) Blocks.SAPLING))) {
                this.setBlockAndNotifyAdequately(world, p, PODZOL);
                break;
            }

            if (s.getMaterial() != Material.AIR && i < 0) {
                break;
            }
        }
    }

    private boolean isAirLeaves(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().isAir(state, world, pos) || state.getBlock().isLeaves(state, world, pos);
    }
}