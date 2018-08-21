package ru.craftlogic.common.region;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.api.Server;
import ru.craftlogic.api.math.Bounding;
import ru.craftlogic.api.util.ConfigurableManager;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.World;
import ru.craftlogic.common.command.CommandManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class RegionManager extends ConfigurableManager {
    private static final Logger LOGGER = LogManager.getLogger("RegionManager");

    final Map<String, Map<UUID, Region>> regions = new HashMap<>();
    final HomeManager homeManager;

    public RegionManager(Server server, Path settingsDirectory) {
        super(server, settingsDirectory.resolve("regions.json"), LOGGER);
        this.homeManager = new HomeManager(this, settingsDirectory.resolve("homes.json"), LOGGER);
    }

    @Override
    public void registerCommands(CommandManager commandManager) {
        commandManager.registerCommandContainer(RegionCommands.class);
    }

    @Override
    public void load(JsonObject regions) {
        for (Map.Entry<String, JsonElement> entry : regions.entrySet()) {
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
                    Location center = Location.deserialize(world.getHandle(), region.get("center").getAsJsonObject());
                    int radius = region.get("radius").getAsInt();
                    this.regions.computeIfAbsent(center.getWorldName(), d -> new HashMap<>())
                                .put(owner, new Region(owner, center, radius, members, flags));
                }
            } else {
                LOGGER.warn("Ignoring unknown world '" + entry.getKey());
            }
        }

        try {
            this.homeManager.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(JsonObject regions) {
        for (Map.Entry<String, Map<UUID, Region>> entry : this.regions.entrySet()) {
            JsonObject worldRegions = new JsonObject();
            for (Map.Entry<UUID, Region> _r : entry.getValue().entrySet()) {
                Region r = _r.getValue();
                JsonObject region = new JsonObject();
                if (!r.members.isEmpty()) {
                    JsonArray members = new JsonArray();
                    for (UUID member : r.members) {
                        members.add(new JsonPrimitive(member.toString()));
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
                region.add("center", r.center.serialize());
                region.addProperty("radius", r.radius);
                worldRegions.add(_r.getKey().toString(), region);
            }
            regions.add(entry.getKey(), worldRegions);
        }

        try {
            this.homeManager.save(true);
        } catch (IOException e) {
            e.printStackTrace();
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
            return this.center.getBlockX() - this.radius;
        }

        @Override
        public double getStartY() {
            return 0;
        }

        @Override
        public double getStartZ() {
            return this.center.getBlockZ() - this.radius;
        }

        @Override
        public double getEndX() {
            return this.center.getBlockX() + this.radius;
        }

        @Override
        public double getEndY() {
            return this.center.getWorld().getHeight();
        }

        @Override
        public double getEndZ() {
            return this.center.getBlockZ() + this.radius;
        }
    }
}
