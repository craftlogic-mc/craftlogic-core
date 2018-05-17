package ru.craftlogic.common.permission;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.management.PlayerProfileCache;
import ru.craftlogic.api.command.*;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.text.TextTranslation;
import ru.craftlogic.api.world.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PermissionCommands implements CommandRegisterer {
    @Command(name = "perm", syntax = {
        "group <group:PermissionGroup> [create|prefix|suffix|addPerm|delPerm] <value>...",
        "group <group:PermissionGroup> [create|delete|info|prefix|suffix|users]",
        "user <username:CachedUsername> [addGroup|delGroup|prefix|suffix] <value>...",
        "user <username:CachedUsername> [info|groups|prefix|suffix]"
    }, aliases = "permissions")
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
                        throw new CommandException("commands.perm.group.create.exists", groupName);
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
                                    throw new WrongUsageException("commands.perm.usage");
                            }
                        }
                    }
                    group = permissionManager.new Group(groupName, parent, new ArrayList<>(), null, null, priority);
                    permissionManager.groups.put(groupName, group);
                    permissionManager.save(true);
                    ctx.sendMessage("commands.perm.group.create.success", groupName);
                } else {
                    if (group == null) {
                        throw new CommandException("commands.perm.group.notFound", groupName);
                    } else {
                        switch (ctx.action()) {
                            case "info":
                                PermissionManager.Group parent = group.parent();
                                ctx.sendMessage(
                                    new TextTranslation("commands.perm.info.group.0")
                                        .gray()
                                        .argText(groupName, Text::darkGray)
                                );
                                ctx.sendMessage(
                                    new TextTranslation("commands.perm.info.group.1")
                                        .argText(parent == null ? "~not set~" : parent.name(), Text::darkGray)
                                );
                                ctx.sendMessage(
                                    new TextTranslation("commands.perm.info.group.2")
                                        .argText(group.prefix())
                                );
                                ctx.sendMessage(
                                    new TextTranslation("commands.perm.info.group.3")
                                        .argText(group.suffix())
                                );
                                ctx.sendMessage("commands.perm.info.group.4");

                                for (String s : group.permissions()) {
                                    ctx.sendMessage(
                                        new TextTranslation("commands.generic.list.entry")
                                            .argText(s, d ->
                                                d.darkGray().suggestCommand("/perm group " + groupName + " delPerm " + s)
                                            )
                                    );
                                }
                                break;
                            case "delete":
                                if (groupName.equals("default")) {
                                    throw new CommandException("commands.perm.group.delete.unable");
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
                OfflinePlayer player = ctx.server().getOfflinePlayerByName(username);
                if (player != null) {
                    PermissionManager.User user = permissionManager.getUser(player);

                } else {
                    throw new CommandException("commands.generic.userNeverPlayed", username);
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
}
