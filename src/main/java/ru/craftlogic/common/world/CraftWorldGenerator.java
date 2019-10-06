package ru.craftlogic.common.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public final class CraftWorldGenerator implements IWorldGenerator {
    private final WorldGenRock rockGen = new WorldGenRock();

    @Override
    public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        for (int i = 0; i < 1; ++i) {
            int x = chunkX * 16 + rand.nextInt(16) + 8;
            int z = chunkZ * 16 + rand.nextInt(16) + 8;
            int y = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY() * 2;
            rockGen.generate(world, rand, new BlockPos(x, y, z));
        }
    }
}
