package ru.craftlogic.client.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCaveSpider extends RenderSpider<EntityCaveSpider> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/spider/cave_spider.png");

    public RenderCaveSpider(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize *= 0.7F;
    }

    @Override
    protected void preRenderCallback(EntityCaveSpider spider, float p_preRenderCallback_2_) {
        GlStateManager.scale(0.7F, 0.7F, 0.7F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCaveSpider spider) {
        return TEXTURE;
    }
}
