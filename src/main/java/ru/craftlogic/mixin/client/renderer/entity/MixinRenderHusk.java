package ru.craftlogic.mixin.client.renderer.entity;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderHusk;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ru.craftlogic.api.entity.Zombie;

@Mixin(RenderHusk.class)
public abstract class MixinRenderHusk extends RenderBiped<EntityZombie> {
    public MixinRenderHusk(RenderManager renderManager, ModelBiped model, float shadowSize) {
        super(renderManager, model, shadowSize);
    }

    @Overwrite
    protected void preRenderCallback(EntityZombie husk, float p_preRenderCallback_2_) {
        float size = husk.getRenderSizeModifier();
        GlStateManager.scale(1.0625F * size, 1.0625F * size, 1.0625F * size);
    }
}
