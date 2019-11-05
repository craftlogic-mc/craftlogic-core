package ru.craftlogic.mixin.client.renderer.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelParrot;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelParrot.class)
public class MixinModelParrot extends ModelBase {
    @Shadow
    private ModelRenderer head;

    /**
     * @author Radviger
     * @reason Proper bird looking animation at a right angle (by their eye, not beak)
     */
    @Inject(method = "setRotationAngles", at = @At(value = "FIELD", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/model/ModelRenderer;rotateAngleY:F", ordinal = 0))
    public void onHeadAngleYSet(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity parrot, CallbackInfo ci) {
        if (parrot.getEntityId() % 2 == 0) {
            netHeadYaw += 90F;
        } else {
            netHeadYaw -= 90F;
        }
        this.head.rotateAngleY = (float) Math.toRadians(netHeadYaw);
    }
}
