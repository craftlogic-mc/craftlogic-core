package ru.craftlogic.client.render;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.util.ResourceLocation;
import ru.craftlogic.api.entity.Bird;
import ru.craftlogic.client.render.model.ModelChicken;
import ru.craftlogic.common.entity.ChickenVariant;

import static net.minecraft.util.math.MathHelper.sin;
import static ru.craftlogic.CraftLogic.MODID;

public class RenderChicken extends RenderLiving<EntityChicken> {
    private static final ResourceLocation chickTextures = new ResourceLocation(MODID, "textures/entity/chicken/chick.png");

    public RenderChicken(RenderManager renderManager) {
        super(renderManager, new ModelChicken(), 0.3F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityChicken chicken) {
        return chicken.isChild() ? chickTextures : ((Bird<ChickenVariant>) chicken).getVariant().getTexture();
    }

    @Override
    protected float handleRotationFloat(EntityChicken chicken, float deltaTime) {
        float flap = chicken.oFlap + (chicken.wingRotation - chicken.oFlap) * deltaTime;
        float flapSpeed = chicken.oFlapSpeed + (chicken.destPos - chicken.oFlapSpeed) * deltaTime;
        return (sin(flap) + 1F) * flapSpeed;
    }
}