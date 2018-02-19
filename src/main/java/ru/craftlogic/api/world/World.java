package ru.craftlogic.api.world;

import net.minecraft.world.WorldServer;
import ru.craftlogic.api.Server;

public class World {
    private final Server server;
    private final WorldServer world;

    public World(Server server, WorldServer world) {
        this.server = server;
        this.world = world;
    }

    public String getName() {
        return this.getDimension().getName();
    }

    public Dimension getDimension() {
        return Dimension.fromVanilla(this.world.provider.getDimensionType());
    }

    public WorldServer getHandle() {
        return world;
    }
}
