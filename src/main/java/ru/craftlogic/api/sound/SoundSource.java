package ru.craftlogic.api.sound;

import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.world.Locatable;

public interface SoundSource extends Locatable {
    boolean isSoundActive(SoundEvent sound);

    default boolean isSoundRepeatable(SoundEvent sound) {
        return false;
    }

    default float getSoundPitch(SoundEvent sound) {
        return 1F;
    }

    default float getSoundVolume(SoundEvent sound) {
        return 1F;
    }

    @SideOnly(Side.CLIENT)
    default void updateSound(SoundEvent sound) {}
}
