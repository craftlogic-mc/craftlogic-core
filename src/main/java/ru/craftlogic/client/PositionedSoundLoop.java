package ru.craftlogic.client;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import ru.craftlogic.api.block.LoopingSoundSource;
import ru.craftlogic.api.world.Location;

public class PositionedSoundLoop extends PositionedSound implements ITickableSound {
    private final LoopingSoundSource source;
    private final Location location;

    public PositionedSoundLoop(LoopingSoundSource source, SoundEvent sound, SoundCategory category) {
        super(sound, category);
        this.source = source;
        this.repeat = true;
        this.location = source.getLocation();
    }

    public PositionedSoundLoop(LoopingSoundSource source, ResourceLocation sound, SoundCategory category) {
        super(sound, category);
        this.source = source;
        this.repeat = true;
        this.location = source.getLocation();
    }

    @Override
    public boolean isDonePlaying() {
        return !this.location.isBlockLoaded() || !this.source.isSoundActive(this.positionedSoundLocation);
    }

    @Override
    public void update() {}

    @Override
    public float getXPosF() {
        return (float) this.location.getDX();
    }

    @Override
    public float getYPosF() {
        return (float) this.location.getDY();
    }

    @Override
    public float getZPosF() {
        return (float) this.location.getDZ();
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
