package ru.craftlogic.api.event.world;

import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;

public class WorldFlushToDiskEvent extends WorldEvent {
    public WorldFlushToDiskEvent(WorldServer world) {
        super(world);
    }

    @Override
    public WorldServer getWorld() {
        return (WorldServer) super.getWorld();
    }
}
