package ru.craftlogic.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import ru.craftlogic.common.entity.EntityWoodpecker;

import static net.minecraft.util.math.MathHelper.cos;

public class ModelWoodpecker extends ModelBase {
    private ModelRenderer leg1;
    private ModelRenderer body;
    private ModelRenderer leg2;
    private ModelRenderer beak;
    private ModelRenderer quiff;
    private ModelRenderer head;
    private ModelRenderer train;
    private ModelRenderer leftLeg;
    private ModelRenderer rightLeg;
    private ModelRenderer neck;
    private ModelRenderer rightWing;
    private ModelRenderer leftWing;

    public ModelWoodpecker() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.leg1 = new ModelRenderer(this, 0, 0);
        this.leg1.addBox(-1F, 0F, -1F, 2, 1, 2);
        this.leg1.setRotationPoint(1.5F, 20F, -1.5F);
        this.leg1.setTextureSize(64, 32);
        this.leg1.mirror = true;
        this.setRotation(this.leg1, -0.122173F, 0F, 0F);
        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-2.5F, -1F, -2F, 5, 4, 8);
        this.body.setRotationPoint(0F, 17F, -2F);
        this.body.setTextureSize(64, 32);
        this.body.mirror = true;
        this.setRotation(this.body, -0.3717861F, 0F, 0F);
        this.leg2 = new ModelRenderer(this, 0, 3);
        this.leg2.addBox(-1F, 0F, -1F, 2, 1, 2);
        this.leg2.setRotationPoint(-1.5F, 20F, -1.5F);
        this.leg2.setTextureSize(64, 32);
        this.leg2.mirror = true;
        this.setRotation(this.leg2, -0.122173F, 0F, 0F);
        this.beak = new ModelRenderer(this, 12, 12);
        this.beak.addBox(-0.5F, -3.3F, -5.5F, 1, 1, 4);
        this.beak.setRotationPoint(0F, 17.5F, -5F);
        this.beak.setTextureSize(64, 32);
        this.beak.mirror = true;
        this.setRotation(this.beak, 0.2094395F, 0F, 0F);
        this.quiff = new ModelRenderer(this, 18, -5);
        this.quiff.addBox(0F, -7F, -0.3F, 0, 4, 5);
        this.quiff.setRotationPoint(0F, 17.5F, -5F);
        this.quiff.setTextureSize(64, 32);
        this.quiff.mirror = true;
        this.setRotation(this.quiff, 0.2094395F, 0F, 0F);
        this.head = new ModelRenderer(this, 31, 10);
        this.head.addBox(-2F, -5F, -2F, 4, 3, 4);
        this.head.setRotationPoint(0F, 17.5F, -5F);
        this.head.setTextureSize(64, 32);
        this.head.mirror = true;
        this.setRotation(this.head, 0.2094395F, 0F, 0F);
        this.train = new ModelRenderer(this, 26, 0);
        this.train.addBox(-2F, 1F, 4F, 4, 2, 8);
        this.train.setRotationPoint(0F, 17F, -2F);
        this.train.setTextureSize(64, 32);
        this.train.mirror = true;
        this.setRotation(this.train, -0.1858931F, 0F, 0F);
        this.leftLeg = new ModelRenderer(this, 0, 12);
        this.leftLeg.addBox(-0.5F, 0F, -0.5F, 1, 3, 1);
        this.leftLeg.setRotationPoint(1.5F, 20.5F, -1.5F);
        this.leftLeg.setTextureSize(64, 32);
        this.leftLeg.mirror = true;
        this.setRotation(this.leftLeg, -0.0523599F, 0F, 0F);
        this.rightLeg = new ModelRenderer(this, 4, 12);
        this.rightLeg.addBox(-0.5F, 0F, -0.5F, 1, 3, 1);
        this.rightLeg.setRotationPoint(-1.5F, 20.5F, -1.5F);
        this.rightLeg.setTextureSize(64, 32);
        this.rightLeg.mirror = true;
        this.setRotation(this.rightLeg, -0.0523599F, 0F, 0F);
        this.neck = new ModelRenderer(this, 0, 25);
        this.neck.addBox(-1.5F, -1F, -5F, 3, 2, 4);
        this.neck.setRotationPoint(0F, 18.5F, -3F);
        this.neck.setTextureSize(64, 32);
        this.neck.mirror = true;
        this.setRotation(this.neck, -0.9712912F, 0F, 0F);
        this.rightWing = new ModelRenderer(this, 20, 12);
        this.rightWing.addBox(-1.5F, 0F, -2F, 1, 4, 9);
        this.rightWing.setRotationPoint(-2F, 16F, -2F);
        this.rightWing.setTextureSize(64, 32);
        this.rightWing.mirror = true;
        this.setRotation(this.rightWing, -0.148353F, 0.148353F, 0.148353F);
        this.leftWing = new ModelRenderer(this, 0, 12);
        this.leftWing.addBox(0.5F, 0F, -2F, 1, 4, 9);
        this.leftWing.setRotationPoint(2F, 16F, -2F);
        this.leftWing.setTextureSize(64, 32);
        this.leftWing.mirror = true;
        this.setRotation(this.leftWing, -0.148353F, -0.148353F, -0.148353F);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSingMod, float rotation, float deltaYaw, float pitch, float scale) {
        super.render(entity, limbSwing, limbSingMod, rotation, deltaYaw, pitch, scale);
        this.setRotationAngles(limbSwing, limbSingMod, rotation, deltaYaw, pitch, scale, entity);
        this.leg1.render(scale);
        this.body.render(scale);
        this.leg2.render(scale);
        this.beak.render(scale);
        this.quiff.render(scale);
        this.head.render(scale);
        this.train.render(scale);
        this.leftLeg.render(scale);
        this.rightLeg.render(scale);
        this.neck.render(scale);
        this.rightWing.render(scale);
        this.leftWing.render(scale);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSingMod, float rotation, float deltaYaw, float pitch, float scale, Entity entity) {
        EntityWoodpecker woodpecker = (EntityWoodpecker) entity;
        if (woodpecker.isHanging()) {
            this.leg1.setRotationPoint(1.5F, 19F, -5.6F);
            this.leg2.setRotationPoint(-1.5F, 19F, -5.6F);
            this.body.setRotationPoint(0F, 17F, -2.5F);
            this.beak.setRotationPoint(0F, 14.5F, -4F);
            this.quiff.setRotationPoint(0F, 14.5F, -4F);
            this.head.setRotationPoint(0F, 14.5F, -4F);
            this.train.setRotationPoint(0F, 17F, -2F);
            this.leftLeg.setRotationPoint(1.5F, 18.8F, -6F);
            this.rightLeg.setRotationPoint(-1.5F, 18.8F, -6F);
            this.neck.setRotationPoint(0F, 15.5F, -4F);
            this.rightWing.setRotationPoint(-2F, 17F, -1F);
            this.leftWing.setRotationPoint(2F, 17F, -1F);
            if (woodpecker.isPecking) {
                this.neck.rotateAngleX = MathHelper.cos(rotation * 2F) * (float)Math.PI * 0.1F - 1.802233F;
                this.quiff.rotateAngleX = this.neck.rotateAngleX + 1.4531671F;
                this.beak.rotateAngleX = this.quiff.rotateAngleX;
                this.head.rotateAngleX = this.quiff.rotateAngleX;
            } else {
                this.neck.rotateAngleX = -1.802233F;
                this.quiff.rotateAngleX = -0.3490659F;
                this.beak.rotateAngleX = this.quiff.rotateAngleX;
                this.head.rotateAngleX = this.quiff.rotateAngleX;
            }

            this.leg1.rotateAngleX = -1.692969F;
            this.leg2.rotateAngleX = -1.692969F;
            this.body.rotateAngleX = -1.788601F;
            this.train.rotateAngleX = -1.745329F;
            this.leftLeg.rotateAngleX = -2.094395F;
            this.rightLeg.rotateAngleX = -2.094395F;
            this.rightWing.rotateAngleX = -1.719149F;
            this.rightWing.rotateAngleY = 0.148353F;
            this.rightWing.rotateAngleZ = -0.148353F;
            this.leftWing.rotateAngleX = -1.719149F;
            this.leftWing.rotateAngleY = -0.148353F;
            this.leftWing.rotateAngleZ = 0.148353F;
        } else {
            this.leg1.setRotationPoint(1.5F, 20F, -1.5F);
            this.leg2.setRotationPoint(-1.5F, 20F, -1.5F);
            this.body.setRotationPoint(0F, 17F, -2F);
            this.beak.setRotationPoint(0F, 17.5F, -5F);
            this.quiff.setRotationPoint(0F, 17.5F, -5F);
            this.head.setRotationPoint(0F, 17.5F, -5F);
            this.train.setRotationPoint(0F, 17F, -2F);
            this.leftLeg.setRotationPoint(1.5F, 20.5F, -1.5F);
            this.rightLeg.setRotationPoint(-1.5F, 20.5F, -1.5F);
            this.neck.setRotationPoint(0F, 18.5F, -3F);
            this.rightWing.setRotationPoint(-2F, 16F, -2F);
            this.leftWing.setRotationPoint(2F, 16F, -2F);
            this.neck.rotateAngleX = -0.9712912F;
            this.quiff.rotateAngleX = 0.2094395F;
            this.beak.rotateAngleX = 0.2094395F;
            this.head.rotateAngleX = 0.2094395F;
            if (!woodpecker.isFlying()) {
                this.rightLeg.rotateAngleX = cos(limbSwing * 0.6662F) * 1.4F * limbSingMod;
                this.leftLeg.rotateAngleX = cos((float) (limbSwing * 0.6662F + Math.PI)) * 1.4F * limbSingMod;
            } else {
                this.leftLeg.rotateAngleX = -0.0523599F;
                this.rightLeg.rotateAngleX = -0.0523599F;
            }
            this.leg1.rotateAngleX = -0.122173F;
            this.leg2.rotateAngleX = -0.122173F;
            this.body.rotateAngleX = -0.3717861F;
            this.train.rotateAngleX = -0.1858931F;
            this.rightWing.rotateAngleX = -0.148353F;
            this.rightWing.rotateAngleY = 0.148353F;
            this.rightWing.rotateAngleZ = 0.148353F;
            this.leftWing.rotateAngleX = -0.148353F;
            this.leftWing.rotateAngleY = -0.148353F;
            this.leftWing.rotateAngleZ = -0.148353F;
            this.rightWing.rotateAngleZ = rotation;
            this.leftWing.rotateAngleZ = -rotation;
        }
    }
}
