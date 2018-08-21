package ru.craftlogic.client.render.entity.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.client.render.entity.RenderSpider;

@SideOnly(Side.CLIENT)
public class LayerSpiderEyes<S extends EntitySpider> implements LayerRenderer<S> {
    private static final ResourceLocation SPIDER_EYES = new ResourceLocation("textures/entity/spider_eyes.png");
    private final RenderSpider<S> renderer;

    public LayerSpiderEyes(RenderSpider<S> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(S spider, float p_doRenderLayer_2_, float p_doRenderLayer_3_, float p_doRenderLayer_4_, float p_doRenderLayer_5_, float p_doRenderLayer_6_, float p_doRenderLayer_7_, float p_doRenderLayer_8_) {
        this.renderer.bindTexture(SPIDER_EYES);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        if (spider.isInvisible()) {
            GlStateManager.depthMask(false);
        } else {
            GlStateManager.depthMask(true);
        }

        int tex = '\uf0f0';
        int x = tex % 65536;
        int y = tex / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)x, (float)y);
        GlStateManager.color(1F, 1F, 1F, 1F);
        Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
        this.renderer.getMainModel().render(spider, p_doRenderLayer_2_, p_doRenderLayer_3_, p_doRenderLayer_5_, p_doRenderLayer_6_, p_doRenderLayer_7_, p_doRenderLayer_8_);
        Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
        int brightness = spider.getBrightnessForRender();
        x = brightness % 65536;
        y = brightness / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)x, (float)y);
        this.renderer.setLightmap(spider);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }

    public boolean shouldCombineTextures() {
        return false;
    }
}

