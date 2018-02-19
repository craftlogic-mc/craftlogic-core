package ru.craftlogic.api.block;

import net.minecraft.util.ResourceLocation;
import ru.craftlogic.api.world.Location;

public interface LoopingSoundSource {
    Location getLocation();

    boolean isSoundActive(ResourceLocation sound);

    default float getSoundPitch(ResourceLocation sound) {
        return 1F;
    }

    default float getSoundVolume(ResourceLocation sound) {
        return 1F;
    }
}
