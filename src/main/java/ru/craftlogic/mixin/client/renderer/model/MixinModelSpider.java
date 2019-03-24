package ru.craftlogic.mixin.client.renderer.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSpider;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.util.math.MathHelper.*;

@Mixin(ModelSpider.class)
public class MixinModelSpider extends ModelBase {
    private ModelRenderer head, neck, body;
    private ModelRenderer[] legs = new ModelRenderer[8];
    private ModelRenderer[] sleg = new ModelRenderer[2];

    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructor(CallbackInfo ci) {
        this.boxList.clear();
        float var1 = 0F;
        byte var2 = 15;
        this.head = new ModelRenderer(this, 32, 4);
        this.head.addBox(-4F, -4F, -8F, 8, 8, 8, var1);
        this.head.setRotationPoint(0F, (float) var2, -3F);
        this.neck = new ModelRenderer(this, 0, 0);
        this.neck.addBox(-3F, -3F, -3F, 6, 6, 6, var1);
        this.neck.setRotationPoint(0F, (float) var2, 0F);
        this.body = new ModelRenderer(this, 0, 12);
        this.body.addBox(-5F, -4F, -6F, 10, 8, 12, var1);
        this.body.setRotationPoint(0F, (float) var2, 9F);
        this.legs[0] = new ModelRenderer(this, 18, 0);
        this.legs[0].addBox(-7F, -1F, -1F, 8, 2, 2, var1);
        this.legs[0].setRotationPoint(-4F, (float) var2, 2F);
        this.legs[1] = new ModelRenderer(this, 18, 0);
        this.legs[1].addBox(-1F, -1F, -1F, 8, 2, 2, var1);
        this.legs[1].setRotationPoint(4F, (float) var2, 2F);
        this.legs[2] = new ModelRenderer(this, 18, 0);
        this.legs[2].addBox(-7F, -1F, -1F, 8, 2, 2, var1);
        this.legs[2].setRotationPoint(-4F, (float) var2, 1F);
        this.legs[3] = new ModelRenderer(this, 18, 0);
        this.legs[3].addBox(-1F, -1F, -1F, 8, 2, 2, var1);
        this.legs[3].setRotationPoint(4F, (float) var2, 1F);
        this.legs[4] = new ModelRenderer(this, 18, 0);
        this.legs[4].addBox(-7F, -1F, -1F, 8, 2, 2, var1);
        this.legs[4].setRotationPoint(-4F, (float) var2, 0F);
        this.legs[5] = new ModelRenderer(this, 18, 0);
        this.legs[5].addBox(-1F, -1F, -1F, 8, 2, 2, var1);
        this.legs[5].setRotationPoint(4F, (float) var2, 0F);
        this.legs[6] = new ModelRenderer(this, 18, 0);
        this.legs[6].addBox(-7F, -1F, -1F, 8, 2, 2, var1);
        this.legs[6].setRotationPoint(-4F, (float) var2, -1F);
        this.legs[7] = new ModelRenderer(this, 18, 0);
        this.legs[7].addBox(-1F, -1F, -1F, 8, 2, 2, var1);
        this.legs[7].setRotationPoint(4F, (float) var2, -1F);
        this.sleg[0] = new ModelRenderer(this, 24, 0);
        this.sleg[0].addBox(-9F, -1F, -1F, 10, 2, 2, var1);
        this.sleg[0].setRotationPoint(-7F, 0.5F, 0F);
        this.sleg[0].rotateAngleZ = -1.05F;
        this.sleg[1] = new ModelRenderer(this, 24, 0);
        this.sleg[1].addBox(-1F, -1F, -1F, 10, 2, 2, var1);
        this.sleg[1].setRotationPoint(7F, 0.5F, 0F);
        this.sleg[1].rotateAngleZ = 1.05F;
        this.legs[0].addChild(this.sleg[0]);
        this.legs[1].addChild(this.sleg[1]);
        this.legs[2].addChild(this.sleg[0]);
        this.legs[3].addChild(this.sleg[1]);
        this.legs[4].addChild(this.sleg[0]);
        this.legs[5].addChild(this.sleg[1]);
        this.legs[6].addChild(this.sleg[0]);
        this.legs[7].addChild(this.sleg[1]);
    }

    /**
     * @author Radviger
     * @reason Custom spider model
     */
    @Overwrite
    public void render(Entity entity, float limbSwing, float limbSingMod, float rotation, float deltaYaw, float pitch, float scale) {
        this.setRotationAngles(limbSwing, limbSingMod, rotation, deltaYaw, pitch, scale, entity);
        this.head.render(scale);
        this.neck.render(scale);
        this.body.render(scale);
        this.legs[0].render(scale);
        this.legs[1].render(scale);
        this.legs[2].render(scale);
        this.legs[3].render(scale);
        this.legs[4].render(scale);
        this.legs[5].render(scale);
        this.legs[6].render(scale);
        this.legs[7].render(scale);
    }

    /**
     * @author Radviger
     * @reason Custom spider model
     */
    @Overwrite
    public void setRotationAngles(float limbSwing, float limbSingMod, float rotation, float yaw, float pitch, float scale, Entity spider) {
        this.head.rotateAngleY = (float) Math.toRadians(yaw);
        this.head.rotateAngleX = (float) Math.toRadians(pitch);
        float var8 = 0.7853982F;
        this.legs[0].rotateAngleZ = -var8;
        this.legs[1].rotateAngleZ = var8;
        this.legs[2].rotateAngleZ = -var8 * 0.74F;
        this.legs[3].rotateAngleZ = var8 * 0.74F;
        this.legs[4].rotateAngleZ = -var8 * 0.74F;
        this.legs[5].rotateAngleZ = var8 * 0.74F;
        this.legs[6].rotateAngleZ = -var8;
        this.legs[7].rotateAngleZ = var8;
        float var9 = -0F;
        float var10 = 0.3926991F;
        this.legs[0].rotateAngleY = var10 * 2F + var9;
        this.legs[1].rotateAngleY = -var10 * 2F - var9;
        this.legs[2].rotateAngleY = var10 * 1F + var9;
        this.legs[3].rotateAngleY = -var10 * 1F - var9;
        this.legs[4].rotateAngleY = -var10 * 1F + var9;
        this.legs[5].rotateAngleY = var10 * 1F - var9;
        this.legs[6].rotateAngleY = -var10 * 2F + var9;
        this.legs[7].rotateAngleY = var10 * 2F - var9;
        float var11 = -(cos(limbSwing * 0.6662F * 2F + 0F) * 0.4F) * limbSingMod;
        float var12 = -(cos((float) (limbSwing * 0.6662F * 2F + Math.PI)) * 0.4F) * limbSingMod;
        float var13 = -(cos((float) (limbSwing * 0.6662F * 2F + Math.PI / 2F)) * 0.4F) * limbSingMod;
        float var14 = -(cos(limbSwing * 0.6662F * 2F + 4.712389F) * 0.4F) * limbSingMod;
        float var15 = abs(sin(limbSwing * 0.6662F + 0F) * 0.4F) * limbSingMod;
        float var16 = abs(sin((float) (limbSwing * 0.6662F + Math.PI)) * 0.4F) * limbSingMod;
        float var17 = abs(sin((float) (limbSwing * 0.6662F + Math.PI / 2F)) * 0.4F) * limbSingMod;
        float var18 = abs(sin(limbSwing * 0.6662F + 4.712389F) * 0.4F) * limbSingMod;
        this.legs[0].rotateAngleY += var11;
        this.legs[1].rotateAngleY += -var11;
        this.legs[2].rotateAngleY += var12;
        this.legs[3].rotateAngleY += -var12;
        this.legs[4].rotateAngleY += var13;
        this.legs[5].rotateAngleY += -var13;
        this.legs[6].rotateAngleY += var14;
        this.legs[7].rotateAngleY += -var14;
        this.legs[0].rotateAngleZ += var15 + 0.675F;
        this.legs[1].rotateAngleZ += -var15 - 0.675F;
        this.legs[2].rotateAngleZ += var16 + 0.675F;
        this.legs[3].rotateAngleZ += -var16 - 0.675F;
        this.legs[4].rotateAngleZ += var17 + 0.675F;
        this.legs[5].rotateAngleZ += -var17 - 0.675F;
        this.legs[6].rotateAngleZ += var18 + 0.675F;
        this.legs[7].rotateAngleZ += -var18 - 0.675F;
    }
}
