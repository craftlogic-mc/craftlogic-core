package ru.craftlogic.common.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.math.MathHelper;
import ru.craftlogic.api.entity.Spider;

public class EntityAISpiderRangedAttack<S extends EntitySpider & Spider> extends EntityAIBase {
    private final S spider;
    private EntityLivingBase attackTarget;
    private int rangedAttackTime;
    private final double moveSpeed;
    private int seeTime;
    private final int attackInterval;
    private final int maxRangedAttackTime;
    private final float attackRadius;
    private final float maxAttackDistance;

    public EntityAISpiderRangedAttack(S spider, double moveSpeed, int attackInterval, float attackRadius) {
        this(spider, moveSpeed, attackInterval, attackInterval, attackRadius);
    }

    public EntityAISpiderRangedAttack(S spider, double moveSpeed, int attackInterval, int maxAttackTime, float attackRadius) {
        this.rangedAttackTime = -1;
        this.spider = spider;
        this.moveSpeed = moveSpeed;
        this.attackInterval = attackInterval;
        this.maxRangedAttackTime = maxAttackTime;
        this.attackRadius = attackRadius;
        this.maxAttackDistance = attackRadius * attackRadius;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase victim = this.spider.getAttackTarget();
        if (victim == null) {
            return false;
        } else {
            this.attackTarget = victim;
            return true;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.shouldExecute() || !this.spider.getNavigator().noPath() || this.spider.world.rand.nextFloat() <= 0.05F;
    }

    @Override
    public void resetTask() {
        this.attackTarget = null;
        this.seeTime = 0;
        this.rangedAttackTime = -1;
    }

    @Override
    public void updateTask() {
        double d = this.spider.getDistanceSq(this.attackTarget.posX, this.attackTarget.getEntityBoundingBox().minY, this.attackTarget.posZ);
        boolean see = this.spider.getEntitySenses().canSee(this.attackTarget);
        if (see) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }

        if (d <= (double)this.maxAttackDistance && this.seeTime >= 20) {
            this.spider.getNavigator().clearPath();
        } else {
            this.spider.getNavigator().tryMoveToEntityLiving(this.attackTarget, this.moveSpeed);
        }

        this.spider.getLookHelper().setLookPositionWithEntity(this.attackTarget, 30F, 30F);

        if (--this.rangedAttackTime == 0) {
            if (!see) {
                return;
            }

            float cooldown = MathHelper.clamp(MathHelper.sqrt(d) / this.attackRadius, 0.1F, 1F);
            this.spider.attackEntityWithRangedAttack(this.attackTarget, cooldown);
            this.rangedAttackTime = MathHelper.floor(cooldown * (float)(this.maxRangedAttackTime - this.attackInterval) + (float)this.attackInterval);
        } else if (this.rangedAttackTime < 0) {
            float cooldown = MathHelper.sqrt(d) / this.attackRadius;
            this.rangedAttackTime = MathHelper.floor(cooldown * (float)(this.maxRangedAttackTime - this.attackInterval) + (float)this.attackInterval);
        }
    }
}
