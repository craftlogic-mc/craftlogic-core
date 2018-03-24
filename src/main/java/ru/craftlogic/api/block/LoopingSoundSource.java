package ru.craftlogic.api.block;

import net.minecraft.util.ResourceLocation;
import ru.craftlogic.api.sound.SoundSource;

public interface LoopingSoundSource extends SoundSource {
    @Override
    default boolean isSoundRepeatable(ResourceLocation sound) {
        return true;
    }
}
