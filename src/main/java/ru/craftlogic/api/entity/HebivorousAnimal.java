package ru.craftlogic.api.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface HebivorousAnimal extends Animal {
    default void onPlantEaten(IBlockState block) {
        ((EntityLiving)this).eatGrassBonus();
    }
    float getRotationPitch();
    int getEatTimer();
    int getMaxEatTimer();

    @SideOnly(Side.CLIENT)
    default float getHeadRotationPointY(float p) {
        int eatTimer = getEatTimer();
        if (eatTimer <= 0) {
            return 0F;
        } else if (eatTimer >= 4 && eatTimer <= getMaxEatTimer() - 4) {
            return 1F;
        } else {
            return eatTimer < 4 ? ((float)eatTimer - p) / 4F : -((float)(eatTimer - getMaxEatTimer()) - p) / 4F;
        }
    }

    @SideOnly(Side.CLIENT)
    default float getHeadRotationAngleX(float p) {
        int eatTimer = getEatTimer();
        if (eatTimer > 4 && eatTimer <= getMaxEatTimer() - 4) {
            float f = ((float)(eatTimer - 4) - p) / 32F;
            return 0.62831855F + 0.2199115F * MathHelper.sin(f * 28.7F);
        } else {
            return eatTimer > 0 ? 0.62831855F : getRotationPitch() * 0.017453292F;
        }
    }
}
