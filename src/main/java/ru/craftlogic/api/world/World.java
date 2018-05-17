package ru.craftlogic.api.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldServer;
import ru.craftlogic.api.server.Server;

import java.util.Objects;

public class World {
    private final Server server;
    private final Dimension dimension;

    public World(Server server, Dimension dimension) {
        this.server = server;
        this.dimension = dimension;
    }

    public String getName() {
        return this.getDimension().getName();
    }

    public Dimension getDimension() {
        return this.dimension;
    }

    public WorldServer getHandle() {
        return this.server.getHandle().getWorld(this.dimension.getVanilla().getId());
    }

    public Location getLocation(BlockPos pos) {
        return new Location(getHandle(), pos);
    }

    public Location getLocation(double x, double y, double z) {
        return new Location(getHandle(), x, y, z);
    }

    public Location getLocation(double x, double y, double z, float yaw, float pitch) {
        return new Location(getHandle(), x, y, z, yaw, pitch);
    }

    public Location getSpawnLocation() {
        return new Location(getHandle(), getHandle().getSpawnPoint());
    }

    public GameRules getRules() {
        return getHandle().getGameRules();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof World)) return false;
        World world = (World) o;
        return dimension == world.dimension;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension.getVanilla().getId());
    }

    @Override
    public String toString() {
        return "World(" + dimension.getName() + ")";
    }

    public int getTotalDays() {
        return (int)(getTotalTime() / 24000L % (3L * 24000L * 24000L));
    }

    public long getCurrentDayTime() {
        return getTotalTime() % 24000L;
    }

    public long getTotalTime() {
        return getHandle().getWorldTime();
    }

    public void setTotalTime(long time) {
        getHandle().setWorldTime(time);
    }

    public void addTotalTime(long deltaTime) {
        setTotalTime(getTotalTime() + deltaTime);
    }
}
