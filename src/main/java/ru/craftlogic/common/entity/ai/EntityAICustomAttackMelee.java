package ru.craftlogic.common.entity.ai;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.monster.EntityMob;

public class EntityAICustomAttackMelee<M extends EntityMob & IRangedAttackMob> extends EntityAIAttackMelee {
    protected final M mob;

    public EntityAICustomAttackMelee(M mob, double speed, boolean longMemory) {
        super(mob, speed, longMemory);
        this.mob = mob;
    }

    @Override
    public void resetTask() {
        super.resetTask();
        this.mob.setSwingingArms(false);
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
        this.mob.setSwingingArms(true);
    }
}
