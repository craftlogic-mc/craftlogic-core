package ru.craftlogic.mixin.entity.projectile;

import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.CraftItems;

@Mixin(EntityEgg.class)
public abstract class MixinEntityEgg extends EntityThrowable {
    public MixinEntityEgg(World world) {
        super(world);
    }

    /**
     * @author Radviger
     * @reason Broken eggs
     */
    @Overwrite
    protected void onImpact(RayTraceResult target) {
        if (target.entityHit != null) {
            target.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), 0F);
        }

        if (!world.isRemote) {
            if (CraftConfig.items.enableRawEggs) {
                int amount = rand.nextInt(32) == 0 ? 2 : 1;

                for (int i = 0; i < amount; ++i) {
                    dropItem(CraftItems.RAW_EGG, 1);
                }
            } else {
                if (rand.nextInt(8) == 0) {
                    int i = 1;
                    if (rand.nextInt(32) == 0) {
                        i = 4;
                    }

                    for (int j = 0; j < i; ++j) {
                        EntityChicken chick = new EntityChicken(world);
                        chick.setGrowingAge(-24000);
                        chick.setLocationAndAngles(posX, posY, posZ, rotationYaw, 0.0F);
                        world.spawnEntity(chick);
                    }
                }
            }

            world.setEntityState(this, (byte) 3);
            setDead();
        }
    }
}
