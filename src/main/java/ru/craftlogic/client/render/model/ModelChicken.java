package ru.craftlogic.client.render.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

import static net.minecraft.util.math.MathHelper.cos;

public class ModelChicken extends ModelBase {
    private ModelRenderer rightLeg, leftLeg;
    private ModelRenderer head;
    private ModelRenderer wattleAndComb;
    private ModelRenderer beak;
    private ModelRenderer breast;
    private ModelRenderer rump;
    private ModelRenderer tail;
    private ModelRenderer rightWing, leftWing;

    private ModelRenderer chickHead;
    private ModelRenderer chickBill;
    private ModelRenderer chickBody;
    private ModelRenderer chickRightLeg, chickLeftLeg;

    public ModelChicken() {
        this.textureWidth = 64;
        this.textureHeight = 32;

        this.rightLeg = new ModelRenderer(this, 7, 25);
        this.rightLeg.addBox(-0.5F, 0F, -0.5F, 1, 2, 1);
        this.rightLeg.setRotationPoint(-1F, 22F, -2F);
        this.rightLeg.setTextureSize(64, 32);
        this.rightLeg.mirror = true;
        this.setRotation(rightLeg, 0F, 0F, 0F);
        this.leftLeg = new ModelRenderer(this, 7, 25);
        this.leftLeg.addBox(-0.5F, 0F, -0.5F, 1, 2, 1);
        this.leftLeg.setRotationPoint(1F, 22F, -2F);
        this.leftLeg.setTextureSize(64, 32);
        this.leftLeg.mirror = true;
        this.setRotation(leftLeg, 0F, 0F, 0F);
        this.leftLeg.mirror = false;
        this.head = new ModelRenderer(this, 0, 0);
        this.head.addBox(-1F, -5F, -1F, 2, 5, 2);
        this.head.setRotationPoint(0F, 20F, -5F);
        this.head.setTextureSize(64, 32);
        this.head.mirror = true;
        this.setRotation(head, 0F, 0F, 0F);
        this.wattleAndComb = new ModelRenderer(this, 10, -5);
        this.wattleAndComb.addBox(0F, -6.5F, -2F, 0, 6, 5);
        this.wattleAndComb.setRotationPoint(0F, 19.5F, -5F);
        this.wattleAndComb.setTextureSize(64, 32);
        this.wattleAndComb.mirror = true;
        this.setRotation(wattleAndComb, 0F, 0F, 0F);
        this.beak = new ModelRenderer(this, 6, 0);
        this.beak.addBox(-0.5F, -4F, -2F, 1, 1, 1);
        this.beak.setRotationPoint(0F, 20F, -5F);
        this.beak.setTextureSize(64, 32);
        this.beak.mirror = true;
        this.setRotation(beak, 0F, 0F, 0F);
        this.breast = new ModelRenderer(this, 0, 8);
        this.breast.addBox(-2F, -2F, 0F, 4, 5, 6);
        this.breast.setRotationPoint(0F, 19F, -5F);
        this.breast.setTextureSize(64, 32);
        this.breast.mirror = true;
        this.setRotation(breast, 0F, 0F, 0F);
        this.rump = new ModelRenderer(this, 0, 19);
        this.rump.addBox(-1.5F, -1.5F, 0F, 3, 3, 2);
        this.rump.setRotationPoint(0F, 18.5F, 1F);
        this.rump.setTextureSize(64, 32);
        this.rump.mirror = true;
        this.setRotation(rump, 0F, 0F, 0F);
        this.tail = new ModelRenderer(this, 14, 0);
        this.tail.addBox(0F, -5.5F, 0F, 0, 7, 7);
        this.tail.setRotationPoint(0F, 18.5F, 1F);
        this.tail.setTextureSize(64, 32);
        this.tail.mirror = true;
        this.setRotation(tail, 0F, 0F, 0F);
        this.rightWing = new ModelRenderer(this, 0, 25);
        this.rightWing.addBox(-1F, 0F, -2F, 1, 3, 4);
        this.rightWing.setRotationPoint(-2F, 18F, -2F);
        this.rightWing.setTextureSize(64, 32);
        this.rightWing.mirror = true;
        this.setRotation(rightWing, 0F, 0F, 0F);
        this.leftWing = new ModelRenderer(this, 0, 25);
        this.leftWing.addBox(0F, 0F, -2F, 1, 3, 4);
        this.leftWing.setRotationPoint(2F, 18F, -2F);
        this.leftWing.setTextureSize(64, 32);
        this.leftWing.mirror = true;
        this.setRotation(leftWing, 0F, 0F, 0F);
        this.leftWing.mirror = false;
        this.chickHead = new ModelRenderer(this, 0, 0);
        this.chickHead.addBox(-1F, -1.5F, -1.5F, 2, 3, 3, 0F);
        this.chickHead.setRotationPoint(0F, 18F, -2.5F);
        this.chickBill = new ModelRenderer(this, 12, 3);
        this.chickBill.addBox(-0.5F, 0.5F, -2.5F, 1, 1, 1, 0F);
        this.chickBill.setRotationPoint(0F, 18F, -2.5F);
        this.chickBody = new ModelRenderer(this, 0, 6);
        this.chickBody.addBox(-2F, -2.5F, -1.5F, 4, 4, 5, 0F);
        this.chickBody.setRotationPoint(0F, 20F, 0F);
        this.chickRightLeg = new ModelRenderer(this, 12, 0);
        this.chickRightLeg.addBox(-0.5F, 0F, -1F, 1, 2, 1, 0F);
        this.chickRightLeg.setRotationPoint(-1F, 22F, 0.5F);
        this.chickLeftLeg = new ModelRenderer(this, 12, 0);
        this.chickLeftLeg.addBox(-0.5F, 0F, -1F, 1, 2, 1, 0F);
        this.chickLeftLeg.setRotationPoint(1F, 22F, 0.5F);
    }

    @Override
    public void render(Entity chicken, float limbSwing, float limbSingMod, float rotation, float deltaYaw, float pitch, float scale) {
        if (this.isChild) {
            this.setRotationAngles(limbSwing, limbSingMod, rotation, deltaYaw, pitch, scale, chicken);
            this.chickHead.render(scale);
            this.chickBill.render(scale);
            this.chickBody.render(scale);
            this.chickRightLeg.render(scale);
            this.chickLeftLeg.render(scale);
        } else {
            this.setRotationAngles(limbSwing, limbSingMod, rotation, deltaYaw, pitch, scale, chicken);
            this.rightLeg.render(scale);
            this.leftLeg.render(scale);
            this.head.render(scale);
            this.wattleAndComb.render(scale);
            this.beak.render(scale);
            this.breast.render(scale);
            this.rump.render(scale);
            this.tail.render(scale);
            this.rightWing.render(scale);
            this.leftWing.render(scale);
        }
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSingMod, float rotation, float yaw, float pitch, float scale, Entity chicken) {
        super.setRotationAngles(limbSwing, limbSingMod, rotation, yaw, pitch, scale, chicken);
        if (chicken.getEntityId() % 2 == 0) {
            yaw += 90F;
        } else {
            yaw -= 90F;
        }
        this.head.rotateAngleX = (float) Math.toRadians(pitch);
        this.head.rotateAngleY = (float) Math.toRadians(yaw);
        this.rightLeg.rotateAngleX = cos(limbSwing * 0.6662F) * 1.4F * limbSingMod;
        this.leftLeg.rotateAngleX = cos((float) (limbSwing * 0.6662F + Math.PI)) * 1.4F * limbSingMod;
        this.rightWing.rotateAngleZ = rotation;
        this.leftWing.rotateAngleZ = -rotation;
        this.wattleAndComb.rotateAngleX = this.head.rotateAngleX;
        this.wattleAndComb.rotateAngleY = this.head.rotateAngleY;
        this.beak.rotateAngleX = this.head.rotateAngleX;
        this.beak.rotateAngleY = this.head.rotateAngleY;
        this.chickHead.rotateAngleX = this.head.rotateAngleX;
        this.chickHead.rotateAngleY = this.head.rotateAngleY;
        this.chickBill.rotateAngleX = this.beak.rotateAngleX;
        this.chickBill.rotateAngleY = this.beak.rotateAngleY;
        this.chickRightLeg.rotateAngleX = this.rightLeg.rotateAngleX;
        this.chickLeftLeg.rotateAngleX = this.leftLeg.rotateAngleX;
    }
}
