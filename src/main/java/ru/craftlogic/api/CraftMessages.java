package ru.craftlogic.api;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.*;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.text.TextString;
import ru.craftlogic.api.text.TextTranslation;
import ru.craftlogic.api.world.Location;

import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CraftMessages {
    public static Text<?, ?> getDisconnectReason(MinecraftServer _server, NetworkManager networkManager, GameProfile profile) {
        Server server = Server.from(_server);
        PlayerList playerList = _server.getPlayerList();
        SocketAddress remoteAddress = networkManager.getRemoteAddress();
        UserListBans bannedPlayers = playerList.getBannedPlayers();
        UserListIPBans bannedIps = playerList.getBannedIPs();
        if (bannedPlayers.isBanned(profile)) {
            UserListBansEntry entry = bannedPlayers.getEntry(profile);
            if (entry.getBanEndDate().before(new Date())) {
                bannedPlayers.removeEntry(profile);
                return null;
            }
            return getBanMessage(entry, false);
        } else if (bannedIps.isBanned(remoteAddress)) {
            UserListIPBansEntry entry = bannedIps.getBanEntry(remoteAddress);
            return getBanMessage(entry, true);
        } else if (!playerList.canJoin(profile)) {
            return getWhitelistMessage(playerList, profile);
        } else if (playerList.getPlayers().size() >= playerList.getMaxPlayers() && !playerList.bypassesPlayerLimit(profile)) {
            return getFullServerMessage(playerList, profile);
        }
        return null;
    }

    public static Text<?, ?> getBanMessage(UserListEntryBan entry, boolean ip) {
        boolean temp = entry.getBanEndDate() != null;
        TextTranslation msg = Text.translation("disconnect." + (ip ? "ip-banned" : "banned") + "." + (temp ? "temp" : "permanent"))
            .appendText("\n")
            .appendTranslate("disconnect.banned.reason", r -> r.arg(entry.getBanReason(), Text::darkRed));
        if (temp) {
            msg.appendText("\n")
               .appendTranslate("disconnect.banned.expiration", e ->
                   e.arg(Text.date(entry.getBanEndDate(), new SimpleDateFormat(CraftConfig.banDateFormat)).darkRed())
               );
        }
        return msg.red();
    }

    public static Text<?, ?> getWhitelistMessage(PlayerList playerList, GameProfile profile) {
        return Text.translation("disconnect.whitelist").red();
    }

    public static Text<?, ?> getFullServerMessage(PlayerList playerList, GameProfile profile) {
        return Text.translation("disconnect.full").red();
    }

    public static void notifyCommandListener(MinecraftServer server, ICommandSender sender, ICommand command, int flags, ITextComponent info) {
        PlayerList playerList = server.getPlayerList();
        WorldServer overworld = server.worlds[0];

        ITextComponent message = new TextComponentTranslation("chat.type.admin", sender.getName(), info);
        Style style = message.getStyle();
        style.setColor(TextFormatting.GRAY);
        style.setItalic(true);

        boolean canBeSentByConsole = !(sender instanceof MinecraftServer) || server.shouldBroadcastConsoleToOps();
        boolean canBeSentByRCon = !(sender instanceof RConConsoleSource) || server.shouldBroadcastRconToOps();

        if (sender.sendCommandFeedback() && canBeSentByConsole && canBeSentByRCon) {
            for (EntityPlayerMP player : playerList.getPlayers()) {
                if (player != sender && playerList.canSendCommands(player.getGameProfile())
                        && command.checkPermission(server, sender)) {

                    player.sendMessage(message);
                }
            }
        }

        if (sender != server && overworld.getGameRules().getBoolean("logAdminCommands")) {
            server.sendMessage(message);
        }

        boolean sendCommandFeedback = overworld.getGameRules().getBoolean("sendCommandFeedback");
        if (sender instanceof CommandBlockBaseLogic) {
            sendCommandFeedback = ((CommandBlockBaseLogic)sender).shouldTrackOutput();
        }

        if ((flags & 1) != 1 && sendCommandFeedback || sender instanceof MinecraftServer) {
            sender.sendMessage(info);
        }
    }

    public static String pluralify(int count, String single, String pluralA, String pluralB) {
        int h = count % 100;
        int t = count % 10;
        if (h > 10 && h < 20) {
            return pluralB;
        }
        if (t > 1 && t < 5) {
            return pluralA;
        }
        if (t == 1) {
            return single;
        }
        return pluralB;
    }

    public static Text<?, ?> parseCoordinates(Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        float yaw = location.getYaw();
        float pitch = location.getPitch();
        return Text.string("(" + x + ", " + y + ", " + z + ")")
                .runCommand("/tp " + x + " " + y + " " + z + " " + yaw + " " + pitch);
    }

    public static Text<?, ?> parseDuration(long delta) {
        TextString time = Text.string();

        long second = 1000;
        long minute = 60 * second;
        long hour = 60 * minute;
        long day = 24 * hour;
        long month = 30 * day;
        long year = 12 * month;

        boolean prev = false;

        if (appendTimeMeasure(delta, year, "year", time, false)) {
            delta %= year;
            prev = true;
        }
        if (prev |= appendTimeMeasure(delta, month, "month", time, prev)) {
            delta %= month;
        }
        if (prev |= appendTimeMeasure(delta, day, "day", time, prev)) {
            delta %= day;
        }
        if (prev |= appendTimeMeasure(delta, hour, "hour", time, prev)) {
            delta %= hour;
        }
        if (prev |= appendTimeMeasure(delta, minute, "minute", time, prev)) {
            delta %= minute;
        }
        appendTimeMeasure(delta, second, "second", time, prev);

        return time;
    }

    private static boolean appendTimeMeasure(long delta, long measure, String name, TextString target, boolean prev) {
        if (delta >= measure) {
            int m = (int) (delta / measure);
            if (prev) {
                target.appendText(" ");
            }
            target.appendTranslate(pluralify(m,
                "commands.generic." + name + ".single",
                "commands.generic." + name + ".plural.a",
                "commands.generic." + name + ".plural.b"
            ), t -> t.arg(m));
            return true;
        }
        return false;
    }

    public static Text<?, ?> format(String format, Map<String, String> args) {
        return format(0, format, args);
    }

    private static Text<?, ?> format(int level, String format, Map<String, String> args) {
        List<Text<?, ?>> children = new ArrayList<>();
        List<String> parts = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        boolean escape = false;
        int l = level;
        for (int i = 0; i < format.length(); i++) {
            char c = format.charAt(i);
            switch (c) {
                case '{': {
                    if (!escape) {
                        l++;
                        if (builder.length() > 0) {
                            if (level == 0) {
                                children.add(Text.string(builder.toString()));
                            } else {
                                parts.add(builder.toString());
                            }
                            builder = new StringBuilder();
                        }
                        Text<?, ?> child = format(l, format.substring(i + 1), args);
                        children.add(child);
                    } else {
                        builder.append(c);
                    }
                    break;
                }
                case '}': {
                    if (!escape) {
                        l--;
                    } else {
                        builder.append(c);
                    }
                    break;
                }
                case '\\': {
                    if (escape) {
                        builder.append(c);
                    }
                    escape = !escape;
                    break;
                }
                default: {
                    if (escape) {
                        escape = false;
                    }
                    builder.append(c);
                }
            }
        }
        if (level != l) {
            throw new IllegalArgumentException("Unbalanced curly brackets at (level: " + level + "; l: " + l +")" );
        }
        if (level == 0) {
            children.add(Text.string(builder.toString()));

            Text<?, ?> result = Text.string();
            for (Text<?, ?> part : children) {
                result.append(part);
            }
            return result;
        } else {
            parts.add(builder.toString());

            String[] split = String.join("%s", parts).split("\\|", 1);

            String var = args.getOrDefault(split[0], "");
            String[] modifiers = split[1].split("\\|");

            return applyModifiers(var, modifiers, children);
        }
    }

    private static Text<?, ?> applyModifiers(String var, String[] modifiers, List<Text<?, ?>> args) {
        Text<?, ?> result = Text.string(var);
        int i = 0;
        String arg = "";
        for (String modifier : modifiers) {
            int start = modifier.indexOf('(');
            if (start != -1) {
                if (modifier.endsWith(")")) {
                    arg = modifier.substring(start + 1, modifier.length() - 1);
                    modifier = modifier.substring(0, start);
                } else {
                    throw new IllegalArgumentException("Unbalanced parentheses at: " + start);
                }
            }
            TextFormatting formatting = TextFormatting.getValueByName(modifier);
            if (formatting != null) {
                if (formatting.isColor()) {
                    result = result.color(formatting);
                } else {
                    switch (formatting) {
                        case OBFUSCATED:
                            result = result.obfuscated();
                            break;
                        case BOLD:
                            result = result.bold();
                            break;
                        case STRIKETHROUGH:
                            result = result.strikethrough();
                            break;
                        case UNDERLINE:
                            result = result.underlined();
                            break;
                        case ITALIC:
                            result = result.underlined();
                            break;
                        case RESET:
                            result = result.reset();
                            break;
                    }
                }
            } else if (start != -1) {
                while (arg.contains("%s")) {
                    Text<?, ?> a = args.get(i++);
                    arg = arg.replaceFirst("%s", a.build().getFormattedText());
                }
                switch (modifier) {
                    case "tooltip": {
                        result.hoverText(arg);
                        break;
                    }
                    case "execute": {
                        result.runCommand(arg);
                        break;
                    }
                    case "suggest": {
                        result.suggestCommand(arg);
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Unknown functional modifier: " + modifier);
                    }
                }
            } else {
                throw new IllegalArgumentException("Unknown modifier: " + modifier);
            }
        }
        return result;
    }
}