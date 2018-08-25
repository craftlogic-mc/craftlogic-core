package ru.craftlogic.mixin.client.renderer.entity;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPigZombie;
import net.minecraft.entity.monster.EntityPigZombie;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RenderPigZombie.class)
public abstract class MixinRenderPigZombie extends RenderBiped<EntityPigZombie> {
    public MixinRenderPigZombie(RenderManager renderManager, ModelBiped model, float shadowSize) {
        super(renderManager, model, shadowSize);
    }

    @Override
    protected void preRenderCallback(EntityPigZombie zombie, float p_preRenderCallback_2_) {
        float size = zombie.getRenderSizeModifier();
        GlStateManager.scale(size, size, size);
    }
}
