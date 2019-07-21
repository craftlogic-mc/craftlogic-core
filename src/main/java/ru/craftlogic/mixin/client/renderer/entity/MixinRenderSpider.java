package ru.craftlogic.mixin.client.renderer.entity;

import net.minecraft.client.model.ModelSpider;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSpider;
import net.minecraft.entity.monster.EntitySpider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RenderSpider.class)
public abstract class MixinRenderSpider<T extends EntitySpider> extends RenderLiving<T> {
    public MixinRenderSpider(RenderManager renderManager) {
        super(renderManager, new ModelSpider(), 1F);
    }

    @Override
    protected void preRenderCallback(T spider, float p_preRenderCallback_2_) {
        float size = spider.getRenderSizeModifier();
        GlStateManager.scale(size, size, size);
    }
}
