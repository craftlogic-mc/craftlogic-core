package ru.craftlogic.common.permission;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.api.Server;
import ru.craftlogic.api.util.ConfigurableManager;
import ru.craftlogic.api.world.Player;

import java.nio.file.Path;
import java.util.*;

public class PermissionManager extends ConfigurableManager {
    private static final Logger LOGGER = LogManager.getLogger("PermissionManager");

    private final Server server;
    private final Path permissionsFile;
    final Map<String, Group> groups = new HashMap<>();
    final Map<UUID, User> users = new HashMap<>();
    private final Map<Group, List<User>> groupUsersCache = new HashMap<>();

    public PermissionManager(Server server) {
        this.server = server;
        this.permissionsFile = server.getDataDirectory().resolve("permissions.json");
    }

    @Override
    public Path getConfigFile() {
        return this.permissionsFile;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void load(JsonObject root) {
        {
            JsonObject groups = root.getAsJsonObject("groups");
            if (groups == null) {
                LOGGER.warn("There's no groups to load! At all...");
                groups = new JsonObject();
                root.add("groups", groups);
                this.setDirty(true);
            }
            if (!groups.has("default")) {
                LOGGER.warn("Default group is missing! Creating empty one...");
                groups.add("default", new JsonObject());
                this.setDirty(true);
            }
            Map<String, JsonObject> groupCache = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : groups.entrySet()) {
                if (!(entry.getValue() instanceof JsonObject)) {
                    LOGGER.error("Group '" + entry.getKey() + "' must be an object! Ignoring it...");
                    continue;
                }
                groupCache.put(entry.getKey(), (JsonObject) entry.getValue());
            }
            for (Map.Entry<String, JsonObject> entry : groupCache.entrySet()) {
                String groupName = entry.getKey();
                JsonObject g = entry.getValue();
                String parentName = "default";
                if (g.has("parent") && !groupCache.containsKey(parentName = g.get("parent").getAsString())) {
                    LOGGER.error("Group '" + groupName + "' has undefined parent '" + parentName + "'! Ignoring it...");
                }
                if (parentName.equals(groupName)) {
                    LOGGER.error("Group '" + groupName + "' cannot be a child of itself!");
                }
                List<String> permissions = new ArrayList<>();
                if (g.has("permissions")) {
                    JsonArray p = g.getAsJsonArray("permissions");
                    for (JsonElement element : p) {
                        permissions.add(element.getAsString());
                    }
                }
                String prefix = g.has("prefix") ? g.get("prefix").getAsString() : null;
                String suffix = g.has("suffix") ? g.get("suffix").getAsString() : null;
                int priority = g.has("priority") ? g.get("priority").getAsInt() : 0;
                this.groups.put(groupName, new Group(groupName, parentName, permissions, prefix, suffix, priority));
            }
            groupCache.clear();
        }
        if (root.has("users")) {
            JsonObject users = root.getAsJsonObject("users");
            for (Map.Entry<String, JsonElement> entry : users.entrySet()) {
                UUID id = UUID.fromString(entry.getKey());
                if (!(entry.getValue() instanceof JsonObject)) {
                    LOGGER.error("User entry '" + id + "' isn't an object! Ignoring it...");
                    continue;
                }
                JsonObject u = (JsonObject) entry.getValue();
                List<String> permissions = new ArrayList<>();
                if (u.has("permissions")) {
                    for (JsonElement element : u.getAsJsonArray("permissions")) {
                        permissions.add(element.getAsString());
                    }
                }
                List<Group> groups = new ArrayList<>();
                groups.add(this.getDefaultGroup());
                if (u.has("groups")) {
                    for (JsonElement group : u.getAsJsonArray("groups")) {
                        String groupName = group.getAsString();
                        if (!this.groups.containsKey(groupName)) {
                            LOGGER.error("User '" + id + "' is a member of an unknown group named '" + groupName + "' Ignoring it...");
                            continue;
                        }
                        groups.add(this.groups.get(groupName));
                    }
                }
                String prefix = u.has("prefix") ? u.get("prefix").getAsString() : null;
                String suffix = u.has("suffix") ? u.get("suffix").getAsString() : null;
                User user = new User(id, groups, permissions, prefix, suffix);
                this.users.put(id, user);
                for (Group group : groups) {
                    this.groupUsersCache.computeIfAbsent(group, k -> new ArrayList<>()).add(user);
                }
            }
        }
        LOGGER.info("Load complete!");
    }

    @Override
    public void save(JsonObject root) {
        {
            JsonObject groups = new JsonObject();
            for (Map.Entry<String, Group> entry : this.groups.entrySet()) {
                Group g = entry.getValue();
                JsonObject group = new JsonObject();
                if (!(g.name.equals("default") && g.parent.equals("default"))) {
                    group.addProperty("parent", g.parent);
                }
                if (!g.permissions.isEmpty()) {
                    JsonArray permissions = new JsonArray();
                    for (String permission : g.permissions) {
                        permissions.add(new JsonPrimitive(permission));
                    }
                    group.add("permissions", permissions);
                }
                if (g.prefix != null) {
                    group.addProperty("prefix", g.prefix);
                }
                if (g.suffix != null) {
                    group.addProperty("suffix", g.suffix);
                }
                if (g.priority != 0) {
                    group.addProperty("priority", g.priority);
                }
                groups.add(entry.getKey(), group);
            }
            root.add("groups", groups);
        }
        JsonObject users = new JsonObject();
        for (Map.Entry<UUID, User> entry : this.users.entrySet()) {
            User u = entry.getValue();
            JsonObject user = new JsonObject();
            if (!u.permissions.isEmpty()) {
                JsonArray permissions = new JsonArray();
                for (String permission : u.permissions) {
                    permissions.add(new JsonPrimitive(permission));
                }
                user.add("permissions", permissions);
            }
            if (u.groups.size() > 1) {
                JsonArray groups = new JsonArray();
                for (Group group : u.groups) {
                    groups.add(new JsonPrimitive(group.name));
                }
                user.add("groups", groups);
            }
            if (u.prefix != null) {
                user.addProperty("prefix", u.prefix);
            }
            if (u.suffix != null) {
                user.addProperty("suffix", u.suffix);
            }
            users.add(entry.getKey().toString(), user);
        }
        root.add("users", users);
    }

    public boolean hasPermissions(GameProfile profile, String... permissions) {
        if (permissions.length == 0) {
            return true;
        }
        User user = this.users.get(profile.getId());
        if (user != null) {
            List<String> ps = user.permissions();
            if (ps.contains("*")) {
                return true;
            }
            for (String permission : permissions) {
                if (!ps.contains(permission) || ps.contains("-" + permission))
                    return false;
            }
            return true;
        }
        return false;
    }

    public Group getDefaultGroup() {
        return this.groups.get("default");
    }

    public Group getGroup(String name) {
        return this.groups.get(name);
    }

    public String[] getAllGroups() {
        return this.groups.keySet().toArray(new String[0]);
    }

    public Collection<Group> getGroups(GameProfile profile) {
        List<Group> groups = new ArrayList<>();
        User user = this.users.get(profile.getId());
        if (user != null) {
            groups.addAll(user.groups);
        } else {
            Group defaultGroup = this.getDefaultGroup();
            if (defaultGroup != null) {
                groups.add(defaultGroup);
            }
        }
        return groups;
    }

    public User getUser(UUID id) {
        return this.users.get(id);
    }

    public User getUser(Player player) {
        return this.getUser(player.getProfile().getId());
    }

    public class Group implements Comparable<Group> {
        final String name, parent;
        final List<String> permissions;
        String prefix, suffix;
        int priority;

        public Group(String name, String parent, List<String> permissions, String prefix, String suffix, int priority) {
            this.name = name;
            this.parent = parent;
            this.permissions = permissions;
            this.prefix = prefix;
            this.suffix = suffix;
            this.priority = priority;
        }

        public String name() {
            return this.name;
        }

        public Group parent() {
            Group parent = PermissionManager.this.groups.get(this.parent);
            if (parent == this) {
                return null;
            } else {
                return parent;
            }
        }

        public String prefix() {
            if (this.prefix != null) {
                return this.prefix;
            } else {
                Group parent = this.parent();
                if (parent == null) {
                    return "";
                } else {
                    return parent.prefix();
                }
            }
        }

        public String suffix() {
            if (this.suffix != null) {
                return this.suffix;
            } else {
                Group parent = this.parent();
                if (parent == null) {
                    return "";
                } else {
                    return parent.prefix();
                }
            }
        }

        public List<String> permissions() {
            List<String> permissions = new ArrayList<>(this.permissions);
            Group parent = this.parent();
            if (parent != null) {
                permissions.addAll(parent.permissions());
            }
            return permissions;
        }

        public List<User> users() {
            List<User> result = new ArrayList<>();
            if (!this.name.equals("default")) {
                List<User> cachedUsers = PermissionManager.this.groupUsersCache.get(this);
                if (cachedUsers != null) {
                    result.addAll(cachedUsers);
                }
            }
            return result;
        }

        @Override
        public int compareTo(Group o) {
            return Integer.compare(this.priority, o.priority);
        }
    }

    public class User {
        final UUID id;
        final List<Group> groups;
        final List<String> permissions;
        String prefix, suffix;

        public User(UUID id, List<Group> groups, List<String> permissions, String prefix, String suffix) {
            this.id = id;
            this.groups = groups;
            this.permissions = permissions;
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public UUID id() {
            return this.id;
        }

        public List<String> permissions() {
            List<String> permissions = new ArrayList<>(this.permissions);
            for (Group group : this.groups) {
                permissions.addAll(group.permissions());
            }
            return permissions;
        }

        public String prefix() {
            if (this.prefix != null) {
                return this.prefix;
            } else {
                Map<Integer, String> prefixes = new TreeMap<>();
                for (Group group : this.groups) {
                    String prefix = group.prefix();
                    if (prefix != null && !prefix.isEmpty()) {
                        prefixes.put(group.priority, prefix);
                    }
                }
                if (!prefixes.isEmpty()) {
                    return prefixes.get(0);
                } else {
                    return "";
                }
            }
        }

        public String suffix() {
            if (this.suffix != null) {
                return this.suffix;
            } else {
                Map<Integer, String> suffixes = new TreeMap<>();
                for (Group group : this.groups) {
                    String suffix = group.suffix();
                    if (suffix != null && !suffix.isEmpty()) {
                        suffixes.put(group.priority, suffix);
                    }
                }
                if (!suffixes.isEmpty()) {
                    return suffixes.get(0);
                } else {
                    return "";
                }
            }
        }
    }
}
