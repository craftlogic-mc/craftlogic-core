package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.server.management.UserListIPBansEntry;
import net.minecraft.util.text.ITextComponent;
import ru.craftlogic.api.CraftMessages;
import ru.craftlogic.api.command.*;
import ru.craftlogic.api.command.CommandContext.Argument;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.server.WorldManager;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.*;
import ru.craftlogic.network.message.MessageStopServer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ManagementCommands implements CommandRegistrar {
    @Command(name = "stop", syntax = {
        "",
        "<delay>",
        "<delay> <reconnect>"
    })
    public static void commandStop(CommandContext ctx) throws CommandException {
        int delay = ctx.has("delay") ? ctx.get("delay").asInt(0, 30) : 0;
        Server server = ctx.server();
        if (delay > 0) {
            int reconnect = ctx.has("reconnect") ? ctx.get("reconnect").asInt(0, 15 * 60) : 60;
            ctx.sendNotification("commands.stop.start_delayed", CraftMessages.parseDuration(delay * 1000L).build());
            server.broadcastPacket(new MessageStopServer(delay, reconnect));
            server.addDelayedTask(ManagementCommands::stopServer, delay * 1000L);
        } else {
            ctx.sendNotification("commands.stop.start");
            stopServer(server);
        }
    }

    private static void stopServer(Server server) {
        server.unwrap().initiateShutdown();
    }

    @Command(name = "op", syntax = {
        "<username:OfflinePlayer>",
        "<username:OfflinePlayer> <level>",
        "<username:OfflinePlayer> <level> <bypassPlayerLimit>"
    }, serverOnly = true)
    public static void commandOp(CommandContext ctx) throws CommandException {
        OfflinePlayer target = ctx.get("username").asOfflinePlayer();
        int level = ctx.getIfPresent("level", a -> a.asInt(1, 4)).orElse(1);
        boolean bypassPlayerLimit = ctx.getIfPresent("bypassPlayerLimit", Argument::asBoolean).orElse(false);

        boolean success = target.setOperator(true, level, bypassPlayerLimit);
        ctx.sendNotification("commands.op." + (success ? "success" : "failed"), target.getDisplayName());
    }

    @Command(name = "deop", syntax = "<username:OfflinePlayer>", serverOnly = true)
    public static void commandDeOp(CommandContext ctx) throws CommandException {
        OfflinePlayer target = ctx.get("username").asOfflinePlayer();
        boolean success = target.setOperator(false, -1, false);
        ctx.sendNotification("commands.deop." + (success ? "success" : "failed"), target.getDisplayName());
    }

    @Command(name = "ops", syntax = {
        "",
        "<level>"
    }, serverOnly = true)
    public static void commandOpList(CommandContext ctx) throws CommandException {
        int level = ctx.getIfPresent("level", a -> a.asInt(1, 4)).orElse(1);

        Set<OfflinePlayer> operators = ctx.server().getPlayerManager().getOperators(level);

        if (operators.isEmpty()) {
            if (level != 1) {
                throw new CommandException("commands.ops.not_found", level);
            } else {
                throw new CommandException("commands.ops.empty");
            }
        }

        ctx.sendMessage("commands.ops.header");

        for (OfflinePlayer operator : operators) {
            boolean b = operator.isBypassesPlayerLimit();
            ITextComponent displayName = operator.getDisplayName();

            ctx.sendMessage(
                Text.translation("commands.ops.entry")
                    .arg(displayName, a -> a.gold().runCommand("/deop " + operator.getName()))
                    .arg(operator.getOperatorLevel())
                    .argTranslate("commands.generic." + (b ? "yes" : "no"), b ? Text::green : Text::red)
            );
        }
    }

    @Command(name = "seen", syntax = "<username:OfflinePlayer>", opLevel = 1)
    public static void commandSeen(CommandContext ctx) throws CommandException {
        OfflinePlayer player = ctx.get("username").asOfflinePlayer();
        if (player.isOnline()) {
            Text<?, ?> coordinates = CraftMessages.parseCoordinates(player.asOnline().getLocation());
            ctx.sendMessage(
                Text.translation("commands.seen.online").yellow()
                    .arg(player.getName(), Text::gold)
                    .arg(coordinates.gold())
            );
        } else {
            WorldManager worldManager = ctx.server().getWorldManager();
            World world = ctx.sender() instanceof LocatableCommandSender ? ctx.senderAsLocatable().getWorld() : worldManager.get(Dimension.OVERWORLD);
            PhantomPlayer fakePlayer = player.asPhantom(world);
            Location lastLocation = fakePlayer.getLocation();
            long lastPlayed = fakePlayer.getLastPlayed();
            if (lastPlayed != 0 && lastLocation != null) {
                Text<?, ?> coordinates = CraftMessages.parseCoordinates(lastLocation);
                Text<?, ?> time = CraftMessages.parseDuration(System.currentTimeMillis() - lastPlayed);
                ctx.sendMessage(
                    Text.translation("commands.seen.offline").yellow()
                        .arg(fakePlayer.getName(), Text::gold)
                        .arg(coordinates.gold())
                        .arg(time.gold())
                );
            } else {
                throw new CommandException("commands.inventory.no_data", player.getName());
            }
        }
    }

    @Command(name = "tempban", syntax = {
        "<username:OfflinePlayer> <time> <reason>...",
        "<username:OfflinePlayer> <time>"
    }, serverOnly = true, opLevel = 3)
    public static void commandTempBan(CommandContext ctx) throws CommandException {
        OfflinePlayer player = ctx.get("username").asOfflinePlayer();
        Date expirationDate = new Date(System.currentTimeMillis() + parseDuration(ctx.get("time").asString()));
        String reason = ctx.getIfPresent("reason", Argument::asString).orElse(null);
        banUser(ctx, player, expirationDate, reason);
    }

    @Command(name = "ban", syntax = {
        "<username:OfflinePlayer> <reason>...",
        "<username:OfflinePlayer>"
    }, serverOnly = true, opLevel = 3)
    public static void commandBan(CommandContext ctx) throws CommandException {
        OfflinePlayer player = ctx.get("username").asOfflinePlayer();
        String reason = ctx.getIfPresent("reason", Argument::asString).orElse(null);
        banUser(ctx, player, null, reason);
    }

    @Command(name = "tempban-ip", syntax = {
        "<address> <time> <reason>...",
        "<address> <time>"
    }, serverOnly = true)
    public static void commandTempBanIP(CommandContext ctx) throws CommandException {
        String address = ctx.get("address").asIP();
        Date expirationDate = new Date(System.currentTimeMillis() + parseDuration(ctx.get("time").asString()));
        String reason = ctx.getIfPresent("reason", Argument::asString).orElse(null);
        banIP(ctx, address, expirationDate, reason);
    }

    @Command(name = "ban-ip", syntax = {
        "<address> <reason>...",
        "<address>"
    }, serverOnly = true)
    public static void commandBanIP(CommandContext ctx) throws CommandException {
        String address = ctx.get("address").asIP();
        String reason = ctx.getIfPresent("reason", Argument::asString).orElse(null);
        banIP(ctx, address, null, reason);
    }

    private static void banUser(CommandContext ctx, OfflinePlayer player, Date expirationDate, String reason) {
        UserListBansEntry bansEntry = new UserListBansEntry(player.getProfile(), null, ctx.server().getName(), expirationDate, reason);
        PlayerList playerList = ctx.server().unwrap().getPlayerList();
        playerList.getBannedPlayers().addEntry(bansEntry);

        if (player.isOnline()) {
            Player p = player.asOnline();
            p.disconnect(CraftMessages.getBanMessage(bansEntry, false));
        }

        ctx.sendNotification(
            Text.translation("commands.ban.success").yellow()
                .arg(player.getName(), Text::gold)
        );
    }

    private static void banIP(CommandContext ctx, String address, Date expirationDate, String reason) {
        UserListIPBansEntry bansEntry = new UserListIPBansEntry(address, null, ctx.server().getName(), expirationDate, reason);

        PlayerList playerList = ctx.server().unwrap().getPlayerList();
        playerList.getBannedIPs().addEntry(bansEntry);

        List<String> users = new ArrayList<>();

        for (Player player : ctx.server().getPlayerManager().getAllOnline()) {
            if (address.equals(player.getIP())) {
                users.add(player.getName());
                player.disconnect(CraftMessages.getBanMessage(bansEntry, true));
            }
        }

        if (users.isEmpty()) {
            ctx.sendNotification(
                Text.translation("commands.banip.success").yellow()
                    .arg(address, Text::gold)
            );
        } else {
            ctx.sendNotification(
                Text.translation("commands.banip.success.players").yellow()
                    .arg(address, Text::gold)
                    .arg(String.join(", ", users), Text::gold)
            );
        }
    }

    public static long parseDuration(String part) throws CommandException {
        float time = 0;
        String type = part.substring(part.length() - 1);
        double t = Double.parseDouble(part.substring(0, part.length() - 1));
        switch (type) {
            case "s": {
                time += t;
                break;
            }
            case "m": {
                time += 60 * t;
                break;
            }
            case "h": {
                time += 60 * 60 * t;
                break;
            }
            case "d": {
                time += 24 * 60 *60 * t;
                break;
            }
            case "w": {
                time += 7 * 24 * 60 * 60 * t;
            }
        }
        if(time <= 0) {
            throw new CommandException("commands.generic.wrongTimeFormat", part);
        }
        return (long)(time * 1000);
    }

    @ArgumentCompleter(type = "OfflinePlayer", isEntityName = true)
    public static Set<String> completerUsername(ArgumentCompletionContext ctx) {
        return ctx.server().getPlayerManager().getAllOfflineNames();
    }
}
