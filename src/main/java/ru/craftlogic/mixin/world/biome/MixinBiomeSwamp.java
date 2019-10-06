package ru.craftlogic.mixin.world.biome;

import net.minecraft.world.biome.BiomeSwamp;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ru.craftlogic.common.world.CraftWorldGenerator;

import java.util.Random;

@Mixin(BiomeSwamp.class)
public class MixinBiomeSwamp {
    /**
     * @author Radviger
     * @reason Custom willow tree
     */
    @Overwrite
    public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
        return CraftWorldGenerator.getWillowGenerator();
    }
}
