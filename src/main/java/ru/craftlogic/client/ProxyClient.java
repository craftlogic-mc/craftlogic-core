package ru.craftlogic.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.CraftSounds;
import ru.craftlogic.api.block.Colored;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.item.ItemBlockBase;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.screen.Elements;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.client.screen.ScreenCustom;
import ru.craftlogic.client.screen.toast.ToastCountdown;
import ru.craftlogic.client.screen.toast.ToastText;
import ru.craftlogic.common.ProxyCommon;
import ru.craftlogic.network.message.*;
import ru.craftlogic.util.ReflectiveUsage;

@ReflectiveUsage
@SideOnly(Side.CLIENT)
public class ProxyClient extends ProxyCommon {
    protected static final AxisAlignedBB SLAB_BOTTOM_BOUNDING = new AxisAlignedBB(0, 0, 0, 1, 0.5, 1);
    protected static final AxisAlignedBB SLAB_TOP_BOUNDING = new AxisAlignedBB(0, 0.5, 0, 1, 1, 1);

    private Balance balance;
    private final ModelManager modelManager = new ModelManager();
    private Minecraft client;

    @Override
    public void preInit() {
        super.preInit();
        this.client = Minecraft.getMinecraft();
        Elements.init();
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
    public void onHighlightDraw(DrawBlockHighlightEvent event) {
        EntityPlayer player = event.getPlayer();
        RayTraceResult target = event.getTarget();
        float partialTicks = event.getPartialTicks();
        if (target.typeOfHit == RayTraceResult.Type.BLOCK) {
            Location location = new Location(player.world, target.getBlockPos());
            Block block = location.getBlock();
            if (block instanceof BlockSlab && ((BlockSlab) block).isDouble() && location.isWithinWorldBorder()) {
                AxisAlignedBB bb = target.hitVec.y > 0.5 ? SLAB_TOP_BOUNDING : SLAB_BOTTOM_BOUNDING;
                this.drawSelectionBB(player, bb, partialTicks);
                event.setCanceled(true);
            }
        }
    }

    public void drawSelectionBB(Entity entity, AxisAlignedBB bb, float partialTicks) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.glLineWidth(2F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        double dx = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
        double dy = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
        double dz = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
        RenderGlobal.drawSelectionBoundingBox(bb.grow(0.0020000000949949026D).offset(-dx, -dy, -dz), 0F, 0F, 0F, 0.4F);

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
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
    public void onTextRender(RenderGameOverlayEvent.Text event) {
        if (this.balance != null && this.client.currentScreen == null) {
            this.balance.render(this.client, event);
        }
    }

    @SubscribeEvent
    public void onOverlayRender(RenderGameOverlayEvent.Pre event) {
        EntityPlayerSP player = this.client.player;
        if (player != null) {
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
    protected EntityPlayer getPlayer(MessageContext context) {
        return context.side.isClient() ? this.client.player : super.getPlayer(context);
    }

    @Override
    protected AdvancedMessage handleShowScreen(MessageShowScreen message, MessageContext context) {
        EntityPlayer player = getPlayer(context);
        switch (message.getType()) {
            case TILE: {
                syncTask(context, () -> {
                    ScreenHolder screenHolder = message.getLocation().getTileEntity(ScreenHolder.class);
                    CraftLogic.showScreen(screenHolder, player, message.getExtraData());
                    player.openContainer.windowId = message.getWindowId();
                });
                break;
            }
            case ENTITY: {
                Entity entity = player.world.getEntityByID(message.getEntityId());
                if (entity instanceof ScreenHolder) {
                    syncTask(context, () -> {
                        CraftLogic.showScreen((ScreenHolder) entity, player, message.getExtraData());
                        player.openContainer.windowId = message.getWindowId();
                    });
                }
                break;
            }
        }
        return null;
    }

    @Override
    protected AdvancedMessage handleShowScriptScreen(MessageShowScriptScreen message, MessageContext context) {
        syncTask(context, () -> this.client.displayGuiScreen(
                new ScreenCustom(message.getId(), message.getInfo(), message.getScript(), message.getArgs())
        ));
        return null;
    }

    @Override
    protected AdvancedMessage handleBalance(MessageBalance message, MessageContext context) {
        syncTask(context, () -> {
            Balance oldBalance = this.balance;
            this.balance = new Balance(message.getBalance(), message.getCurrency(), message.getFormat());
            if (oldBalance != null) {
                int d = oldBalance.compareTo(this.balance);
                EntityPlayer player = getPlayer(context);
                if (d != 0) {
                    SoundEvent sound = d < 0 ? CraftSounds.BALANCE_ADD : CraftSounds.BALANCE_SUBTRACT;
                    player.playSound(sound, 1F, 0.7F + player.world.rand.nextFloat() * 0.3F);
                }
            }
        });
        return null;
    }

    @Override
    protected AdvancedMessage handleClearChat(MessageClearChat message, MessageContext context) {
        syncTask(context, () -> ProxyClient.this.client.ingameGUI.getChatGUI().clearChatMessages(message.sent));
        return null;
    }

    @Override
    protected AdvancedMessage handleClientCustom(MessageCustom message, MessageContext context) {
        if (this.client.currentScreen instanceof ScreenCustom) {
            ScreenCustom screen = (ScreenCustom) this.client.currentScreen;
            String channel = message.getChannel();
            NBTTagCompound response = screen.handlePayload(channel, message.getData());
            if (response != null) {
                return new MessageCustom(channel, response);
            }
        }
        return null;
    }

    @Override
    protected AdvancedMessage handleToast(MessageToast message, MessageContext context) {
        GuiToast toasts = this.client.getToastGui();
        toasts.add(new ToastText(message.getTitle(), message.getSubtitle(), message.getTimeout()));
        return null;
    }

    @Override
    protected AdvancedMessage handleCountdown(MessageCountdown message, MessageContext context) {
        GuiToast toasts = this.client.getToastGui();
        ToastCountdown toast = toasts.getToast(ToastCountdown.class, message.getId());
        if (toast != null) {
            toast.setCountdown(message.getTimeout());
        } else {
            toasts.add(
                new ToastCountdown(message.getId(), message.getTitle(), message.getTimeout(), message.getColor())
            );
        }
        return null;
    }

    private static class Balance implements Comparable<Balance> {
        private final float balance;
        private final String currency, format;
        private final ITextComponent text;

        private Balance(float balance, String currency, String format) {
            this.balance = balance;
            this.currency = currency;
            this.format = format;
            this.text = Text.translation("tooltip.balance")
                            .arg(String.format(this.format, this.balance))
                            .arg(this.currency)
                            .build();
        }

        public void render(Minecraft client, RenderGameOverlayEvent.Text event) {
            ScaledResolution resolution = event.getResolution();
            FontRenderer fontRenderer = client.fontRenderer;
            int x = 5;
            int y = resolution.getScaledHeight() - fontRenderer.FONT_HEIGHT - 5;
            fontRenderer.drawString(this.text.getFormattedText(), x, y, 0xFFFFFF, true);
        }

        @Override
        public int compareTo(Balance o) {
            return Float.compare(this.balance, o.balance);
        }
    }
}

