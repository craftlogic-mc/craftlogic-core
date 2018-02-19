package ru.craftlogic;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.EventConverter;
import ru.craftlogic.api.Server;
import ru.craftlogic.api.block.BlockBase;
import ru.craftlogic.api.block.LoopingSoundSource;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.block.holders.TileEntityHolder;
import ru.craftlogic.api.item.ItemBlockBase;
import ru.craftlogic.api.util.TileEntityInfo;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.client.PositionedSoundLoop;
import ru.craftlogic.common.CraftEventListener;
import ru.craftlogic.common.ProxyCommon;
import ru.craftlogic.util.DimensionMap;

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

    private static Map<ResourceLocation, TileEntityInfo<?>> TILE_REGISTRY = new HashMap<>();
    private static Server SERVER;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new CraftEventListener());
        MinecraftForge.EVENT_BUS.register(PROXY);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, PROXY);
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

    public static DimensionMap getDimensionMap() {
        World world = getOrLoadDimension(0);
        return getWorldData(world, DimensionMap.class, MODID + ":dimension_map");
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

    public static WorldServer getOrLoadDimension(int dimension) {
        if (DimensionManager.isDimensionRegistered(dimension)) {
            WorldServer ret = DimensionManager.getWorld(dimension);
            if (ret == null) {
                DimensionManager.initDimension(dimension);
                ret = DimensionManager.getWorld(dimension);
            }

            return ret;
        } else {
            return null;
        }
    }

    public static String getActiveModId() {
        ModContainer amc = Loader.instance().activeModContainer();
        return (amc != null ? amc.getModId() : MODID);
    }

    public static Server getServer() {
        return SERVER;
    }

    public static void showScreen(ScreenHolder screenHolder, EntityPlayer player) {
        showScreen(screenHolder, player, 0);
    }

    public static void showScreen(ScreenHolder screenHolder, EntityPlayer player, int subId) {
        Location pos = screenHolder.getLocation();
        player.openGui(MODID, subId, player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
    }

    public static void registerTileEntity(@Nonnull String name, TileEntityInfo<?> type) {
        if (!name.contains(":")) {
            name = getActiveModId() + name;
        }
        GameRegistry.registerTileEntity(type.clazz, name);
        TILE_REGISTRY.put(new ResourceLocation(name), type);
    }

    public static <T extends TileEntity> TileEntityInfo<T> getTileEntityInfo(Class<T> clazz) {
        for (TileEntityInfo<?> type : TILE_REGISTRY.values()) {
            if (type.clazz == clazz) {
                return (TileEntityInfo<T>) type;
            }
        }
        return null;
    }

    public static IBlockState registerBlock(@Nonnull Block block) {
        GameRegistry.findRegistry(Block.class).register(block);
        return block.getDefaultState();
    }

    public static IBlockState registerBlockWithItem(@Nonnull BlockBase block) {
        return registerBlockWithItem(block, ItemBlockBase::new);
    }

    public static <B extends Block> IBlockState registerBlockWithItem(@Nonnull B block, Function<B, Item> itemBlockMaker) {
        IBlockState state = registerBlock(block);
        Item itemBlock = itemBlockMaker.apply(block).setRegistryName(block.getRegistryName());
        registerItem(itemBlock);
        return state;
    }

    public static Item registerItem(@Nonnull Item item) {
        GameRegistry.findRegistry(Item.class).register(item);
        return item;
    }

    public static SoundEvent registerSoundEvent(@Nonnull String name) {
        if (!name.contains(":")) {
            name = getActiveModId() + ":" + name;
        }
        SoundEvent soundEvent = new SoundEvent(new ResourceLocation(name)).setRegistryName(name);
        GameRegistry.findRegistry(SoundEvent.class).register(soundEvent);
        return soundEvent;
    }

    @SideOnly(Side.CLIENT)
    public static void playLoopingSound(LoopingSoundSource source, SoundEvent sound) {
        playLoopingSound(source, sound, SoundCategory.BLOCKS);
    }

    @SideOnly(Side.CLIENT)
    public static void playLoopingSound(LoopingSoundSource source, SoundEvent sound, SoundCategory category) {
        PositionedSoundLoop loop = new PositionedSoundLoop(source, sound, category);
        SoundHandler soundHandler = getSoundHandler();
        soundHandler.playSound(loop);
    }

    @SideOnly(Side.CLIENT)
    private static SoundHandler getSoundHandler() {
        return Minecraft.getMinecraft().getSoundHandler();
    }
}
