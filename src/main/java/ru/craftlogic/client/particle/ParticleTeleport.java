package ru.craftlogic.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleTeleport extends Particle {
    private final float initialScale;
    private final Entity host;
    private final double yOffset;

    public ParticleTeleport(World world, Entity host, double velX, double velY, double velZ) {
        super(world, host.posX, host.posY, host.posZ, velX, velY, velZ);
        this.host = host;
        posX = host.posX;
        posY = host.posY;
        posZ = host.posZ;
        float f = rand.nextFloat() * 0.6F + 0.4F;
        particleScale = rand.nextFloat() * 0.2F + 0.5F;
        initialScale = particleScale;
        particleRed = f * 0.9F;
        particleGreen = f * 0.3F;
        particleBlue = f * 1.0F;
        particleMaxAge = rand.nextInt(11) + 50;
        setParticleTextureIndex(rand.nextInt(9));
        yOffset = 0.6 * rand.nextFloat();
    }

    @Override
    public void move(double x, double y, double z) {
        setBoundingBox(getBoundingBox().offset(x, y, z));
        resetPositionToBB();
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        //float age = ((float)particleAge + partialTicks) / (float)particleMaxAge;
        //age = 1.0F - age;
        //particleScale = initialScale * age;
        super.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    @Override
    public int getBrightnessForRender(float partialTick) {
        int brightness = super.getBrightnessForRender(partialTick);
        float age = (float)particleAge / (float)particleMaxAge;
        int j = brightness & 255;
        int k = brightness >> 16 & 255;
        k += (int)(age * 15.0F * 16.0F);
        if (k > 240) {
            k = 240;
        }

        return j | k << 16;
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        float age = (float)particleAge / (float)particleMaxAge;
        float speed = 2 * age * age - age;
        double angle = age * 4 * Math.PI;
        double sin = Math.sin(age * Math.PI / 2);
        double radius = 0.7 * Math.sqrt(sin);
        posX = host.posX + radius * Math.cos(angle);
        posY = host.posY + age * 1.5 + yOffset;
        posZ = host.posZ + radius * Math.sin(angle);
        if (particleAge++ >= particleMaxAge) {
            setExpired();
        }
    }
}
