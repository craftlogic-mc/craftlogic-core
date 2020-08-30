package ru.craftlogic.common.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;
import java.util.function.Supplier;

public class WorldGenLyingItem extends WorldGenerator {
    private final Supplier<IBlockState> state;

    public WorldGenLyingItem(Supplier<IBlockState> state) {
        this.state = state;
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos pos) {
        IBlockState state = this.state.get();
        for (IBlockState s = world.getBlockState(pos); (s.getBlock().isAir(s, world, pos) || s.getBlock().isLeaves(s, world, pos)) && pos.getY() > 0; s = world.getBlockState(pos)) {
            pos = pos.down();
        }
        for (int i = 0; i < 128; ++i) {
            BlockPos p = pos.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
            if (state.getBlock().canPlaceBlockAt(world, p)) {
                world.setBlockState(p, state, 2);
                return true;
            }
        }
        return false;
    }
}
