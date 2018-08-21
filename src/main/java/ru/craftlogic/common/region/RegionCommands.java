package ru.craftlogic.common.region;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.command.*;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.OfflinePlayer;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.common.region.RegionManager.Region;

import java.util.*;

public class RegionCommands implements CommandRegistrar {
    @Command(name = "home", syntax = {
        "[create|delete]",
        "[invite|expel|ban] <player:Player>",
        "flag <flag:RegionFlag>",
        "flag <flag:RegionFlag> <value>...",
        "<player:Player>",
        ""
    })
    public static void commandHome(CommandContext ctx) throws Exception {
        Player sender = ctx.senderAsPlayer();
        RegionManager regionManager = ctx.server().getRegionManager();
        Location location = sender.getLocation();
        if (ctx.has("player") & ctx.hasAction()) {
            String player = ctx.get("player").asString();
            OfflinePlayer target = ctx.server().getOfflinePlayerByName(player);
            if (target != null && target.getId() != null) {
                UUID targetId = target.getId();
                String targetName = target.getName();
                if (targetId.equals(sender.getProfile().getId())) {
                    throw new CommandException("commands.home.player.yourself");
                }
                switch (ctx.action()) {
                    case "invite": {
                        Region region = regionManager.getRegion(sender.getWorld().getName(), sender.getId());
                        if (region != null) {
                            if (!region.members.contains(targetId)) {
                                region.members.add(targetId);
                                regionManager.save(true);
                                sender.sendMessage("commands.home.invite.success", targetName);
                            } else {
                                throw new CommandException("commands.home.invite.already", targetName);
                            }
                        } else {
                            throw new CommandException("commands.home.missing", location.getWorldName());
                        }
                        break;
                    }
                    case "expel": {
                        Region region = regionManager.getRegion(sender.getWorld().getName(), sender.getId());
                        if (region != null) {
                            if (region.members.contains(targetId)) {
                                region.members.remove(targetId);
                                regionManager.save(true);
                                ctx.sendMessage("commands.home.expel.success", targetName);
                            } else {
                                throw new CommandException("commands.home.expel.already", targetName);
                            }
                        } else {
                            throw new CommandException("commands.home.missing", location.getWorldName());
                        }
                        break;
                    }
                    case "ban": {
                        throw new CommandException("commands.home.ban.unsupported");
                    }
                }
            } else {
                throw new CommandException("commands.generic.player.notFound", player);
            }
        } else if (ctx.hasConstant() && ctx.constant().equals("flag")) {
            String flag = ctx.get("flag").asString();
            if (ctx.has("value")) {
                String value = ctx.get("value").asString();
            } else {

            }
        } else if (ctx.hasAction()) {
            Region region = regionManager.getRegion(sender.getWorld().getName(), sender.getProfile().getId());
            if (ctx.action().equals("create")) {
                if (region != null) {
                    throw new CommandException("commands.home.create.already", location.getWorldName());
                } else {
                    int radius = 24;
                    List<Region> intersects = regionManager.getRegions(location, radius);
                    if (intersects.isEmpty()) {
                        UUID owner = sender.getProfile().getId();
                        region = regionManager.new Region(owner, location, radius, new ArrayList<>(), new HashMap<>());
                        regionManager.regions.computeIfAbsent(location.getWorldName(), k -> new HashMap<>())
                                             .put(owner, region);
                        regionManager.save(true);
                        ctx.sendMessage("commands.home.create.success");
                    } else {
                        throw new CommandException("commands.home.create.intersects", radius);
                    }
                }
            } else if (region == null) {
                throw new CommandException("commands.home.missing", location.getWorldName());
            } else {
                regionManager.regions.get(location.getWorldName()).remove(sender.getProfile().getId(), region);
                ctx.sendMessage("commands.home.delete.success");
                regionManager.save(true);
            }
        } else {
            String targetPlayerName = sender.getProfile().getName();
            Region region;
            if (ctx.has("player")) {
                targetPlayerName = ctx.get("player").asString();
                OfflinePlayer targetPlayer = ctx.get("player").asOfflinePlayer();
                if (targetPlayer != null && targetPlayer.getId() != null) {
                    region = regionManager.getRegion(location.getWorldName(), targetPlayer.getId());
                    if (region != null && !region.members.contains(sender.getProfile().getId())) {
                        throw new CommandException("commands.home.accessDenied", targetPlayerName);
                    }
                } else {
                    throw new CommandException("commands.generic.player.notFound", targetPlayerName);
                }
            } else {
                region = regionManager.getRegion(sender.getWorld().getName(), sender.getProfile().getId());
            }
            if (region == null) {
                throw new CommandException("commands.home.missing", location.getWorldName());
            } else {
                if (sender.teleport(region.center)) {
                    if (region.owner.equals(sender.getProfile().getId())) {
                        ctx.sendMessage("commands.home.welcome");
                    } else {
                        ctx.sendMessage("commands.home.welcome.other", targetPlayerName);
                    }
                }
            }

        }
    }

    @ArgumentCompleter(type = "RegionFlag")
    public static List<String> completerRegFlag(ArgumentCompletionContext ctx) {
        return Collections.emptyList();
    }
}
