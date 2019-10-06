package ru.craftlogic.common.world;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import ru.craftlogic.api.CraftBlocks;
import ru.craftlogic.common.block.BlockLog3;
import ru.craftlogic.common.block.BlockPlanks2;

import java.util.Random;

public class WorldGenWillow extends WorldGenAbstractTree {
    private final IBlockState trunk = CraftBlocks.LOG3.getDefaultState().withProperty(BlockLog3.VARIANT, BlockPlanks2.PlanksType2.WILLOW);
    private final IBlockState leaf = CraftBlocks.LEAVES3.getDefaultState().withProperty(BlockLog3.VARIANT, BlockPlanks2.PlanksType2.WILLOW).withProperty(BlockLeaves.CHECK_DECAY, Boolean.FALSE);

    public WorldGenWillow(boolean notify) {
        super(notify);
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos pos) {
        int i = rand.nextInt(4) + 5;

        while (world.getBlockState(pos.down()).getMaterial() == Material.WATER) {
            pos = pos.down();
        }

        boolean flag = true;

        if (pos.getY() >= 1 && pos.getY() + i + 1 <= 256) {
            for (int j = pos.getY(); j <= pos.getY() + 1 + i; ++j) {
                int k = 1;

                if (j == pos.getY()) {
                    k = 0;
                }

                if (j >= pos.getY() + 1 + i - 2) {
                    k = 3;
                }

                BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();

                for (int dx = pos.getX() - k; dx <= pos.getX() + k && flag; ++dx) {
                    for (int dz = pos.getZ() - k; dz <= pos.getZ() + k && flag; ++dz) {
                        if (j >= 0 && j < 256) {
                            IBlockState iblockstate = world.getBlockState(p.setPos(dx, j, dz));
                            Block block = iblockstate.getBlock();

                            if (!iblockstate.getBlock().isAir(iblockstate, world, p.setPos(dx, j, dz)) && !iblockstate.getBlock().isLeaves(iblockstate, world, p.setPos(dx, j, dz))) {
                                if (block != Blocks.WATER && block != Blocks.FLOWING_WATER) {
                                    flag = false;
                                } else if (j > pos.getY()) {
                                    flag = false;
                                }
                            }
                        } else {
                            flag = false;
                        }
                    }
                }
            }

            if (!flag) {
                return false;
            } else {
                BlockPos down = pos.down();
                IBlockState state = world.getBlockState(down);
                boolean isSoil = state.getBlock().canSustainPlant(state, world, down, net.minecraft.util.EnumFacing.UP, ((net.minecraft.block.BlockSapling) Blocks.SAPLING));

                if (isSoil && pos.getY() < world.getHeight() - i - 1) {
                    state.getBlock().onPlantGrow(state, world, pos.down(), pos);

                    for (int k1 = pos.getY() - 3 + i; k1 <= pos.getY() + i; ++k1) {
                        int j2 = k1 - (pos.getY() + i);
                        int l2 = 2 - j2 / 2;

                        for (int j3 = pos.getX() - l2; j3 <= pos.getX() + l2; ++j3) {
                            int k3 = j3 - pos.getX();

                            for (int i4 = pos.getZ() - l2; i4 <= pos.getZ() + l2; ++i4) {
                                int j1 = i4 - pos.getZ();

                                if (Math.abs(k3) != l2 || Math.abs(j1) != l2 || rand.nextInt(2) != 0 && j2 != 0) {
                                    BlockPos blockpos = new BlockPos(j3, k1, i4);
                                    state = world.getBlockState(blockpos);

                                    if (state.getBlock().canBeReplacedByLeaves(state, world, blockpos)) {
                                        this.setBlockAndNotifyAdequately(world, blockpos, leaf);
                                    }
                                }
                            }
                        }
                    }

                    for (int j = 0; j < i; ++j) {
                        BlockPos upN = pos.up(j);
                        IBlockState s = world.getBlockState(upN);
                        Block block2 = s.getBlock();

                        if (block2.isAir(s, world, upN) || block2.isLeaves(s, world, upN) || block2 == Blocks.FLOWING_WATER || block2 == Blocks.WATER) {
                            this.setBlockAndNotifyAdequately(world, pos.up(j), trunk);
                        }
                    }

                    for (int dy = pos.getY() - 3 + i; dy <= pos.getY() + i; ++dy) {
                        int k2 = dy - (pos.getY() + i);
                        int i3 = 2 - k2 / 2;
                        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();

                        for (int dx = pos.getX() - i3; dx <= pos.getX() + i3; ++dx) {
                            for (int dz = pos.getZ() - i3; dz <= pos.getZ() + i3; ++dz) {
                                p.setPos(dx, dy, dz);

                                if (world.getBlockState(p).getMaterial() == Material.LEAVES) {
                                    BlockPos pw = p.west();
                                    BlockPos pe = p.east();
                                    BlockPos pn = p.north();
                                    BlockPos ps = p.south();

                                    if (rand.nextInt(4) == 0 && world.isAirBlock(pw)) {
                                        addCatkin(world, pw);
                                    }

                                    if (rand.nextInt(4) == 0 && world.isAirBlock(pe)) {
                                        addCatkin(world, pe);
                                    }

                                    if (rand.nextInt(4) == 0 && world.isAirBlock(pn)) {
                                        addCatkin(world, pn);
                                    }

                                    if (rand.nextInt(4) == 0 && world.isAirBlock(ps)) {
                                        addCatkin(world, ps);
                                    }
                                }
                            }
                        }
                    }

                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private void addCatkin(World world, BlockPos pos) {
        this.setBlockAndNotifyAdequately(world, pos, leaf);
        int i = 4;

        for (BlockPos p = pos.down(); world.isAirBlock(p) && i > 0; --i) {
            this.setBlockAndNotifyAdequately(world, p, leaf);
            p = p.down();
        }
    }
}