package ru.craftlogic.mixin.entity.passive;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntitySheep.class)
public abstract class MixinEntitySheep extends EntityAnimal implements IShearable {
    public MixinEntitySheep(World world) {
        super(world);
    }

    @Shadow
    public abstract void setSheared(boolean sheared);

    @Shadow
    public abstract EnumDyeColor getFleeceColor();
}
