package ru.craftlogic.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.CraftSounds;
import ru.craftlogic.api.block.Colored;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.item.ItemBlockBase;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.screen.Elements;
import ru.craftlogic.api.screen.toast.AdvancedToast;
import ru.craftlogic.client.screen.ScreenPlayerInfo;
import ru.craftlogic.client.screen.ScreenQuestion;
import ru.craftlogic.client.screen.ScreenReconnect;
import ru.craftlogic.client.screen.toast.ToastCountdown;
import ru.craftlogic.client.screen.toast.ToastText;
import ru.craftlogic.common.ProxyCommon;
import ru.craftlogic.network.message.*;
import ru.craftlogic.util.ReflectiveUsage;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@ReflectiveUsage
@SideOnly(Side.CLIENT)
public class ProxyClient extends ProxyCommon {
    private final ModelManager modelManager = new ModelManager();
    private final Minecraft client = FMLClientHandler.instance().getClient();
    private final ExecutorService taskScheduler = Executors.newSingleThreadExecutor(
        r -> new Thread(r, "Client task scheduler")
    );

    public void addTask(Consumer<Minecraft> task) {
        this.addDelayedTask(task, 0L);
    }

    public void addDelayedTask(Consumer<Minecraft> task, long delay) {
        long start = System.currentTimeMillis();
        this.client.addScheduledTask(() -> {
            long delta = System.currentTimeMillis() - start;
            if (delta >= delay) {
                task.accept(this.client);
            } else {
                this.taskScheduler.submit(() -> addDelayedTask(task, delay - delta));
            }
        });
    }

    @Override
    public void preInit() {
        Elements.init();
        super.preInit();
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void postInit() {
        super.postInit();
    }

    @SubscribeEvent
    public void onModelsRegister(ModelRegistryEvent event) {
        this.modelManager.init();
    }

    @SubscribeEvent
    public void onBlockColorRegister(ColorHandlerEvent.Block event) {
        for (Block block : Block.REGISTRY) {
            if (block instanceof Colored) {
                event.getBlockColors().registerBlockColorHandler(((Colored) block)::getBlockColor, block);
            }
        }
    }

    @SubscribeEvent
    public void onItemColorRegister(ColorHandlerEvent.Item event) {
        for (Item item : Item.REGISTRY) {
            if (item instanceof Colored) {
                if (item instanceof ItemBlockBase) {
                    Block block = Block.getBlockFromItem(item);
                    if (block instanceof Colored) {
                        event.getItemColors().registerItemColorHandler(((Colored) block)::getItemColor, block);
                        continue;
                    }
                }
                event.getItemColors().registerItemColorHandler(((Colored) item)::getItemColor, item);
            }
        }
    }

    @SubscribeEvent
    public void onOverlayRender(RenderGameOverlayEvent.Pre event) {
        EntityPlayerSP player = this.client.player;
        if (player != null && CraftConfig.tweaks.enableHidingFullHUDBars) {
            switch (event.getType()) {
                case HEALTH:
                    if (player.getHealth() == player.getMaxHealth()) {
                        event.setCanceled(true);
                    }
                    break;
                case FOOD:
                    if (!player.getFoodStats().needFood()) {
                        event.setCanceled(true);
                    }
                    break;
                case EXPERIENCE: {
                    if (player.experience == 0) {
                        event.setCanceled(true);
                    }
                    break;
                }
                case HEALTHMOUNT: {
                    Entity ridingEntity = player.getRidingEntity();
                    if (ridingEntity instanceof EntityLivingBase && ((EntityLivingBase) ridingEntity).getHealth() == ((EntityLivingBase) ridingEntity).getMaxHealth()) {
                        event.setCanceled(true);
                    }
                    break;
                }
            }
        }
    }

    @Override
    protected AdvancedMessage handleServerStop(MessageServerStop message, MessageContext context) {
        syncTask(context, () -> {
            GuiToast toasts = this.client.getToastGui();
            String address = null;
            if (this.client.getConnection() != null) {
                NetworkManager networkManager = this.client.getConnection().getNetworkManager();
                InetSocketAddress remoteAddress = (InetSocketAddress) networkManager.getRemoteAddress();
                address = remoteAddress.getHostString() + ":" + remoteAddress.getPort();
            }
            int delay = message.getDelay();
            int reconnect = message.getReconnect();
            String a = address;
            toasts.add(
                new ToastCountdown("stop", new TextComponentTranslation("tooltip.server_stop"), delay, 0xFF777777, CraftSounds.COUNTDOWN_TICK)
            );
            addDelayedTask(client -> {
                WorldClient world = client.world;
                if (world != null) {
                    world.sendQuittingDisconnectingPacket();
                    client.loadWorld(null);
                }
                if (reconnect > 0 && a != null) {
                    client.displayGuiScreen(new ScreenReconnect(a, reconnect, new TextComponentTranslation("gui.reconnect.stop")));
                } else if (world != null) {
                    client.displayGuiScreen(new GuiDisconnected(new GuiMultiplayer(new GuiMainMenu()), "disconnect.lost", new TextComponentTranslation("multiplayer.disconnect.server_shutdown")));
                }
            }, delay * 1000L);
        });
        return null;
    }

    @Override
    protected AdvancedMessage handleServerCrash(MessageServerCrash message, MessageContext context) {
        syncTask(context, () -> {
            String address = null;
            if (this.client.getConnection() != null) {
                NetworkManager networkManager = this.client.getConnection().getNetworkManager();
                InetSocketAddress remoteAddress = (InetSocketAddress) networkManager.getRemoteAddress();
                address = remoteAddress.getHostString() + ":" + remoteAddress.getPort();
            }
            int reconnect = message.getReconnect();
            String a = address;

            WorldClient world = client.world;
            if (world != null) {
                world.sendQuittingDisconnectingPacket();
                client.loadWorld(null);
            }
            if (reconnect > 0 && a != null) {
                client.displayGuiScreen(new ScreenReconnect(a, reconnect, new TextComponentTranslation("gui.reconnect.crash")));
            } else if (world != null) {
                client.displayGuiScreen(new GuiDisconnected(new GuiMultiplayer(new GuiMainMenu()), "disconnect.lost", new TextComponentTranslation("multiplayer.disconnect.server_shutdown")));
            }
        });
        return null;
    }

    @Override
    protected AdvancedMessage handleShowScreen(MessageShowScreen message, MessageContext context) {
        switch (message.getType()) {
            case TILE: {
                syncTask(context, () -> {
                    EntityPlayer player = getPlayer(context);
                    ScreenHolder screenHolder = message.getLocation().getTileEntity(ScreenHolder.class);
                    CraftAPI.showScreen(screenHolder, player, message.getExtraData());
                    player.openContainer.windowId = message.getWindowId();
                });
                break;
            }
            case ENTITY: {
                syncTask(context, () -> {
                    EntityPlayer player = getPlayer(context);
                    Entity entity = player.world.getEntityByID(message.getEntityId());
                    if (entity instanceof ScreenHolder) {
                        CraftAPI.showScreen((ScreenHolder) entity, player, message.getExtraData());
                        player.openContainer.windowId = message.getWindowId();
                    }
                });
                break;
            }
        }
        return null;
    }

    @Override
    protected AdvancedMessage handleToast(MessageToast message, MessageContext context) {
        ToastText toast = new ToastText(message.getTitle(), message.getSubtitle(), message.getTimeout());
        syncTask(context, () -> {
            GuiToast toasts = this.client.getToastGui();
            toasts.add(toast);
        });
        return null;
    }

    @Override
    protected AdvancedMessage handleCountdown(MessageCountdown message, MessageContext context) {
        syncTask(context, () -> {
            GuiToast toasts = this.client.getToastGui();
            ((AdvancedToast)toasts).remove(t -> t instanceof ToastCountdown && t.getType().equals(message.getId()));
            toasts.add(
                new ToastCountdown(message.getId(), message.getTitle(), message.getTimeout(), message.getColor(), message.getTickSound())
            );
        });
        return null;
    }

    @Override
    protected AdvancedMessage handleQuestion(MessageQuestion message, MessageContext context) {
        String id = message.getId();
        ScreenQuestion screen = new ScreenQuestion(message.getQuestion(), message.getTimeout(), choice ->
            CraftAPI.NETWORK.sendToServer(new MessageConfirmation(id, choice))
        );
        syncTask(context, () -> this.client.displayGuiScreen(screen));
        return null;
    }

    @Override
    protected AdvancedMessage handlePlayerInfo(MessagePlayerInfo message, MessageContext context) {
        ScreenPlayerInfo screen = new ScreenPlayerInfo(
            message.getProfile(), message.getFirstPlayed(), message.getLastPlayed(), message.getTimePlayed(),
            message.isEditAllowed(), message.getLastLocation(), message.getBedLocation()
        );
        syncTask(context, () -> this.client.displayGuiScreen(screen));
        return null;
    }
}

