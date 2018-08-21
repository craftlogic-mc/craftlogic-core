package ru.craftlogic.api;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.world.DimensionData;

public class CraftWorlds {
    static void init() {

    }

    public static DimensionData getDimensionData() {
        World world = getOrLoadWorld(0);
        return getWorldData(world, DimensionData.class, "craft_dimension_map");
    }

    public static <T extends WorldSavedData> T getWorldData(World world, Class<T> cls, String name) {
        MapStorage storage = world.getPerWorldStorage();
        T result = (T) storage.getOrLoadData(cls, name);
        if (result == null) {
            try {
                result = cls.getConstructor(String.class).newInstance(name);
                storage.setData(name, result);
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }

        return result;
    }

    public static World getOrLoadWorld(int dimension) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            World world = getClientWorld();
            if (world != null && world.provider.getDimension() == dimension) {
                return world;
            }
        } else if (CraftAPI.getServer() != null) {
            if (DimensionManager.isDimensionRegistered(dimension)) {
                WorldServer ret = DimensionManager.getWorld(dimension);
                if (ret == null) {
                    DimensionManager.initDimension(dimension);
                    ret = DimensionManager.getWorld(dimension);
                }

                return ret;
            }
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public static World getClientWorld() {
        return FMLClientHandler.instance().getWorldClient();
    }
}
