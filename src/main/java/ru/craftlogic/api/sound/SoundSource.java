package ru.craftlogic.api.sound;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.world.Locateable;

public interface SoundSource extends Locateable {
    boolean isSoundActive(ResourceLocation sound);

    default boolean isSoundRepeatable(ResourceLocation sound) {
        return false;
    }

    default float getSoundPitch(ResourceLocation sound) {
        return 1F;
    }

    default float getSoundVolume(ResourceLocation sound) {
        return 1F;
    }

    @SideOnly(Side.CLIENT)
    default void updateSound(ResourceLocation sound) {}
}
