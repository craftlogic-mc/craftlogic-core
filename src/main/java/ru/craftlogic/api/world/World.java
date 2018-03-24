package ru.craftlogic.api.world;

import net.minecraft.util.math.BlockPos;
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

    public Location getLocation(BlockPos pos) {
        return new Location(this.getHandle(), pos);
    }

    public Location getLocation(double x, double y, double z) {
        return new Location(this.getHandle(), x, y, z);
    }

    public Location getLocation(double x, double y, double z, float yaw, float pitch) {
        return new Location(this.getHandle(), x, y, z, yaw, pitch);
    }
}
