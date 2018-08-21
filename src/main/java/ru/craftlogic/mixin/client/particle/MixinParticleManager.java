package ru.craftlogic.mixin.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Map;

@SideOnly(Side.CLIENT)
@Mixin(ParticleManager.class)
public class MixinParticleManager {
    @Shadow @Final private Map<Integer, IParticleFactory> particleTypes;
    @Shadow protected World world;

    @Shadow
    public void addEffect(Particle particle) {}

    /**
     * @author Radviger
     * @reason Colored rain particles
     */
    @Nullable
    @Overwrite
    public Particle spawnEffectParticle(int id, double x, double y, double z, double mx, double my, double mz, int... data) {
        IParticleFactory factory = this.particleTypes.get(id);
        if (factory != null) {
            Particle particle = factory.createParticle(id, this.world, x, y, z, mx, my, mz, data);
            if (particle != null) {
                if (data.length >= 1) {
                    if (particle instanceof ParticleRain || particle instanceof ParticleBubble) {
                        java.awt.Color rgb = new java.awt.Color(data[0]);
                        particle.setRBGColorF(
                            (float)rgb.getRed() / 255F,
                            (float)rgb.getGreen() / 255F,
                            (float)rgb.getBlue() / 255F
                        );
                    }
                }
                this.addEffect(particle);
                return particle;
            }
        }

        return null;
    }
}
