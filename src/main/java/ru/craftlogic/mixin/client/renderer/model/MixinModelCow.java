package ru.craftlogic.mixin.client.renderer.model;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelCow;
import net.minecraft.client.model.ModelQuadruped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.entity.Cow;
import ru.craftlogic.api.model.AdvancedModelBox;

@Mixin(ModelCow.class)
public class MixinModelCow extends ModelQuadruped {
    private float headRotationAngleX;
    private ModelBox udder;

    public MixinModelCow() {
        super(12, 0F);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructor(CallbackInfo info) {
        this.udder = new ModelBox(this.body, 52, 0, -2F, 2F, -8F, 4, 6, 1, 0F);
        this.body.cubeList.add(this.udder);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelRenderer;addBox(FFFIIIF)V"))
    public void addBox(ModelRenderer part, float a, float b, float c, int d, int e, int f, float g) {
        if (part != this.body || a != -2F && b != -2F && c != -8F) {
            part.addBox(a, b, c, d, e, f, g);
        }
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entity, float p_setLivingAnimations_2_, float p_setLivingAnimations_3_, float p) {
        super.setLivingAnimations(entity, p_setLivingAnimations_2_, p_setLivingAnimations_3_, p);
        this.head.rotationPointY = 4F + ((Cow)entity).getHeadRotationPointY(p) * 9F;
        this.headRotationAngleX = ((Cow)entity).getHeadRotationAngleX(p);
    }

    @Override
    public void setRotationAngles(float p_setRotationAngles_1_, float p_setRotationAngles_2_, float p_setRotationAngles_3_, float p_setRotationAngles_4_, float p_setRotationAngles_5_, float p_setRotationAngles_6_, Entity p_setRotationAngles_7_) {
        super.setRotationAngles(p_setRotationAngles_1_, p_setRotationAngles_2_, p_setRotationAngles_3_, p_setRotationAngles_4_, p_setRotationAngles_5_, p_setRotationAngles_6_, p_setRotationAngles_7_);
        this.head.rotateAngleX = this.headRotationAngleX;
    }

    @Override
    public void render(Entity cow, float p_render_2_, float p_render_3_, float p_render_4_, float p_render_5_, float p_render_6_, float p_render_7_) {
        ((AdvancedModelBox)this.udder).setVisible(((Cow)cow).hasMilk());
        super.render(cow, p_render_2_, p_render_3_, p_render_4_, p_render_5_, p_render_6_, p_render_7_);
    }
}
