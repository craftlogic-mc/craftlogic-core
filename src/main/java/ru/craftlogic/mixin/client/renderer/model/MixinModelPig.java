package ru.craftlogic.mixin.client.renderer.model;

import net.minecraft.client.model.ModelPig;
import net.minecraft.client.model.ModelQuadruped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import ru.craftlogic.api.entity.Pig;

@Mixin(ModelPig.class)
public class MixinModelPig extends ModelQuadruped {
    private float headRotationAngleX;

    public MixinModelPig() {
        super(6, 0F);
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entity, float p_setLivingAnimations_2_, float p_setLivingAnimations_3_, float p) {
        super.setLivingAnimations(entity, p_setLivingAnimations_2_, p_setLivingAnimations_3_, p);
        if (entity instanceof Pig) {
            this.head.rotationPointY = 12F + ((Pig)entity).getHeadRotationPointY(p) * 9F;
            this.headRotationAngleX = ((Pig)entity).getHeadRotationAngleX(p);
        }
    }

    @Override
    public void setRotationAngles(float p_setRotationAngles_1_, float p_setRotationAngles_2_, float p_setRotationAngles_3_, float p_setRotationAngles_4_, float p_setRotationAngles_5_, float p_setRotationAngles_6_, Entity p_setRotationAngles_7_) {
        super.setRotationAngles(p_setRotationAngles_1_, p_setRotationAngles_2_, p_setRotationAngles_3_, p_setRotationAngles_4_, p_setRotationAngles_5_, p_setRotationAngles_6_, p_setRotationAngles_7_);
        this.head.rotateAngleX = this.headRotationAngleX;
    }
}
