package ru.craftlogic.client.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.util.ResourceLocation;
import ru.craftlogic.api.entity.Chicken;
import ru.craftlogic.client.render.model.ModelChicken;

import static net.minecraft.util.math.MathHelper.sin;
import static ru.craftlogic.api.CraftAPI.MOD_ID;

public class RenderChicken extends RenderLiving<EntityChicken> {
    private static final ResourceLocation CHILD_TEXTURE = new ResourceLocation(MOD_ID, "textures/entity/chicken/chick.png");

    public RenderChicken(RenderManager renderManager) {
        super(renderManager, new ModelChicken(), 0.3F);
    }

    @Override
    protected void preRenderCallback(EntityChicken chicken, float p_preRenderCallback_2_) {
        if (chicken.isChild()) {
            GlStateManager.scale(0.7F, 0.7F, 0.7F);
        } else {
            GlStateManager.scale(1.3F, 1.3F, 1.3F);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityChicken chicken) {
        return chicken.isChild() ? CHILD_TEXTURE : ((Chicken) chicken).getVariant().getTexture();
    }

    @Override
    protected float handleRotationFloat(EntityChicken chicken, float deltaTime) {
        float flap = chicken.oFlap + (chicken.wingRotation - chicken.oFlap) * deltaTime;
        float flapSpeed = chicken.oFlapSpeed + (chicken.destPos - chicken.oFlapSpeed) * deltaTime;
        return (sin(flap) + 1F) * flapSpeed;
    }
}