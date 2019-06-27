package ru.craftlogic.mixin.entity.monster;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.entity.Spider;
import ru.craftlogic.common.entity.ai.EntityAISpiderRangedAttack;
import ru.craftlogic.common.entity.projectile.EntitySpiderSpit;

@Mixin(EntitySpider.class)
public abstract class MixinEntitySpider extends EntityMob implements Spider {
    public MixinEntitySpider(World world) {
        super(world);
    }

    @Inject(method = "initEntityAI", at = @At("RETURN"))
    protected void onAiInit(CallbackInfo info) {
        this.tasks.addTask(4, new EntityAISpiderRangedAttack<>((EntitySpider & Spider) (Object) this, 1.25, 40, 20F));
    }

    @Override
    public void setSwingingArms(boolean b) { }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float damage) {
        EntitySpiderSpit spit = new EntitySpiderSpit(this.world, (EntitySpider) (Object) this);
        double dx = target.posX - this.posX;
        double dy = target.getEntityBoundingBox().minY + (double)(target.height / 3F) - spit.posY;
        double dz = target.posZ - this.posZ;
        float speed = MathHelper.sqrt(dx * dx + dz * dz) * 0.2F;
        spit.shoot(dx, dy + (double)speed, dz, 1.5F, 10F);
        this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LLAMA_SPIT, this.getSoundCategory(), 1F, 1F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
        this.world.spawnEntity(spit);
    }
}
