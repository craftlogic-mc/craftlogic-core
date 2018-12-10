package ru.craftlogic.api.server;

import com.google.common.collect.ImmutableSet;
import jline.internal.Nullable;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import ru.craftlogic.api.util.ServerManager;
import ru.craftlogic.api.world.Dimension;
import ru.craftlogic.api.world.World;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WorldManager extends ServerManager {
    private final Map<DimensionType, World> loadedWorlds = new HashMap<>();

    public WorldManager(Server server, Path settingsDirectory) {
        super(server, LogManager.getLogger("WorldManager"));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldLoaded(WorldEvent.Load event) {
        net.minecraft.world.World world = event.getWorld();
        if (world instanceof WorldServer) {
            World w = new World(this.server, (WorldServer) world);
            this.loadedWorlds.put(world.provider.getDimensionType(), w);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldUnloaded(WorldEvent.Unload event) {
        net.minecraft.world.World world = event.getWorld();
        if (world instanceof WorldServer) {
            this.loadedWorlds.remove(world.provider.getDimensionType());
        }
    }

    public Set<World> getAllLoaded() {
        return ImmutableSet.copyOf(this.loadedWorlds.values());
    }

    public Set<String> getAllLoadedNames() {
        Set<String> result = new HashSet<>();
        for (World world : getAllLoaded()) result.add(world.getName());
        return ImmutableSet.copyOf(result);
    }

    @Nullable
    public World get(Dimension dimension) {
        for (World world : getAllLoaded()) {
            if (dimension == world.getDimension()) {
                return world;
            }
        }
        return null;
    }

    @Nullable
    public World get(String name) {
        for (World world : getAllLoaded()) {
            if (name.equalsIgnoreCase(world.getName())) {
                return world;
            }
        }
        return null;
    }
}
