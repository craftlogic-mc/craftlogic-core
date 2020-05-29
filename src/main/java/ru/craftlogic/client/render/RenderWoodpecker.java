package ru.craftlogic.client.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import ru.craftlogic.client.model.ModelWoodpecker;
import ru.craftlogic.common.entity.EntityWoodpecker;

import static ru.craftlogic.api.CraftAPI.MOD_ID;

public class RenderWoodpecker extends RenderLiving<EntityWoodpecker> {
    private static final ResourceLocation textureGreater = new ResourceLocation(MOD_ID, "textures/entity/woodpecker/greater.png");
    private static final ResourceLocation textureGreen = new ResourceLocation(MOD_ID, "textures/entity/woodpecker/green.png");
    private static final ResourceLocation texturePileated = new ResourceLocation(MOD_ID, "textures/entity/woodpecker/pileated.png");

    public RenderWoodpecker(RenderManager entityRenderManager) {
        super(entityRenderManager, new ModelWoodpecker(), 0.3F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityWoodpecker entity) {
        switch (entity.getVariant()) {
            case GREEN:
                return textureGreen;
            case PILEATED:
                return texturePileated;
            default:
                return textureGreater;
        }
    }

    @Override
    protected void applyRotations(EntityWoodpecker woodpecker, float ageInTicks, float rotationYaw, float partialTicks) {
        if (woodpecker.isHanging()) {
            GlStateManager.translate(0F, -0.1F, 0F);
            BlockPos treePos = woodpecker.getTreePos();
            float yaw = (float) (Math.toDegrees(Math.atan2((int)woodpecker.posZ - treePos.getZ(), (int)woodpecker.posX - treePos.getX())) + 90);
            rotationYaw = MathHelper.wrapDegrees(yaw);
        } else {
            GlStateManager.translate(0F, MathHelper.cos(ageInTicks * 0.3F) * 0.1F, 0F);
        }

        super.applyRotations(woodpecker, ageInTicks, rotationYaw, partialTicks);
    }

    @Override
    protected float handleRotationFloat(EntityWoodpecker woodpecker, float partialTicks) {
        float f1 = woodpecker.flap + (woodpecker.wingRotation - woodpecker.flap) * partialTicks;
        float f2 = woodpecker.flapSpeed + (woodpecker.destPos - woodpecker.flapSpeed) * partialTicks;
        return (MathHelper.sin(f1) + 1F) * f2;
    }
}
