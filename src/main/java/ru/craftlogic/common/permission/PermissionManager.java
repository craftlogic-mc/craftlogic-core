package ru.craftlogic.common.permission;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.api.Server;
import ru.craftlogic.api.util.ConfigurableManager;
import ru.craftlogic.api.world.OfflinePlayer;
import ru.craftlogic.common.command.CommandManager;
import ru.craftlogic.common.permission.GroupManager.Group;
import ru.craftlogic.common.permission.UserManager.User;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public class PermissionManager extends ConfigurableManager {
    private static final Logger LOGGER = LogManager.getLogger("PermissionManager");

    private final Path configFile;
    final UserManager userManager;
    final GroupManager groupManager;

    public PermissionManager(Server server, Path settingsDirectory) {
        super(server, settingsDirectory.resolve("permissions.json"), LOGGER);
        this.configFile = settingsDirectory.resolve("permissions.json");
        this.userManager = new UserManager(this, settingsDirectory.resolve("permissions/users.json"), LOGGER);
        this.groupManager = new GroupManager(this, settingsDirectory.resolve("permissions/groups.json"), LOGGER);
    }

    @Override
    public Path getConfigFile() {
        return this.configFile;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void registerCommands(CommandManager commandManager) {
        commandManager.registerCommandContainer(PermissionCommands.class);
    }

    @Override
    protected void load(JsonObject config) {
        try {
            this.groupManager.load();
            this.userManager.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("Load complete!");
    }

    @Override
    protected void save(JsonObject config) {
        try {
            this.groupManager.save(true);
            this.userManager.save(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasPermissions(GameProfile profile, String... permissions) {
        return this.hasPermissions(profile, Arrays.asList(permissions));
    }

    public boolean hasPermissions(GameProfile profile, Collection<String> permissions) {
        User user = this.userManager.getUser(profile.getId());
        return user.hasPermissions(permissions);
    }

    public Group getDefaultGroup() {
        return this.groupManager.groups.get("default");
    }

    public Group getGroup(String name) {
        return this.groupManager.groups.get(name);
    }

    public String[] getAllGroups() {
        return this.groupManager.groups.keySet().toArray(new String[0]);
    }

    public Collection<Group> getGroups(OfflinePlayer player) {
        return this.getGroups(player.getId());
    }

    public Collection<Group> getGroups(UUID id) {
        return this.userManager.getGroups(id);
    }

    public User getUser(OfflinePlayer player) {
        return this.getUser(player.getId());
    }

    public User getUser(UUID id) {
        return this.userManager.getUser(id);
    }
}
