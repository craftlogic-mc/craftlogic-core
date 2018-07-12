package ru.craftlogic.mixin.entity.projectile;

import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ru.craftlogic.common.CraftItems;

@Mixin(EntityEgg.class)
public abstract class MixinEntityEgg extends EntityThrowable {
    public MixinEntityEgg(World world) {
        super(world);
    }

    @Overwrite
    protected void onImpact(RayTraceResult target) {
        if (target.entityHit != null) {
            target.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 0F);
        }

        if (!this.world.isRemote) {
            int amount = this.rand.nextInt(32) == 0 ? 2 : 1;

            for(int i = 0; i < amount; ++i) {
                this.dropItem(CraftItems.RAW_EGG, 1);
            }

            this.world.setEntityState(this, (byte)3);
            this.setDead();
        }
    }
}
