package ru.craftlogic.api.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldServer;
import net.minecraft.world.border.WorldBorder;
import ru.craftlogic.api.server.Server;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class World {
    private final Server server;
    private final Dimension dimension;
    private final WeakReference<WorldServer> handle;

    public World(Server server, WorldServer handle) {
        this.server = server;
        this.dimension = Dimension.fromVanilla(handle.provider.getDimensionType());
        this.handle = new WeakReference<>(handle);
        GameRules rules = getRules();
        rules.addGameRule("hidePlayerJoinMessages", "false", GameRules.ValueType.BOOLEAN_VALUE);
        rules.addGameRule("hidePlayerLeaveMessages", "false", GameRules.ValueType.BOOLEAN_VALUE);
    }

    public static World fromVanilla(Server server, net.minecraft.world.World world) {
        return server.getWorldManager().get(Dimension.fromVanilla(world.provider.getDimensionType()));
    }

    public Server getServer() {
        return this.server;
    }

    public String getName() {
        return this.getDimension().getName();
    }

    public Dimension getDimension() {
        return this.dimension;
    }

    public boolean isLoaded() {
        return unwrap() != null;
    }

    public WorldServer unwrap() {
        return this.handle.get();
    }

    public Location getLocation(BlockPos pos) {
        return new Location(unwrap(), pos);
    }

    public Location getLocation(double x, double y, double z) {
        return new Location(unwrap(), x, y, z);
    }

    public Location getLocation(double x, double y, double z, float yaw, float pitch) {
        return new Location(unwrap(), x, y, z, yaw, pitch);
    }

    public Location getSpawnLocation() {
        return new Location(unwrap(), unwrap().getSpawnPoint());
    }

    public int getHeight() {
        return this.unwrap().getHeight();
    }

    public int getTerrainHeight(int x, int z) {
        return this.unwrap().getHeight(x, z);
    }

    public GameRules getRules() {
        return unwrap().getGameRules();
    }

    public Set<Player> getPlayers() {
        Set<Player> result = new HashSet<>();
        for (Player player : server.getPlayerManager().getAllOnline()) {
            if (player.unwrap() != null && player.getWorld().equals(this)) {
                result.add(player);
            }
        }
        return result;
    }

    public Path getDir() {
        WorldServer handle = this.handle.get();
        Path dir = handle.getSaveHandler().getWorldDirectory().toPath();
        String sub = handle.provider.getSaveFolder();
        if (sub != null) {
            dir = dir.resolve(sub);
        }
        return dir;
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
        return unwrap().getWorldTime();
    }

    public void setTotalTime(long time) {
        unwrap().setWorldTime(time);
    }

    public void addTotalTime(long deltaTime) {
        setTotalTime(getTotalTime() + deltaTime);
    }

    public boolean isRaining() {
        return unwrap().getWorldInfo().isRaining();
    }

    public void setRaining(boolean raining) {
        unwrap().getWorldInfo().setRaining(raining);
    }

    public boolean isThundering() {
        return unwrap().getWorldInfo().isThundering();
    }

    public void setThundering(boolean thundering) {
        unwrap().getWorldInfo().setThundering(thundering);
    }

    public int getCleanWeatherTime() {
        return unwrap().getWorldInfo().getCleanWeatherTime();
    }

    public void setCleanWeatherTime(int time) {
        unwrap().getWorldInfo().setCleanWeatherTime(time);
    }

    public int getRainTime() {
        return unwrap().getWorldInfo().getRainTime();
    }

    public void setRainTime(int time) {
        unwrap().getWorldInfo().setRainTime(time);
    }

    public int getThunderTime() {
        return unwrap().getWorldInfo().getThunderTime();
    }

    public void setThunderTime(int time) {
        unwrap().getWorldInfo().setThunderTime(time);
    }

    public WorldBorder getBorder() {
        return unwrap().getWorldBorder();
    }

    public Random getRandom() {
        return unwrap().rand;
    }
}
