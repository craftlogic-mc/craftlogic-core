package ru.craftlogic.common.world;

import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import ru.craftlogic.api.CraftBlocks;

import java.util.Random;

public class WorldGenRock extends WorldGenerator {
    public WorldGenRock() {}

    private IBlockState getRockForSoil(IBlockState soil) {
        if (soil.getBlock() == Blocks.SAND) {
            switch (soil.getValue(BlockSand.VARIANT)) {
                case SAND:
                    return CraftBlocks.SANDY_ROCK.getDefaultState();
                case RED_SAND:
                    return CraftBlocks.RED_SANDY_ROCK.getDefaultState();
            }
        }
        return CraftBlocks.ROCK.getDefaultState();
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos pos) {
        for (IBlockState s = world.getBlockState(pos); (s.getBlock().isAir(s, world, pos) || s.getBlock().isLeaves(s, world, pos)) && pos.getY() > 0; s = world.getBlockState(pos)) {
            pos = pos.down();
        }
        for (int i = 0; i < 128; ++i) {
            BlockPos p = pos.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
            IBlockState soil = world.getBlockState(p.down());
            IBlockState state = getRockForSoil(soil);
            if (state.getBlock().canPlaceBlockAt(world, p)) {
                world.setBlockState(p, state, 2);
                return true;
            }
        }
        return false;
    }
}
