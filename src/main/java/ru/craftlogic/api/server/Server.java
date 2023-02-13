package ru.craftlogic.api.server;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.economy.EconomyManager;
import ru.craftlogic.api.event.server.ServerAddManagersEvent;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.permission.PermissionManager;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.util.ServerManager;
import ru.craftlogic.api.world.CommandSender;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.common.command.CommandManager;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class Server implements CommandSender {
    private final MinecraftServer handle;
    private final Map<Class<? extends ServerManager>, ServerManager> managers = new HashMap<>();
    private final ExecutorService taskScheduler = Executors.newSingleThreadExecutor(
        r -> new Thread(r, "Server task scheduler")
    );
    private final Set<UUID> cancelledTasks = new HashSet<>();
    private UUID currentlyProcessingPlayer;

    public Server(MinecraftServer handle) {
        this.handle = handle;
    }

    public static Server from(MinecraftServer server) {
        return ((AdvancedServer)server).wrapped();
    }

    public <M extends ServerManager> void addManager(Class<? extends M> type, BiFunction<Server, Path, M> factory) {
        if (this.managers.containsKey(type)) {
            throw new IllegalStateException("Manager of type " + type + " is already registered!");
        }
        M manager = factory.apply(this, this.getSettingsDirectory());
        this.managers.put(type, manager);
        MinecraftForge.EVENT_BUS.register(manager);
    }

    public <M extends ServerManager> M getManager(Class<? extends M> type) {
        return (M) this.managers.get(type);
    }

    @Override
    public boolean hasPermission(String permission, int opLevel) {
        return true;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }

    @Override
    public String getName() {
        return "Server";
    }

    public WorldManager getWorldManager() {
        return this.getManager(WorldManager.class);
    }

    public PlayerManager getPlayerManager() {
        return this.getManager(PlayerManager.class);
    }

    public CommandManager getCommandManager() {
        return this.getManager(CommandManager.class);
    }

    public PermissionManager getPermissionManager() {
        for (ServerManager manager : this.managers.values()) {
            if (manager instanceof PermissionManager) {
                return (PermissionManager) manager;
            }
        }
        return new PermissionManager() {
            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public boolean hasPermissions(GameProfile profile, Collection<String> permissions) {
                throw new UnsupportedOperationException("Permission system disabled");
            }

            @Override
            public String getPermissionMetadata(GameProfile profile, String meta) {
                throw new UnsupportedOperationException("Permission system disabled");
            }
        };
    }

    public EconomyManager getEconomyManager() {
        for (ServerManager manager : this.managers.values()) {
            if (manager instanceof EconomyManager) {
                return (EconomyManager) manager;
            }
        }
        return new EconomyManager() {
            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public float getBalance(UUID id) {
                throw new UnsupportedOperationException("Economy system disabled");
            }

            @Override
            public void setBalance(UUID id, float balance) {
                throw new UnsupportedOperationException("Economy system disabled");
            }
        };
    }

    public Path getDataDirectory() {
        return this.handle.getDataDirectory().toPath();
    }

    public Path getSettingsDirectory() {
        return this.getDataDirectory().resolve("settings/");
    }

    public void start() {
        this.addManager(WorldManager.class, WorldManager::new);
        this.addManager(PlayerManager.class, PlayerManager::new);
        this.addManager(CommandManager.class, CommandManager::new);
        MinecraftForge.EVENT_BUS.post(new ServerAddManagersEvent(this));

        if (!Files.exists(this.getSettingsDirectory())) {
            try {
                Files.createDirectory(this.getSettingsDirectory());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (ServerManager manager : this.managers.values()) {
            try {
                manager.load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        CommandManager commandManager = this.getCommandManager();

        for (ServerManager manager : this.managers.values()) {
            manager.registerCommands(commandManager);
        }
    }

    public void stop(StopReason reason) {
        if (reason != StopReason.CORE && !isDedicated() && !isIntegratedPublic()) {
            throw new IllegalStateException("Cannot stop integrated server!");
        }

        for (ServerManager manager : this.managers.values()) {
            try {
                manager.unload();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (reason != StopReason.CORE) {
            this.handle.stopServer();
        }
    }

    public boolean cancelTask(UUID id) {
        return this.cancelledTasks.add(id);
    }

    public UUID addTask(Consumer<Server> task) {
        UUID id = UUID.randomUUID();
        this.addDelayedTask(id, task, 0L);
        return id;
    }

    public UUID addDelayedTask(Consumer<Server> task, long delay) {
        UUID id = UUID.randomUUID();
        this.addDelayedTask(id, task, delay);
        return id;
    }

    private void addDelayedTask(UUID id, Consumer<Server> task, long delay) {
        long start = System.currentTimeMillis();
        this.handle.addScheduledTask(() -> {
            if (!this.cancelledTasks.remove(id)) {
                long delta = System.currentTimeMillis() - start;
                if (delta >= delay) {
                    task.accept(this);
                } else {
                    this.taskScheduler.submit(() -> addDelayedTask(id, task, delay - delta));
                }
            }
        });
    }

    public UUID addScheduledTask(Consumer<Server> task, long delay, long interval) {
        UUID id = UUID.randomUUID();
        addScheduledTask(id, task, delay, interval);
        return id;
    }

    private void addScheduledTask(UUID id, Consumer<Server> task, long delay, long interval) {
        addDelayedTask(id, s -> addRepeatedTask(id, task, interval), delay);
    }

    public UUID addRepeatedTask(Consumer<Server> task, long interval) {
        UUID id = UUID.randomUUID();
        addRepeatedTask(id, task, interval);
        return id;
    }

    private void addRepeatedTask(UUID id, Consumer<Server> task, long interval) {
        if (!cancelledTasks.remove(id)) {
            addTask(task.andThen(server -> {
                addDelayedTask(id, s -> {
                    addRepeatedTask(id, task, interval);
                }, interval);
            }));
        }
    }

    public UUID currentlyProcessingPlayer() {
        return currentlyProcessingPlayer;
    }

    public void currentlyProcessingPlayer(UUID player) {
        currentlyProcessingPlayer = player;
    }

    public boolean isSinglePlayer() {
        return this.handle.isSinglePlayer();
    }

    @Override
    public MinecraftServer unwrap() {
        return this.handle;
    }

    public boolean isDedicated() {
        return FMLCommonHandler.instance().getSide() == Side.SERVER;
    }

    public boolean isPublic() {
        return isDedicated() || isIntegratedPublic();
    }

    @SideOnly(Side.CLIENT)
    private boolean isIntegratedPublic() {
        IntegratedServer server = FMLClientHandler.instance().getClient().getIntegratedServer();
        return server != null && server.getPublic();
    }

    @Override
    public String toString() {
        MinecraftServer handle = unwrap();
        if (isDedicated()) {
            return "DedicatedServer{" +
                    "host=" + handle.getServerHostname() +
                    ", port=" + handle.getServerPort() +
                    ", motd=" + handle.getMOTD() +
                    '}';
        } else {
            return "IntegratedServer{" +
                    "motd=" + handle.getMOTD() +
                    '}';
        }
    }

    @Override
    public Server getServer() {
        return this;
    }

    public void broadcast(ITextComponent message) {
        for (Player player : getPlayerManager().getAllOnline()) {
            player.sendMessage(message);
        }
    }

    public void broadcast(Text<?, ?> message) {
        for (Player player : getPlayerManager().getAllOnline()) {
            player.sendMessage(message);
        }
    }

    public void broadcastPacket(AdvancedMessage packet) {
        for (Player player : getPlayerManager().getAllOnline()) {
            player.sendPacket(packet);
        }
    }

    /*public void broadcastPacket(String channel, NBTTagCompound packet) {
        for (Player player : getPlayerManager().getAllOnline()) {
            player.sendPacket(channel, packet);
        }
    }*/

    public void broadcastToast(Text<?, ?> title, int timeout) {
        for (Player player : getPlayerManager().getAllOnline()) {
            player.sendToast(title, timeout);
        }
    }

    public void broadcastToast(ITextComponent title, int timeout) {
        for (Player player : getPlayerManager().getAllOnline()) {
            player.sendToast(title, timeout);
        }
    }

    public void broadcastToast(Text<?, ?> title, Text<?, ?> subtitle, int timeout) {
        for (Player player : getPlayerManager().getAllOnline()) {
            player.sendToast(title, subtitle, timeout);
        }
    }

    public void broadcastToast(ITextComponent title, ITextComponent subtitle, int timeout) {
        for (Player player : getPlayerManager().getAllOnline()) {
            player.sendToast(title, subtitle, timeout);
        }
    }

    public void broadcastCountdown(String id, Text<?, ?> title, int timeout) {
        for (Player player : getPlayerManager().getAllOnline()) {
            player.sendCountdown(id, title, timeout);
        }
    }

    public void broadcastCountdown(String id, ITextComponent title, int timeout) {
        for (Player player : getPlayerManager().getAllOnline()) {
            player.sendCountdown(id, title, timeout);
        }
    }

    public void broadcastCountdown(String id, Text<?, ?> title, int timeout, int color) {
        for (Player player : getPlayerManager().getAllOnline()) {
            player.sendCountdown(id, title, timeout, color);
        }
    }

    public void broadcastCountdown(String id, ITextComponent title, int timeout, int color) {
        for (Player player : getPlayerManager().getAllOnline()) {
            player.sendCountdown(id, title, timeout, color);
        }
    }

    public void broadcastCountdown(String id, Text<?, ?> title, int timeout, int color, SoundEvent tickSound) {
        for (Player player : getPlayerManager().getAllOnline()) {
            player.sendCountdown(id, title.build(), timeout, color, tickSound);
        }
    }

    public void broadcastCountdown(String id, ITextComponent title, int timeout, int color, SoundEvent tickSound) {
        for (Player player : getPlayerManager().getAllOnline()) {
            player.sendCountdown(id, title, timeout, color, tickSound);
        }
    }

    public void broadcastTitle(ITextComponent title, @Nullable ITextComponent subtitle, int fadeIn, int timeout, int fadeOut) {
        for (Player player : getPlayerManager().getAllOnline()) {
            player.sendTitle(title, subtitle, fadeIn, timeout, fadeOut);
        }
    }

    public void broadcastTitle(Text<?, ?> title, @Nullable Text<?, ?> subtitle, int fadeIn, int timeout, int fadeOut) {
        for (Player player : getPlayerManager().getAllOnline()) {
            player.sendTitle(title, subtitle, fadeIn, timeout, fadeOut);
        }
    }

    public enum StopReason {
        PLUGIN, CORE, COMMAND
    }
}
