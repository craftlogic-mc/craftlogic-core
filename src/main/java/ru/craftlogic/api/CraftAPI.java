package ru.craftlogic.api;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.network.AdvancedNetwork;
import ru.craftlogic.network.message.MessageShowScreen;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CraftAPI {
    public static final String MOD_ID = "{@mod:id}";
    public static final String MOD_VERSION = "{@mod:version}";
    public static final AdvancedNetwork NETWORK = new AdvancedNetwork(MOD_ID);

    private static boolean init, postInit;

    public static void init(Side side) {
        if (init) {
            throw new IllegalStateException("API has already been initialized!");
        }
        init = true;

        NETWORK.openChannel();
        CraftSounds.init(side);
        CraftFluids.init(side);
        CraftBlocks.init(side);
        CraftItems.init(side);
        CraftTileEntities.init(side);
        CraftEntities.init(side);
        CraftBarrelModes.init(side);
        CraftRecipes.init(side);
    }

    public static void postInit(Side side) {
        if (postInit) {
            throw new IllegalStateException("API has already been post-initialized!");
        }
        postInit = true;

        CraftRecipes.postInit(side);
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


    public static int parseColor(JsonElement e) {
        return parseColor(e.getAsJsonPrimitive().isString() ? e.getAsString() : e.getAsNumber());
    }

    public static int parseColor(Object e) {
        if (e instanceof Number) {
            return ((Number) e).intValue();
        }
        String color = ((String)e).toLowerCase();

        if (color.startsWith("#")) {
            color = color.substring(1);
        } else if (color.startsWith("0x")) {
            color = color.substring(2);
        } else {
            throw new IllegalArgumentException("Illegal color code: " + e);
        }

        return Integer.parseInt(color, 16);
    }

    public static void walkModResources(ModContainer mod, String base, Predicate<Path> filter, Consumer<Path> visitor) {
        Path source = mod.getSource().toPath();
        if (Files.exists(source)) {
            if (Files.isDirectory(source)) {
                try {
                    Path root = source.resolve(base);
                    if (Files.exists(root)) {
                        Files.walk(root).filter(filter).forEach(visitor);
                    }
                } catch (IOException e) {
                    throw new IllegalStateException("Error walking root directory", e);
                }
            } else {
                try (FileSystem fs = FileSystems.newFileSystem(source, null)) {
                    Path root = fs.getPath("/" + base);
                    if (Files.exists(root)) {
                        Files.walk(root).filter(filter).forEach(visitor);
                    }
                } catch (IOException e) {
                    throw new IllegalStateException("Error loading FileSystem from jar", e);
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

                    NETWORK.sendTo(player, new MessageShowScreen(screenHolder, windowId, subId));

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
