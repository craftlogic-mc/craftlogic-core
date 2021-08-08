package ru.craftlogic.mixin.client.renderer.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelZombieVillager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ru.craftlogic.api.entity.Zombie;

@Mixin(ModelZombieVillager.class)
public abstract class MixinModelZombieVillager extends ModelBiped {
    @Override
    public void setLivingAnimations(EntityLivingBase entity, float p_setLivingAnimations_2_, float p_setLivingAnimations_3_, float p_setLivingAnimations_4_) {
        this.rightArmPose = ArmPose.EMPTY;
        this.leftArmPose = ArmPose.EMPTY;
        ItemStack heldItem = entity.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem.getItem() == Items.BOW && ((Zombie)entity).isSwingingArms()) {
            if (entity.getPrimaryHand() == EnumHandSide.RIGHT) {
                this.rightArmPose = ArmPose.BOW_AND_ARROW;
            } else {
                this.leftArmPose = ArmPose.BOW_AND_ARROW;
            }
        }

        super.setLivingAnimations(entity, p_setLivingAnimations_2_, p_setLivingAnimations_3_, p_setLivingAnimations_4_);
    }

    /**
     * @author Radviger
     * @reason Custom zombie features
     */
    @Overwrite
    public void setRotationAngles(float limbSwing, float limbSwingMod, float rotation, float deltaYaw, float pitch, float scale, Entity entity) {
        super.setRotationAngles(limbSwing, limbSwingMod, rotation, deltaYaw, pitch, scale, entity);
        ItemStack heldItem = ((EntityLivingBase)entity).getHeldItemMainhand();
        if (entity instanceof Zombie) {
            Zombie zombie = (Zombie)entity;
            if (zombie.isSwingingArms() && (heldItem.isEmpty() || heldItem.getItem() != Items.BOW)) {
                float lvt_10_1_ = MathHelper.sin(this.swingProgress * 3.1415927F);
                float lvt_11_1_ = MathHelper.sin((1F - (1F - this.swingProgress) * (1F - this.swingProgress)) * 3.1415927F);
                this.bipedRightArm.rotateAngleZ = 0F;
                this.bipedLeftArm.rotateAngleZ = 0F;
                this.bipedRightArm.rotateAngleY = -(0.1F - lvt_10_1_ * 0.6F);
                this.bipedLeftArm.rotateAngleY = 0.1F - lvt_10_1_ * 0.6F;
                this.bipedRightArm.rotateAngleX = -1.5707964F;
                this.bipedLeftArm.rotateAngleX = -1.5707964F;
                this.bipedRightArm.rotateAngleX -= lvt_10_1_ * 1.2F - lvt_11_1_ * 0.4F;
                this.bipedLeftArm.rotateAngleX -= lvt_10_1_ * 1.2F - lvt_11_1_ * 0.4F;
                this.bipedRightArm.rotateAngleZ += MathHelper.cos(rotation * 0.09F) * 0.05F + 0.05F;
                this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(rotation * 0.09F) * 0.05F + 0.05F;
                this.bipedRightArm.rotateAngleX += MathHelper.sin(rotation * 0.067F) * 0.05F;
                this.bipedLeftArm.rotateAngleX -= MathHelper.sin(rotation * 0.067F) * 0.05F;
            }
        }
    }

    @Override
    public void postRenderArm(float partialTicks, EnumHandSide side) {
        float point = side == EnumHandSide.RIGHT ? 1F : -1F;
        ModelRenderer arm = this.getArmForSide(side);
        arm.rotationPointX += point;
        arm.postRender(partialTicks);
        arm.rotationPointX -= point;
    }
}
