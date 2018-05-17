package ru.craftlogic.common.region;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandException;
import ru.craftlogic.api.command.*;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.Player;

import java.util.*;

public class RegionCommands implements CommandRegisterer {
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
            GameProfile targetProfile = ctx.server().getOfflinePlayerByName(player).getProfile();
            if (targetProfile != null && targetProfile.getId() != null) {
                UUID targetId = targetProfile.getId();
                if (targetId.equals(sender.getProfile().getId())) {
                    throw new CommandException("commands.home.player.yourself");
                }
                RegionManager.Region region = regionManager.getRegion(sender.getWorld().getName(), targetId);
                if (region != null) {
                    switch (ctx.action()) {
                        case "invite":
                            if (!region.members.contains(targetId)) {
                                region.members.add(targetId);
                                regionManager.save(true);
                                throw new CommandException("commands.home.invite.success", targetProfile.getName());
                            } else {
                                throw new CommandException("commands.home.invite.already", targetProfile.getName());
                            }
                        case "expel":
                            if (region.members.contains(targetId)) {
                                region.members.remove(targetId);
                                ctx.sendMessage("commands.home.expel.success", targetProfile.getName());
                                regionManager.save(true);
                            } else {
                                throw new CommandException("commands.home.expel.already", targetProfile.getName());
                            }
                            break;
                        case "ban":
                            throw new CommandException("commands.home.ban.unsupported");
                    }
                } else {
                    throw new CommandException("commands.home.missing", location.getWorldName());
                }
            } else {
                throw new CommandException("commands.home.player.notFound", player);
            }
        } else if (ctx.hasConstant() && ctx.constant().equals("flag")) {
            String flag = ctx.get("flag").asString();
            if (ctx.has("value")) {
                String value = ctx.get("value").asString();
            } else {

            }
        } else if (ctx.hasAction()) {
            RegionManager.Region region = regionManager.getRegion(sender.getWorld().getName(), sender.getProfile().getId());
            if (ctx.action().equals("create")) {
                if (region != null) {
                    throw new CommandException("commands.home.create.already");
                } else {
                    int radius = 24;
                    List<RegionManager.Region> intersects = regionManager.getRegions(location, radius);
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
            RegionManager.Region region = null;
            if (ctx.has("player")) {
                targetPlayerName = ctx.get("player").asString();
                GameProfile targetPlayer = ctx.server().getOfflinePlayerByName(targetPlayerName).getProfile();
                if (targetPlayer != null && targetPlayer.getId() != null) {
                    region = regionManager.getRegion(location.getWorldName(), targetPlayer.getId());
                    if (region != null && !region.members.contains(sender.getProfile().getId())) {
                        throw new CommandException("commands.home.accessDenied", targetPlayerName);
                    }
                } else {
                    throw new CommandException("commands.home.player.notFound", targetPlayerName);
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
