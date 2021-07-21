package ru.craftlogic.mixin.entity;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
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

    @ModifyConstant(method = "onEntityUpdate", constant = @Constant(intValue = 300))
    public int onRestoreAirSupply(int old) {
        return increaseAirSupply(getAir());
    }

    @Override
    public int increaseAirSupply(int air) {
        int i = EnchantmentHelper.getRespirationModifier((EntityLivingBase) (Object) this);
        return Math.min(300, i > 0 && this.rand.nextInt(i + 1) > 0 ? air + 8 : air + 4);
    }
}
