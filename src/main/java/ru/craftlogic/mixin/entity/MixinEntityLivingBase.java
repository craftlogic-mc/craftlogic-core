package ru.craftlogic.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.entity.Creature;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity implements Creature {
    @Shadow protected boolean isJumping;

    public MixinEntityLivingBase(World world) {
        super(world);
    }

    @Override
    public boolean isJumping() {
        return isJumping;
    }
}
