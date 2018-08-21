package ru.craftlogic.common.permission;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.api.util.ConfigurableManager;

import java.nio.file.Path;
import java.util.*;

public class GroupManager extends ConfigurableManager {
    final Map<String, Group> groups = new HashMap<>();
    final Map<Group, List<UserManager.User>> groupUsersCache = new HashMap<>();

    public GroupManager(PermissionManager permissionManager, Path configPath, Logger logger) {
        super(permissionManager.getServer(), configPath, logger);
    }

    @Override
    protected void load(JsonObject groups) {
        if (groups.size() == 0) {
            getLogger().warn("There's no groups to load! At all...");
            this.setDirty(true);
        }
        if (!groups.has("default")) {
            getLogger().warn("Default group is missing! Creating empty one...");
            groups.add("default", new JsonObject());
            this.setDirty(true);
        }
        Map<String, JsonObject> groupCache = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : groups.entrySet()) {
            if (!(entry.getValue() instanceof JsonObject)) {
                getLogger().error("Group '" + entry.getKey() + "' must be an object! Ignoring it...");
                continue;
            }
            groupCache.put(entry.getKey(), (JsonObject) entry.getValue());
        }
        for (Map.Entry<String, JsonObject> entry : groupCache.entrySet()) {
            String groupName = entry.getKey();
            JsonObject g = entry.getValue();
            String parentName = "default";
            if (g.has("parent") && !groupCache.containsKey(parentName = g.get("parent").getAsString())) {
                getLogger().error("Group '" + groupName + "' has undefined parent '" + parentName + "'! Ignoring it...");
            }
            if (parentName.equals(groupName) && !groupName.equals("default")) {
                getLogger().error("Group '" + groupName + "' cannot be a child of itself!");
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

    @Override
    protected void save(JsonObject groups) {
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
            Group parent = GroupManager.this.groups.get(this.parent);
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

        public Set<String> permissions() {
            Set<String> permissions = new HashSet<>(this.permissions);
            Group parent = this.parent();
            if (parent != null) {
                permissions.addAll(parent.permissions());
            }
            return permissions;
        }

        public Set<UserManager.User> users() {
            Set<UserManager.User> result = new HashSet<>();
            if (!this.name.equals("default")) {
                List<UserManager.User> cachedUsers = GroupManager.this.groupUsersCache.get(this);
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
}