package ru.craftlogic.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.api.Server;
import ru.craftlogic.api.math.Bounding;
import ru.craftlogic.api.util.JsonConfiguration;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.World;

import java.nio.file.Path;
import java.util.*;

public class RegionManager implements JsonConfiguration {
    private static final Logger LOGGER = LogManager.getLogger("RegionManager");

    private final Server server;
    private final Path regionsFile;
    final Map<String, Map<UUID, Region>> regions = new HashMap<>();
    private boolean needsSave = false;

    public RegionManager(Server server) {
        this.server = server;
        this.regionsFile = server.getDataDirectory().resolve("regions.json");
    }

    @Override
    public Path getConfigFile() {
        return this.regionsFile;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public boolean isDirty() {
        return this.needsSave;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.needsSave = dirty;
    }

    @Override
    public void load0(JsonObject root) {
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            World world = this.server.getWorld(entry.getKey());
            if (world != null) {
                JsonObject worldRegions = entry.getValue().getAsJsonObject();
                for (Map.Entry<String, JsonElement> _e : worldRegions.entrySet()) {
                    JsonObject region = _e.getValue().getAsJsonObject();
                    UUID owner = UUID.fromString(_e.getKey());
                    List<UUID> members = new ArrayList<>();
                    if (region.has("members")) {
                        for (JsonElement _m : region.get("members").getAsJsonArray()) {
                            members.add(UUID.fromString(_m.getAsString()));
                        }
                    }
                    Map<String, String> flags = new HashMap<>();
                    if (region.has("flags")) {
                        for (Map.Entry<String, JsonElement> e : region.get("flags").getAsJsonObject().entrySet()) {
                            flags.put(e.getKey(), e.getValue().getAsString());
                        }
                    }
                    Location center = Location.fromJson(world.getHandle(), region.get("center").getAsJsonObject());
                    int radius = region.get("radius").getAsInt();
                    this.regions.computeIfAbsent(center.getWorldName(), d -> new HashMap<>())
                                .put(owner, new Region(owner, center, radius, members, flags));
                }
            } else {
                LOGGER.warn("Ignoring unknown world '" + entry.getKey());
            }
        }
    }

    @Override
    public void save0(JsonObject root) {
        for (Map.Entry<String, Map<UUID, Region>> entry : this.regions.entrySet()) {
            JsonObject worldRegions = new JsonObject();
            for (Map.Entry<UUID, Region> _r : entry.getValue().entrySet()) {
                Region r = _r.getValue();
                JsonObject region = new JsonObject();
                if (!r.members.isEmpty()) {
                    JsonArray members = new JsonArray();
                    for (UUID member : r.members) {
                        members.add(member.toString());
                    }
                    region.add("members", members);
                }
                if (!r.flags.isEmpty()) {
                    JsonObject flags = new JsonObject();
                    for (Map.Entry<String, String> _f : r.flags.entrySet()) {
                        flags.addProperty(_f.getKey(), _f.getValue());
                    }
                    region.add("flags", flags);
                }
                region.add("center", r.center.toJson());
                region.addProperty("radius", r.radius);
                worldRegions.add(_r.getKey().toString(), region);
            }
            root.add(entry.getKey(), worldRegions);
        }
    }

    public Region getRegion(Location location) {
        Map<UUID, Region> worldRegions = this.regions.get(location.getWorld().provider.getDimensionType().getName());
        if (worldRegions != null) {
            for (Map.Entry<UUID, Region> entry : worldRegions.entrySet()) {
                if (entry.getValue().isOwning(location)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public Region getRegion(String world, UUID owner) {
        Map<UUID, Region> worldRegions = this.regions.get(world);
        if (worldRegions != null) {
            return worldRegions.get(owner);
        }
        return null;
    }

    public List<Region> getRegions(Location location, int radius) {
        Map<UUID, Region> worldRegions = this.regions.get(location.getWorldName());
        if (worldRegions != null) {
            List<Region> result = new ArrayList<>();
            Bounding origin = location.toFullHeightBounding(radius);
            for (Region region : worldRegions.values()) {
                if (origin.isIntersects(region)) {
                    result.add(region);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    public class Region implements Bounding {
        final UUID owner;
        final List<UUID> members;
        final Map<String, String> flags;
        final Location center;
        int radius;

        public Region(UUID owner, Location center, int radius, List<UUID> members, Map<String, String> flags) {
            this.owner = owner;
            this.center = center;
            this.radius = radius;
            this.members = members;
            this.flags = flags;
        }

        @Override
        public double getStartX() {
            return this.center.getX() - this.radius;
        }

        @Override
        public double getStartY() {
            return 0;
        }

        @Override
        public double getStartZ() {
            return this.center.getZ() - this.radius;
        }

        @Override
        public double getEndX() {
            return this.center.getX() + this.radius;
        }

        @Override
        public double getEndY() {
            return this.center.getWorld().getHeight();
        }

        @Override
        public double getEndZ() {
            return this.center.getZ() + this.radius;
        }
    }
}
