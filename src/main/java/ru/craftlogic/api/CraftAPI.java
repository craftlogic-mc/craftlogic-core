package ru.craftlogic.api;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;

public class CraftAPI {
    public static final String MOD_ID = "{@mod:id}";
    public static final String MOD_VERSION = "{@mod:version}";

    private static Server server;
    private static boolean init;

    public static void init(Side side) {
        if (init) {
            throw new IllegalStateException("API has already been initialized!");
        }
        init = true;

        CraftNetwork.init(side);
        CraftSounds.init(side);
        CraftFluids.init(side);
        CraftBlocks.init(side);
        CraftItems.init(side);
        CraftTileEntities.init(side);
        CraftEntities.init(side);
        CraftPlants.init(side);
        CraftBarrelModes.init(side);
        CraftRecipes.init(side);
    }

    public static Server getServer() {
        return server;
    }

    public static void setServer(Server server) {
        if (CraftAPI.server != null) {
            throw new IllegalStateException("Server has already been set!");
        }
        CraftAPI.server = server;
    }

    public static String getActiveModId() {
        return getActiveModId(MOD_ID);
    }

    public static String getActiveModId(String fallback) {
        ModContainer amc = Loader.instance().activeModContainer();
        return (amc != null ? amc.getModId() : fallback);
    }

    public static ResourceLocation wrapWithActiveModId(String name, String fallback) {
        return wrapWithModId(name, getActiveModId(fallback));
    }

    public static ResourceLocation wrapWithModId(String name, String modId) {
        return !name.contains(":") ? new ResourceLocation(modId, name) : new ResourceLocation(name);
    }
}
