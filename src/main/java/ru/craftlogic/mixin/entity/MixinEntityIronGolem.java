package ru.craftlogic.mixin.entity;

import net.minecraft.entity.monster.EntityIronGolem;
import org.spongepowered.asm.mixin.Mixin;
import ru.craftlogic.api.entity.Creature;

@Mixin(EntityIronGolem.class)
public abstract class MixinEntityIronGolem implements Creature {
    @Override
    public int increaseAirSupply(int air) {
        return air;
    }
}
