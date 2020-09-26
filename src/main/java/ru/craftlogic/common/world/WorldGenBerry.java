package ru.craftlogic.common.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeForest;
import net.minecraft.world.biome.BiomeHills;
import net.minecraft.world.biome.BiomeTaiga;
import net.minecraft.world.gen.feature.WorldGenerator;
import ru.craftlogic.api.CraftBlocks;
import ru.craftlogic.common.block.BlockBerryBush;

import javax.annotation.Nullable;
import java.util.Random;

public class WorldGenBerry extends WorldGenerator {
    public WorldGenBerry() {}

    @Nullable
    private BlockBerryBush getBerry(Biome biome, Random rand) {
        if (biome instanceof BiomeForest || biome instanceof BiomeTaiga) {
            return rand.nextInt(3) == 0 ? CraftBlocks.BLUEBERRY : (rand.nextInt(2) == 0 ? CraftBlocks.BLACKBERRY : CraftBlocks.RASPBERRY);
        } else if (biome instanceof BiomeHills) {
            return CraftBlocks.STRAWBERRY;
        }
        return null;
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos pos) {
        for (IBlockState s = world.getBlockState(pos); (s.getBlock().isAir(s, world, pos) || s.getBlock().isLeaves(s, world, pos)) && pos.getY() > 0; s = world.getBlockState(pos)) {
            pos = pos.down();
        }
        for (int i = 0; i < 128; ++i) {
            BlockPos p = pos.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
            BlockBerryBush state = getBerry(world.getBiome(pos), rand);
            if (state == null) {
                continue;
            }
            if (state.canPlaceBlockAt(world, p)) {
                world.setBlockState(p, state.getDefaultState().withProperty(BlockBerryBush.RIPE, rand.nextBoolean()), 2);
                if (rand.nextInt(3) == 0) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }
}
