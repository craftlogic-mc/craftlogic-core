package ru.craftlogic.common.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeForest;
import net.minecraft.world.biome.BiomeHills;
import net.minecraft.world.biome.BiomeTaiga;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.fml.common.IWorldGenerator;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.CraftBlocks;

import java.util.Random;

public final class CraftWorldGenerator implements IWorldGenerator {
    private static WorldGenAbstractTree PINE_GENERATOR, MEGA_PINE_GENERATOR, WILLOW_GENERATOR;

    private final WorldGenRock rockGen = new WorldGenRock();
    private final WorldGenLyingItem stickGen = new WorldGenLyingItem(() -> CraftBlocks.STICK.getDefaultState());
    private final WorldGenBerry berryGen = new WorldGenBerry();

    public static WorldGenAbstractTree getPineGenerator() {
        if (PINE_GENERATOR == null) {
            PINE_GENERATOR = new WorldGenPine(false);
        }
        return PINE_GENERATOR;
    }

    public static WorldGenAbstractTree getMegaPineGenerator() {
        if (MEGA_PINE_GENERATOR == null) {
            MEGA_PINE_GENERATOR = new WorldGenMegaPine(false);
        }
        return MEGA_PINE_GENERATOR;
    }

    public static WorldGenAbstractTree getWillowGenerator() {
        if (WILLOW_GENERATOR == null) {
            WILLOW_GENERATOR = new WorldGenWillow(false);
        }
        return WILLOW_GENERATOR;
    }

    @Override
    public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (CraftConfig.items.enableRocks && world.provider.getDimensionType() == DimensionType.OVERWORLD) {
            for (int i = 0; i < 1; ++i) {
                int x = chunkX * 16 + rand.nextInt(16) + 8;
                int z = chunkZ * 16 + rand.nextInt(16) + 8;
                int y = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY() * 2;
                rockGen.generate(world, rand, new BlockPos(x, y, z));
            }
        }
        for (int i = 0; i < 1; ++i) {
            int x = chunkX * 16 + rand.nextInt(16) + 8;
            int z = chunkZ * 16 + rand.nextInt(16) + 8;
            int y = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY() * 2;
            Biome biome = world.getBiome(new BlockPos(x, y, z));
            if (biome.decorator.treesPerChunk > 0) {
                stickGen.generate(world, rand, new BlockPos(x, y, z));
            }
        }
        if (CraftConfig.items.enableBerries && rand.nextInt(20) == 0) {
            for (int i = 0; i < 1; ++i) {
                int x = chunkX * 16 + rand.nextInt(16) + 8;
                int z = chunkZ * 16 + rand.nextInt(16) + 8;
                int y = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY() * 2;
                Biome biome = world.getBiome(new BlockPos(x, y, z));
                if (biome instanceof BiomeForest || biome instanceof BiomeTaiga || biome instanceof BiomeHills) {
                    berryGen.generate(world, rand, new BlockPos(x, y, z));
                }
            }
        }
    }
}
