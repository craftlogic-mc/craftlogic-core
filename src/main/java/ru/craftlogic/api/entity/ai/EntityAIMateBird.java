package ru.craftlogic.api.entity.ai;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import ru.craftlogic.api.entity.Bird;

import java.util.List;
import java.util.Random;

public class EntityAIMateBird<B extends EntityAnimal & Bird> extends EntityAIBase {
    private final B bird;
    private final Class<? extends B> mateClass;
    private World world;
    private B targetBird;
    private int delay;
    private double moveSpeed;

    public EntityAIMateBird(B bird, double moveSpeed) {
        this(bird, moveSpeed, (Class<? extends B>) bird.getClass());
    }

    public EntityAIMateBird(B bird, double moveSpeed, Class<? extends B> target) {
        this.bird = bird;
        this.world = bird.world;
        this.mateClass = target;
        this.moveSpeed = moveSpeed;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (!this.bird.isInLove()) {
            return false;
        } else {
            this.targetBird = this.getNearbyMate();
            return this.targetBird != null;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.targetBird.isEntityAlive() && this.targetBird.isInLove() && this.delay < 60;
    }

    @Override
    public void resetTask() {
        this.targetBird = null;
        this.delay = 0;
    }

    @Override
    public void updateTask() {
        this.bird.getLookHelper().setLookPositionWithEntity(this.targetBird, 10.0F, (float)this.bird.getVerticalFaceSpeed());
        this.bird.getNavigator().tryMoveToEntityLiving(this.targetBird, this.moveSpeed);
        ++this.delay;
        if (this.delay >= 60 && this.bird.getDistanceSq(this.targetBird) < 9.0D) {
            this.spawnEgg();
        }

    }

    private B getNearbyMate() {
        List<B> animals = this.world.getEntitiesWithinAABB(this.mateClass, this.bird.getEntityBoundingBox().grow(8));
        double d = Double.MAX_VALUE;
        B target = null;

        for (B animal : animals) {
            if (this.bird.canMateWith(animal) && this.bird.getDistanceSq(animal) < d) {
                target = animal;
                d = this.bird.getDistanceSq(animal);
            }
        }

        return target;
    }

    private void spawnEgg() {
        EntityPlayerMP player = this.bird.getLoveCause();
        if (player == null && this.targetBird.getLoveCause() != null) {
            player = this.targetBird.getLoveCause();
        }

        if (player != null) {
            player.addStat(StatList.ANIMALS_BRED);
        }

        this.bird.setGrowingAge(6000);
        this.targetBird.setGrowingAge(6000);
        this.bird.resetInLove();
        this.targetBird.resetInLove();
        Random random = this.bird.getRNG();

        if (!this.bird.isRooster()) {
            int delay = this.bird.getEggLayingDelay();
            int possibleEggs = this.bird.getPossibleEggsCount();
            if (delay <= 0) {
                this.bird.setEggLayingDelay(this.world.rand.nextInt(6000) + 6000);
                this.targetBird.setPossibleEggsCount(possibleEggs + this.world.rand.nextInt(5) + 1);
            }
        } else if (!this.targetBird.isRooster()) {
            int delay = this.targetBird.getEggLayingDelay();
            int possibleEggs = this.targetBird.getPossibleEggsCount();
            if (delay <= 0) {
                this.targetBird.setEggLayingDelay(this.world.rand.nextInt(6000) + 6000);
                this.targetBird.setPossibleEggsCount(possibleEggs + this.world.rand.nextInt(5) + 1);
            }
        }

        for (int i = 0; i < 7; ++i) {
            double dx = random.nextGaussian() * 0.02;
            double dy = random.nextGaussian() * 0.02;
            double dz = random.nextGaussian() * 0.02;
            double ox = random.nextDouble() * (double)this.bird.width * 2.0D - (double)this.bird.width;
            double oy = 0.5D + random.nextDouble() * (double)this.bird.height;
            double oz = random.nextDouble() * (double)this.bird.width * 2.0D - (double)this.bird.width;
            this.world.spawnParticle(EnumParticleTypes.HEART, this.bird.posX + ox, this.bird.posY + oy, this.bird.posZ + oz, dx, dy, dz);
        }

        if (this.world.getGameRules().getBoolean("doMobLoot")) {
            this.world.spawnEntity(new EntityXPOrb(this.world, this.bird.posX, this.bird.posY, this.bird.posZ, random.nextInt(7) + 1));
        }
    }
}
