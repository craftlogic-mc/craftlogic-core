package ru.craftlogic.client.sound;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import ru.craftlogic.api.sound.SoundSource;
import ru.craftlogic.api.world.Location;

public class Sound extends PositionedSound implements ITickableSound {
    private final SoundSource source;

    public Sound(SoundSource source, SoundEvent sound, SoundCategory category) {
        super(sound, category);
        this.source = source;
    }

    public Sound(SoundSource source, ResourceLocation sound, SoundCategory category) {
        super(sound, category);
        this.source = source;
    }

    public Location getLocation() {
        return this.source.getLocation();
    }

    @Override
    public boolean canRepeat() {
        return this.source.isSoundRepeatable(this.positionedSoundLocation);
    }

    @Override
    public boolean isDonePlaying() {
        return !getLocation().isBlockLoaded() || !this.source.isSoundActive(this.positionedSoundLocation);
    }

    @Override
    public void update() {
        this.source.updateSound(this.positionedSoundLocation);
    }

    @Override
    public float getXPosF() {
        return (float) getLocation().getDX();
    }

    @Override
    public float getYPosF() {
        return (float) getLocation().getDY();
    }

    @Override
    public float getZPosF() {
        return (float) getLocation().getDZ();
    }

    @Override
    public float getPitch() {
        return this.source.getSoundPitch(this.positionedSoundLocation) * this.sound.getPitch();
    }

    @Override
    public float getVolume() {
        return this.source.getSoundVolume(this.positionedSoundLocation) * this.sound.getVolume();
    }
}
