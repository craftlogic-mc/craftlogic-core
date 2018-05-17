package ru.craftlogic.mixin.entity.effect;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.block.LightningStruckable;
import ru.craftlogic.api.world.Location;

@Mixin(EntityLightningBolt.class)
public abstract class MixinEntityLightningBolt extends EntityWeatherEffect {
    public MixinEntityLightningBolt(World world) {
        super(world);
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    public void constructor(World world, double x, double y, double z, boolean effectOnly, CallbackInfo info) {
        Location location = new Location(this);
        EntityLightningBolt lightningBolt = (EntityLightningBolt)(EntityWeatherEffect)this;
        if (location.isBlockLoaded()) {
            LightningStruckable struckable = location.getBlock(LightningStruckable.class);
            if (struckable != null) {
                struckable.onStruckByLightning(location, lightningBolt, effectOnly);
            }
        }
    }
}
