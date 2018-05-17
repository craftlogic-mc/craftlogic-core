package ru.craftlogic.api.sound;

import net.minecraft.entity.Entity;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.world.Location;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class EntitySoundSource implements LoopingSoundSource {
    private final Entity entity;
    private final Predicate<Entity> validationChecker;
    private final BiConsumer<Entity, SoundEvent> updater;

    public EntitySoundSource(Entity entity, Predicate<Entity> validationChecker, BiConsumer<Entity, SoundEvent> updater) {
        this.entity = entity;
        this.validationChecker = validationChecker;
        this.updater = updater;
    }

    @Override
    public boolean isSoundActive(SoundEvent sound) {
        return !this.entity.isDead && this.validationChecker.test(this.entity);
    }

    @Override
    public Location getLocation() {
        return new Location(this.entity);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateSound(SoundEvent sound) {
        if (this.updater != null) {
            this.updater.accept(this.entity, sound);
        }
    }
}
