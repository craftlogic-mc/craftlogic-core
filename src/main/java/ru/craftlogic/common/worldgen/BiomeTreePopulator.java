package ru.craftlogic.common.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDataBasePopulator;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBasePopulatorJson;
import net.minecraft.util.ResourceLocation;

import static ru.craftlogic.api.CraftAPI.MOD_ID;

public class BiomeTreePopulator implements IBiomeDataBasePopulator {
    private final BiomeDataBasePopulatorJson populator;

    public BiomeTreePopulator() {
        populator = new BiomeDataBasePopulatorJson(new ResourceLocation(MOD_ID, "worldgen/trees.json"));
    }

    @Override
    public void populate(BiomeDataBase biomeData) {
        populator.populate(biomeData);
    }
}
