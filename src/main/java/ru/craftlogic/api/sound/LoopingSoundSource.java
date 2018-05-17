package ru.craftlogic.api.sound;

import net.minecraft.util.SoundEvent;

public interface LoopingSoundSource extends SoundSource {
    @Override
    default boolean isSoundRepeatable(SoundEvent sound) {
        return true;
    }
}
