package ru.craftlogic.api.entity;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nullable;
import java.util.UUID;

public interface Tameable {
    boolean isTamed();
    void setTamed(boolean tamed);
    @Nullable
    UUID getOwnerId();
    void setOwnerId(@Nullable UUID id);

    default boolean isOwner(EntityLivingBase entity) {
        return entity.getUniqueID() == this.getOwnerId();
    }

    default void setTamedBy(EntityLivingBase entity) {
        this.setTamed(true);
        this.setOwnerId(entity.getUniqueID());
        if (entity instanceof EntityPlayerMP && this instanceof EntityAnimal) {
            CriteriaTriggers.TAME_ANIMAL.trigger((EntityPlayerMP)entity, (EntityAnimal)this);
        }
    }
}
