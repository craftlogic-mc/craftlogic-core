package ru.craftlogic.api.event.world;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

public class WorldEntitySpawnCountEvent extends Event {
    public final EnumCreatureType type;
    public final World world;
    public int maxCount;

    public WorldEntitySpawnCountEvent(EnumCreatureType type, World world) {
        this.type = type;
        this.maxCount = type.getMaxNumberOfCreature();
        this.world = world;
    }
}
