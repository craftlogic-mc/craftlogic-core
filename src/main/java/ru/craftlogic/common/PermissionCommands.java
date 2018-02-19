package ru.craftlogic.common;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.management.PlayerProfileCache;
import ru.craftlogic.api.command.*;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class PermissionCommands implements CommandContainer {
    @Command(name = "permissions", syntax = {
        "group <group:PermissionGroup> [create|prefix|suffix|addPerm|delPerm] <value>...",
        "group <group:PermissionGroup> [create|delete|info|prefix|suffix|users]",
        "user <username:CachedUsername> [addGroup|delGroup|prefix|suffix] <value>...",
        "user <username:CachedUsername> [info|groups|prefix|suffix]"
    }, aliases = "perm")
    public static void commandPerm(CommandContext ctx) throws Exception {
        String firstConst = ctx.constant();
        PermissionManager permissionManager = ctx.server().getPermissionManager();
        PlayerProfileCache profileCache = ctx.server().getProfileCache();
        switch (firstConst) {
            case "group":
                String groupName = ctx.get("group").asString();
                PermissionManager.Group group = permissionManager.getGroup(groupName);
                if (ctx.action().equals("create")) {
                    if (group != null) {
                        ctx.failure("commands.perm.group.create.exists", groupName);
                    }
                    int priority = 0;
                    String parent = "default";
                    if (ctx.has("value")) {
                        String value = ctx.get("value").asString();
                        if (value.contains(" ")) {
                            String[] vals = value.split(" ");
                            switch (vals.length) {
                                case 2:
                                    priority = Integer.parseInt(vals[1]);
                                case 1:
                                    parent = vals[0];
                                    break;
                                default:
                                    ctx.failure("commands.perm.usage");
                            }
                        }
                    }
                    group = permissionManager.new Group(groupName, parent, new ArrayList<>(), null, null, priority);
                    permissionManager.groups.put(groupName, group);
                    permissionManager.save(true);
                    ctx.sendMessage("commands.perm.group.create.success", groupName);
                } else {
                    if (group == null) {
                        ctx.failure("commands.perm.group.notFound", groupName);
                    } else {
                        switch (ctx.action()) {
                            case "info":
                                PermissionManager.Group parent = group.parent();
                                ctx.sendMessage("commands.perm.info.group.0", groupName);
                                ctx.sendMessage("commands.perm.info.group.1", parent == null ? "~not set~" : parent.name());
                                ctx.sendMessage("commands.perm.info.group.2", group.prefix());
                                ctx.sendMessage("commands.perm.info.group.3", group.suffix());
                                ctx.sendMessage("commands.perm.info.group.4");
                                for (String s : group.permissions()) {
                                    ctx.sendMessage(s);
                                }
                                break;
                            case "delete":
                                if (groupName.equals("default")) {
                                    ctx.failure("commands.perm.group.delete.unable");
                                } else {
                                    permissionManager.groups.remove(groupName, group);
                                    permissionManager.save(true);
                                    ctx.sendMessage("commands.perm.group.delete.success", groupName);
                                }
                                break;
                            case "prefix":
                                if (!ctx.has("value")) {
                                    ctx.sendMessage("commands.perm.info.prefix", groupName, group.prefix());
                                } else {
                                    group.prefix = ctx.get("value").asString();
                                    permissionManager.save(true);
                                    ctx.sendMessage("commands.perm.info.prefix.set", groupName, group.prefix());
                                }
                                break;
                            case "suffix":
                                if (!ctx.has("value")) {
                                    ctx.sendMessage("commands.perm.info.suffix", groupName, group.suffix());
                                } else {
                                    group.suffix = ctx.get("value").asString();
                                    permissionManager.save(true);
                                    ctx.sendMessage("commands.perm.info.suffix.set", groupName, group.suffix());
                                }
                                break;
                            case "users":
                                if (groupName.equalsIgnoreCase("default")) {
                                    ctx.sendMessage("commands.perm.info.users.everyone");
                                } else {
                                    List<String> users = new ArrayList<>();
                                    for (PermissionManager.User user : group.users()) {
                                        UUID id = user.id();
                                        GameProfile profile = profileCache.getProfileByUUID(id);
                                        if (profile != null && profile.getName() != null) {
                                            users.add(id.toString() + " (" + profile.getName() + ")");
                                        } else {
                                            users.add(id.toString());
                                        }
                                    }
                                    ctx.sendMessage("commands.perm.info.users", users.toString());
                                }
                                break;
                            case "addPerm": {
                                String perm = ctx.get("value").asString();
                                if (group.permissions.add(perm)) {
                                    ctx.sendMessage("commands.perm.permadd.success", groupName, perm);
                                } else {
                                    ctx.sendMessage("commands.perm.permadd.unable", groupName, perm);
                                }
                                break;
                            }
                            case "delPerm": {
                                String perm = ctx.get("value").asString();
                                if (group.permissions.remove(perm)) {
                                    ctx.sendMessage("commands.perm.permdel.success", groupName, perm);
                                } else {
                                    ctx.sendMessage("commands.perm.permdel.unable", groupName, perm);
                                }
                            }
                        }
                    }
                }
                break;
            case "user":
                String username = ctx.get("username").asString();
                Player player = ctx.server().getOfflinePlayerByName(username);
                if (player != null) {
                    PermissionManager.User user = permissionManager.getUser(player);

                } else {
                    ctx.failure("commands.perm.user.notfound");
                }
        }
    }

    @ArgumentCompleter(type = "PermissionGroup")
    public static List<String> completerPermGroup(ArgumentCompletionContext ctx) {
        String[] groupsNames = ctx.server().getPermissionManager().getAllGroups();
        List<String> variants = ctx.partialName().isEmpty() ? new ArrayList<>(groupsNames.length) : new ArrayList<>();
        for (String groupName : groupsNames) {
            if (groupName.startsWith(ctx.partialName())) {
                variants.add(groupName);
            }
        }
        return variants;
    }

    @ArgumentCompleter(type = "World")
    public static List<String> completerWorld(ArgumentCompletionContext ctx) {
        Set<World> worlds = ctx.server().getLoadedWorlds();
        List<String> variants = ctx.partialName().isEmpty() ? new ArrayList<>(worlds.size()) : new ArrayList<>();
        for (World world : worlds) {
            String worldName = world.getName();
            if (worldName.startsWith(ctx.partialName())) {
                variants.add(worldName);
            }
        }
        return variants;
    }

    @ArgumentCompleter(type = "Player", isEntityName = true)
    public static List<String> completerPlayer(ArgumentCompletionContext ctx) {
        String[] onlinePlayerNames = ctx.server().getOnlinePlayerNames();
        List<String> variants = ctx.partialName().isEmpty() ? new ArrayList<>(onlinePlayerNames.length) : new ArrayList<>();
        for (String playerName : onlinePlayerNames) {
            if (playerName.startsWith(ctx.partialName())) {
                variants.add(playerName);
            }
        }
        return variants;
    }

    @ArgumentCompleter(type = "CachedUsername", isEntityName = true)
    public static List<String> completerUsername(ArgumentCompletionContext ctx) {
        String[] usernames = ctx.server().getProfileCache().getUsernames();
        List<String> variants = ctx.partialName().isEmpty() ? new ArrayList<>(usernames.length) : new ArrayList<>();
        for (String username : usernames) {
            if (username.startsWith(ctx.partialName())) {
                variants.add(username);
            }
        }
        return variants;
    }

    @ArgumentCompleter(type = "XCoord")
    public static List<String> completerXCoord(ArgumentCompletionContext ctx) {
        if (ctx.partialName().isEmpty()) {
            return singletonList(String.valueOf((ctx.targetBlock() != null ? ctx.targetBlock() : ctx.sender().getPosition()).getX()));
        }
        return emptyList();
    }

    @ArgumentCompleter(type = "YCoord")
    public static List<String> completerYCoord(ArgumentCompletionContext ctx) {
        if (ctx.partialName().isEmpty()) {
            return singletonList(String.valueOf((ctx.targetBlock() != null ? ctx.targetBlock() : ctx.sender().getPosition()).getY()));
        }
        return emptyList();
    }

    @ArgumentCompleter(type = "ZCoord")
    public static List<String> completerZCoord(ArgumentCompletionContext ctx) {
        if (ctx.partialName().isEmpty()) {
            return singletonList(String.valueOf((ctx.targetBlock() != null ? ctx.targetBlock() : ctx.sender().getPosition()).getZ()));
        }
        return emptyList();
    }
}
