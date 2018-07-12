package ru.craftlogic.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.block.Colored;
import ru.craftlogic.api.item.ItemBlockBase;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.client.render.RenderChicken;
import ru.craftlogic.client.render.RenderThrownItem;
import ru.craftlogic.common.ProxyCommon;
import ru.craftlogic.common.entity.EntityThrownItem;

import static ru.craftlogic.CraftLogic.registerEntityRenderer;

@SideOnly(Side.CLIENT)
public class ProxyClient extends ProxyCommon {
    protected static final AxisAlignedBB SLAB_BOTTOM_BOUNDING = new AxisAlignedBB(0, 0, 0, 1, 0.5, 1);
    protected static final AxisAlignedBB SLAB_TOP_BOUNDING = new AxisAlignedBB(0, 0.5, 0, 1, 1, 1);

    private final ModelManager modelManager = new ModelManager();

    @Override
    public void preInit() {
        super.preInit();
        registerEntityRenderer(EntityThrownItem.class, RenderThrownItem::new);
        registerEntityRenderer(EntityChicken.class, RenderChicken::new);
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
    public void onBlockColorRegister(ColorHandlerEvent.Item event) {
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
}
