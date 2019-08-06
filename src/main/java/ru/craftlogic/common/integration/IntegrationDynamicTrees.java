package ru.craftlogic.common.integration;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.WorldGenRegistry.BiomeDataBasePopulatorRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ru.craftlogic.api.integration.ModIntegration;
import ru.craftlogic.common.worldgen.BiomeTreePopulator;

public class IntegrationDynamicTrees implements ModIntegration {
    private static final String MOD_ID = "dynamictrees";

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    @Optional.Method(modid = MOD_ID)
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(this);
        ModConfigs.replaceVanillaSapling = true;
    }

    @Override
    public void init() {}

    @Override
    public void postInit() {}

    @SubscribeEvent
    @Optional.Method(modid = MOD_ID)
    public static void registerTreePopulator(final BiomeDataBasePopulatorRegistryEvent event) {
        event.register(new BiomeTreePopulator());
    }
}
