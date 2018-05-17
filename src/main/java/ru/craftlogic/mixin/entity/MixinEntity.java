package ru.craftlogic.mixin.entity;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Random;

@Mixin(Entity.class)
public class MixinEntity {
    @Shadow public World world;
    @Shadow protected Random rand;
    @Shadow protected boolean inWater;
    @Shadow public float fallDistance;
    @Shadow protected boolean firstUpdate;
    @Shadow public double posX,  posY,  posZ;
    @Shadow public double motionX,  motionY,  motionZ;
    @Shadow public float width, height;

    @Shadow
    public AxisAlignedBB getEntityBoundingBox() {
        return null;
    }

    @Shadow
    @Nullable
    public Entity getRidingEntity() {
        return null;
    }

    @Shadow
    public void extinguish() {}

    @Shadow
    public Entity getControllingPassenger() {
        return null;
    }

    @Shadow
    public boolean isBeingRidden() {
        return false;
    }

    @Shadow
    protected SoundEvent getSwimSound() {
        return null;
    }

    @Shadow
    protected SoundEvent getSplashSound() {
        return null;
    }

    @Shadow
    public void playSound(SoundEvent sound, float volume, float pitch) {}

    @Overwrite
    protected void doWaterSplashEffect() {
        int color = 0xFFFFFF;
        Entity entity = this.isBeingRidden() && this.getControllingPassenger() != null ? this.getControllingPassenger() : (Entity)(Object)this;
        float f = entity == (Object)this ? 0.2F : 0.9F;

        float volume = MathHelper.sqrt(entity.motionX * entity.motionX * 0.20000000298023224D + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ * 0.20000000298023224D) * f;
        if (volume > 1F) {
            volume = 1F;
        }

        this.playSound(this.getSplashSound(), volume, 1F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
        float f2 = (float)MathHelper.floor(this.getEntityBoundingBox().minY);

        for(int j = 0; (float)j < 1F + this.width * 20F; ++j) {
            float dx = (this.rand.nextFloat() * 2F - 1F) * this.width;
            float dz = (this.rand.nextFloat() * 2F - 1F) * this.width;
            this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX + (double)dx, (double)(f2 + 1F), this.posZ + (double)dz, this.motionX, this.motionY - (double)(this.rand.nextFloat() * 0.2F), this.motionZ, color);
        }

        for (int j = 0; (float)j < 1F + this.width * 20F; ++j) {
            float dx = (this.rand.nextFloat() * 2F - 1F) * this.width;
            float dz = (this.rand.nextFloat() * 2F - 1F) * this.width;
            this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (double)dx, (double)(f2 + 1F), this.posZ + (double)dz, this.motionX, this.motionY, this.motionZ, color);
        }
    }

    @Overwrite
    public boolean isOverWater() {
        return this.world.handleMaterialAcceleration(this.getEntityBoundingBox().grow(0.0, -20.0, 0.0).shrink(0.001), Material.WATER, (Entity)(Object)this);
    }

    @Overwrite
    public boolean handleWaterMovement() {
        if (this.getRidingEntity() instanceof EntityBoat) {
            this.inWater = false;
        } else if (this.world.handleMaterialAcceleration(this.getEntityBoundingBox().grow(0.0, -0.4000000059604645, 0.0).shrink(0.001), Material.WATER, (Entity)(Object)this)) {
            if (!this.inWater && !this.firstUpdate) {
                this.doWaterSplashEffect();
            }

            this.fallDistance = 0F;
            this.inWater = true;
            this.extinguish();
        } else {
            this.inWater = false;
        }

        return this.inWater;
    }
}
