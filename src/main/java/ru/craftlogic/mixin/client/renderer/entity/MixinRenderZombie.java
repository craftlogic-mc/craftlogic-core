package ru.craftlogic.mixin.client.renderer.entity;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import ru.craftlogic.api.entity.Zombie;

@Mixin(RenderZombie.class)
public abstract class MixinRenderZombie extends RenderBiped<EntityZombie> {
    public MixinRenderZombie(RenderManager renderManager, ModelBiped model, float shadowSize) {
        super(renderManager, model, shadowSize);
    }

    @Override
    protected void preRenderCallback(EntityZombie zombie, float p_preRenderCallback_2_) {
        float size = zombie.getRenderSizeModifier();
        GlStateManager.scale(size, size, size);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityZombie zombie) {
        return ((Zombie)zombie).getVariant().getTexture();
    }
}
