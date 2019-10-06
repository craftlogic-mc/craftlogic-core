package ru.craftlogic.mixin.world.biome;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeTaiga;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.common.world.CraftWorldGenerator;

import java.util.Random;

@Mixin(BiomeTaiga.class)
public abstract class MixinBiomeTaiga extends Biome {
    @Shadow @Final private BiomeTaiga.Type type;
    @Shadow @Final private static WorldGenTaiga2 SPRUCE_GENERATOR;
    @Shadow @Final private static WorldGenMegaPineTree MEGA_SPRUCE_GENERATOR;

    public MixinBiomeTaiga(BiomeProperties properties) {
        super(properties);
    }

    /**
     * @author Radvihger
     * @reason Custom pine tree
     */
    @Overwrite
    public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
        if ((type == BiomeTaiga.Type.MEGA || type == BiomeTaiga.Type.MEGA_SPRUCE) && rand.nextInt(3) == 0) {
            return type != BiomeTaiga.Type.MEGA_SPRUCE && rand.nextInt(13) != 0 ? CraftWorldGenerator.getMegaPineGenerator() : MEGA_SPRUCE_GENERATOR;
        } else {
            return rand.nextInt(3) == 0 ? CraftWorldGenerator.getPineGenerator() : SPRUCE_GENERATOR;
        }
    }
}
