package ru.craftlogic;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.common.ProxyCommon;

import static ru.craftlogic.api.CraftAPI.MOD_ID;

@Mod(modid = MOD_ID, version = CraftAPI.MOD_VERSION, dependencies = "after:dynamictrees")
public class CraftLogic {
    @SidedProxy(clientSide = "ru.craftlogic.client.ProxyClient", serverSide = "ru.craftlogic.common.ProxyCommon")
    public static ProxyCommon PROXY;

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    static {
        FluidRegistry.enableUniversalBucket();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(PROXY);
        MinecraftForge.ORE_GEN_BUS.register(PROXY);
        PROXY.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        PROXY.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        PROXY.postInit();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerAboutToStartEvent event) {
        Server.from(event.getServer()).start();
    }
}
