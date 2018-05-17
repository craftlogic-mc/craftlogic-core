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
        return this.source.isSoundRepeatable(this.getEvent());
    }

    @Override
    public boolean isDonePlaying() {
        return !getLocation().isBlockLoaded() || !this.source.isSoundActive(this.getEvent());
    }

    @Override
    public void update() {
        this.source.updateSound(this.getEvent());
    }

    @Override
    public float getXPosF() {
        return (float) getLocation().getX();
    }

    @Override
    public float getYPosF() {
        return (float) getLocation().getY();
    }

    @Override
    public float getZPosF() {
        return (float) getLocation().getZ();
    }

    @Override
    public float getPitch() {
        return this.source.getSoundPitch(this.getEvent()) * this.sound.getPitch();
    }

    @Override
    public float getVolume() {
        return this.source.getSoundVolume(this.getEvent()) * this.sound.getVolume();
    }

    public SoundEvent getEvent() {
        return SoundEvent.REGISTRY.getObject(this.positionedSoundLocation);
    }
}
