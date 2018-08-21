package ru.craftlogic.common.entity.ai;

import net.minecraft.entity.monster.EntityZombie;
import ru.craftlogic.api.entity.Zombie;

public class EntityAICustomZombieAttack<Z extends EntityZombie & Zombie> extends EntityAICustomAttackMelee<Z> {
    private int raiseArmTicks;

    public EntityAICustomZombieAttack(Z zombie, double speed, boolean longMemory) {
        super(zombie, speed, longMemory);
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
        this.raiseArmTicks = 0;
    }

    @Override
    public void resetTask() {
        super.resetTask();
        this.mob.setArmsRaised(false);
    }

    @Override
    public void updateTask() {
        super.updateTask();
        ++this.raiseArmTicks;
        this.mob.setArmsRaised(this.raiseArmTicks >= 5 && this.attackTick < 10);
    }
}
