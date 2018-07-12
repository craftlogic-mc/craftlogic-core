package ru.craftlogic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.api.block.BlockBase;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.block.holders.TileEntityHolder;
import ru.craftlogic.api.item.ItemBlockBase;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.network.AdvancedMessageHandler;
import ru.craftlogic.api.network.message.MessageShowScreen;
import ru.craftlogic.api.plants.PlantType;
import ru.craftlogic.api.server.EventConverter;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.sound.SoundSource;
import ru.craftlogic.api.util.TileEntityInfo;
import ru.craftlogic.api.world.DimensionData;
import ru.craftlogic.client.sound.Sound;
import ru.craftlogic.common.ProxyCommon;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Mod(modid = CraftLogic.MODID, version = CraftLogic.VERSION)
public class CraftLogic {
    public static final String MODID = "craftlogic";
    public static final String VERSION = "1.0-ALPHA";

    @SidedProxy(clientSide = "ru.craftlogic.client.ProxyClient", serverSide = "ru.craftlogic.common.ProxyCommon")
    public static ProxyCommon PROXY;

    public static SimpleNetworkWrapper NET;

    private static final Logger LOGGER = LogManager.getLogger(MODID);

    private static Map<ResourceLocation, TileEntityInfo<?>> TILE_REGISTRY = new HashMap<>();
    private static Server SERVER;

    static {
        FluidRegistry.enableUniversalBucket();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(PROXY);
        PROXY.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NET = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        PROXY.init();
    }

    private static int packetId;

    public static <IN extends AdvancedMessage, OUT extends AdvancedMessage>
    void registerMessage(AdvancedMessageHandler<? super IN, ? extends OUT> handler, Class<IN> type, Side side) {
        NET.registerMessage(handler, type, packetId++, side);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        for (Block block : Block.REGISTRY) {
            if (block instanceof TileEntityHolder) {
                if (block instanceof BlockGrass) {
                    System.out.println("Registering tile for grass");
                }
                ((TileEntityHolder) block).registerTileEntity();
            }
        }
        PROXY.postInit();
    }

    @Mod.EventHandler
    public void serverReadyForStart(FMLServerAboutToStartEvent event) {
        SERVER = new Server(event.getServer());
        MinecraftForge.EVENT_BUS.register(new EventConverter(SERVER));
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        try {
            SERVER.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Mod.EventHandler
    public void serverStop(FMLServerStoppingEvent event) {
        SERVER.stop(Server.StopReason.CORE);
    }

    public static DimensionData getDimensionData() {
        World world = getOrLoadDimension(0);
        return getWorldData(world, DimensionData.class, MODID + ":dimension_map");
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

    public static World getOrLoadDimension(int dimension) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            World world = getClientWorld();
            if (world != null && world.provider.getDimension() == dimension) {
                return world;
            }
        } else if (SERVER != null) {
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

    public static String getActiveModId() {
        return getActiveModId(MODID);
    }

    public static String getActiveModId(String fallback) {
        ModContainer amc = Loader.instance().activeModContainer();
        return (amc != null ? amc.getModId() : fallback);
    }

    public static ResourceLocation wrapWithActiveModId(String id, String fallback) {
        return !id.contains(":") ? new ResourceLocation(getActiveModId(fallback), id) : new ResourceLocation(id);
    }

    public static Server getServer() {
        return SERVER;
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

                    NET.sendTo(new MessageShowScreen(screenHolder, windowId, subId), (EntityPlayerMP) player);

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

    public static void registerTileEntity(@Nonnull String name, TileEntityInfo<?> type) {
        ResourceLocation id = wrapWithActiveModId(name, MODID);
        GameRegistry.registerTileEntity(type.clazz, id.toString());
        TILE_REGISTRY.put(id, type);
    }

    public static <T extends TileEntity> TileEntityInfo<T> getTileEntityInfo(Class<T> clazz) {
        for (TileEntityInfo<?> type : TILE_REGISTRY.values()) {
            if (type.clazz == clazz) {
                return (TileEntityInfo<T>) type;
            }
        }
        return null;
    }

    private static int nextEntityId;

    public static <E extends Entity> void registerEntity(Class<E> type, String name, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates) {
        ResourceLocation id = wrapWithActiveModId(name, MODID);
        EntityRegistry.registerModEntity(id, type, id.getResourcePath(), nextEntityId++, id.getResourceDomain(), trackingRange, updateFrequency, sendsVelocityUpdates);
    }

    public static <E extends Entity> void registerEntity(Class<E> type, String name, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates, int eggPrimary, int eggSecondary) {
        ResourceLocation id = wrapWithActiveModId(name, MODID);
        EntityRegistry.registerModEntity(id, type, id.getResourcePath(), nextEntityId++, id.getResourceDomain(), trackingRange, updateFrequency, sendsVelocityUpdates, eggPrimary, eggSecondary);
    }

    @SideOnly(Side.CLIENT)
    public static <E extends Entity> void registerEntityRenderer(Class<E> type, Function<RenderManager, Render<E>> renderer) {
        RenderingRegistry.registerEntityRenderingHandler(type, renderer::apply);
    }

    public static <P extends PlantType> P registerPlant(@Nonnull P plant) {
        GameRegistry.findRegistry(PlantType.class).register(plant);
        return plant;
    }

    public static <B extends Block> B registerBlock(@Nonnull B block) {
        GameRegistry.findRegistry(Block.class).register(block);
        return block;
    }

    public static <B extends BlockBase> B registerBlockWithItem(@Nonnull B block) {
        return registerBlockWithItem(block, ItemBlockBase::new);
    }

    public static <B extends Block> B registerBlockWithItem(@Nonnull B block, Function<B, Item> itemBlockMaker) {
        B result = registerBlock(block);
        Item itemBlock = itemBlockMaker.apply(block).setRegistryName(block.getRegistryName());
        registerItem(itemBlock);
        return result;
    }

    public static Item registerItem(@Nonnull Item item) {
        GameRegistry.findRegistry(Item.class).register(item);
        return item;
    }

    public static SoundEvent registerSoundEvent(@Nonnull String name) {
        ResourceLocation id = wrapWithActiveModId(name, MODID);
        SoundEvent soundEvent = new SoundEvent(id).setRegistryName(id);
        GameRegistry.findRegistry(SoundEvent.class).register(soundEvent);
        return soundEvent;
    }

    @SideOnly(Side.CLIENT)
    public static void playSound(SoundSource source, SoundEvent sound) {
        playSound(source, sound, SoundCategory.BLOCKS);
    }

    @SideOnly(Side.CLIENT)
    public static void playSound(SoundSource source, SoundEvent sound, SoundCategory category) {
        SoundHandler soundHandler = getSoundHandler();
        soundHandler.playSound(new Sound(source, sound, category));
    }

    @SideOnly(Side.CLIENT)
    private static SoundHandler getSoundHandler() {
        return Minecraft.getMinecraft().getSoundHandler();
    }

    @SideOnly(Side.CLIENT)
    private static World getClientWorld() {
        return FMLClientHandler.instance().getWorldClient();
    }
}
