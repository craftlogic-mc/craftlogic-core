package ru.craftlogic;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.CraftNetwork;
import ru.craftlogic.api.Server;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.block.holders.TileEntityHolder;
import ru.craftlogic.common.ProxyCommon;
import ru.craftlogic.network.message.MessageShowScreen;
import ru.craftlogic.network.message.MessageShowScriptScreen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.craftlogic.api.CraftAPI.MOD_ID;

@Mod(modid = MOD_ID, version = CraftAPI.MOD_VERSION)
public class CraftLogic {
    @SidedProxy(clientSide = "ru.craftlogic.client.ProxyClient", serverSide = "ru.craftlogic.common.ProxyCommon")
    public static ProxyCommon PROXY;

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(PROXY);
        PROXY.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        PROXY.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        for (Block block : Block.REGISTRY) {
            if (block instanceof TileEntityHolder) {
                ((TileEntityHolder) block).registerTileEntity();
            }
        }
        PROXY.postInit();
    }

    @Mod.EventHandler
    public void serverReadyToStart(FMLServerAboutToStartEvent event) {
        CraftAPI.setServer(new Server(event.getServer()));
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        CraftAPI.getServer().start();
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        CraftAPI.getServer().stop(Server.StopReason.CORE);
    }

    public static void showScreen(String id, EntityPlayer player, String args) {
        Server server = CraftAPI.getServer();
        if (server != null) {
            String name = "scripts/screens/" + id;
            Path script = server.getDataDirectory().resolve(name + ".gs");
            if (Files.exists(script)) {
                try {
                    String raw = String.join("\n", Files.readAllLines(script));
                    Path info = server.getDataDirectory().resolve(name + ".json");
                    JsonObject obj;
                    if (Files.exists(info)) {
                        obj = new Gson().fromJson(Files.newBufferedReader(info), JsonObject.class);
                    } else {
                        obj = new JsonObject();
                    }
                    CraftNetwork.sendTo(player, new MessageShowScriptScreen(id, obj, raw, args));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void showScreen(ScreenHolder screenHolder, EntityPlayer player) {
        showScreen(screenHolder, player, 0);
    }

    public static void showScreen(ScreenHolder screenHolder, EntityPlayer player, int subId) {
        Side side = FMLCommonHandler.instance().getSide();
        if (!(player instanceof FakePlayer)) {
            if (player instanceof EntityPlayerMP) {
                Container container = screenHolder.createContainer(player, subId);
                if (container != null) {
                    EntityPlayerMP playerMP = (EntityPlayerMP) player;
                    playerMP.getNextWindowId();
                    playerMP.closeContainer();
                    int windowId = playerMP.currentWindowId;

                    CraftNetwork.sendTo(player, new MessageShowScreen(screenHolder, windowId, subId));

                    container.windowId = windowId;
                    container.addListener(playerMP);
                    playerMP.openContainer = container;
                }
            } else if (side.isClient()) {
                Object screen = screenHolder.createScreen(player, subId);
                FMLCommonHandler.instance().showGuiScreen(screen);
            }
        }
    }
}
