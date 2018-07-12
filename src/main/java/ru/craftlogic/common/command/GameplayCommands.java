package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import net.minecraft.world.GameType;
import net.minecraftforge.common.util.FakePlayer;
import org.apache.commons.lang3.StringUtils;
import ru.craftlogic.api.command.*;
import ru.craftlogic.api.command.CommandContext.Argument;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.text.TextTranslation;
import ru.craftlogic.api.util.WrappedPlayerEnderchest;
import ru.craftlogic.api.util.WrappedPlayerInventory;
import ru.craftlogic.api.world.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class GameplayCommands implements CommandRegisterer {
    @Command(name = "gamemode", aliases = "gm", syntax = {
        "<mode:GameMode>",
        "<mode:GameMode> <player:Player>",
        ""
    })
    public static void commandGameMode(CommandContext ctx) throws CommandException {
        if (ctx.has("mode")) {
            GameType mode = GameType.parseGameTypeWithDefault(ctx.get("mode").asString(), GameType.SURVIVAL);
            String modeName = mode.getName();
            if (ctx.has("player")) {
                if (ctx.checkPermissions(true, "commands.gamemode.others")) {
                    Player target = ctx.get("player").asPlayer();
                    target.setGameMode(mode);
                    ctx.sendMessage(
                        new TextTranslation("commands.gamemode.set.other")
                            .gray()
                            .arg(target.getDisplayName())
                            .argTranslate("commands.gamemode." + modeName, Text::darkGray)
                    );
                }
            } else {
                Player sender = ctx.senderAsPlayer();
                sender.setGameMode(mode);
                ctx.sendMessage(
                    new TextTranslation("commands.gamemode.set.self")
                        .gray()
                        .argTranslate("commands.gamemode." + modeName, Text::darkGray)
                );
            }
        } else {
            Player sender = ctx.senderAsPlayer();
            GameType oldMode = sender.getGameMode();
            GameType newMode = GameType.getByID((sender.getGameMode().getID() + 1) % (GameType.values().length - 1));
            sender.setGameMode(newMode);
            ctx.sendMessage(
                new TextTranslation("commands.gamemode.toggle")
                    .gray()
                    .argTranslate("commands.gamemode." + oldMode.getName(), Text::darkGray)
                    .argTranslate("commands.gamemode." + newMode.getName(), Text::darkGray)
            );
        }
    }

    @Command(name = "fly", syntax = {
        "",
        "[off|on]",
        "<player:Player>",
        "<player:Player> [off|on]"
    })
    public static void commandFly(CommandContext ctx) throws CommandException {
        Player target = ctx.getIfPresent("player", Argument::asPlayer).orElse(ctx.senderAsPlayer());
        boolean fly = ctx.hasAction() ? ctx.action().equals("on") : !target.isFlyingAllowed();
        target.setFlyingAllowed(fly);
        String mode = "commands.fly." + (fly ? "on" : "off");
        if (!ctx.has("player") || ctx.senderAsPlayer().equals(target)) {
            ctx.sendMessage(
                new TextTranslation("commands.fly.self")
                    .gray()
                    .argTranslate(mode, Text::darkGray)
            );
        } else {
            ctx.sendMessage(
                new TextTranslation("commands.fly.self")
                    .gray()
                    .argTranslate(mode, Text::darkGray)
                    .arg(target.getDisplayName())
            );
        }
    }

    @Command(name = "inventory", aliases = "inv", syntax = {
        "<player:Player>"
    })
    public static void commandInventory(CommandContext ctx) throws CommandException {
        Player viewer = ctx.senderAsPlayer();
        OfflinePlayer target = ctx.get("player").asOfflinePlayer();
        World requesterWorld = viewer.getWorld();
        if (target.loadData(requesterWorld, true)) {
            FakePlayer fakePlayer = target.asFake(requesterWorld);
            viewer.openChestInventory(new WrappedPlayerInventory(fakePlayer.inventory, viewer, target));
        } else {
            throw new CommandException("commands.inventory.no_data", target.getProfile().getName());
        }
    }

    @Command(name = "enderchest", aliases = "ec", syntax = {
        "<player:Player>",
        ""
    })
    public static void commandEnderchest(CommandContext ctx) throws CommandException {
        Player viewer = ctx.senderAsPlayer();
        OfflinePlayer target = ctx.has("player") ? ctx.get("player").asOfflinePlayer() : viewer;
        World requesterWorld = viewer.getWorld();
        if (target.loadData(requesterWorld, true)) {
            FakePlayer fakePlayer = target.asFake(requesterWorld);
            viewer.openChestInventory(new WrappedPlayerEnderchest(fakePlayer.getInventoryEnderChest(), viewer, target));
        } else {
            throw new CommandException("commands.inventory.no_data", target.getProfile().getName());
        }
    }

    @Command(name = "time", syntax = {
        "[day|night]",
        "set <value>",
        "add <value>",
        "add <value> <unit:TimeUnit>",
        "query [day|daytime|gametime]"
    })
    public static void commandTime(CommandContext ctx) throws CommandException {
        if (ctx.hasConstant()) {
            switch (ctx.constant()) {
                case "set": {
                    long time;
                    String value = ctx.get("value").asString().toLowerCase();
                    boolean phrase;
                    if (value.matches("d|day|n|night")) {
                        phrase = true;
                        time = parseTimePhrase(value);
                    } else {
                        phrase = false;
                        time = ctx.get("value").asInt();
                    }
                    for (World world : getAffectedWorlds(ctx.sender())) {
                        ctx.sendMessage(
                            new TextTranslation("commands.time.set")
                                .yellow()
                                .argTranslate("%s", a -> {
                                    if (phrase) {
                                        a.argTranslate("commands.time.set." + value, Text::gold);
                                    } else {
                                        a.argTranslate("commands.time.ticks", b -> b.arg(time).gold());
                                    }
                                })
                                .argText(world.getName(), Text::gold)
                        );
                        world.setTotalTime(time);
                    }
                    break;
                }
                case "add": {
                    long time;
                    if (ctx.has("unit")) {
                        time = parseTimeUnit(ctx.get("value").asInt(), ctx.get("unit").asString());
                    } else {
                        time = ctx.get("value").asInt();
                    }
                    for (World world : getAffectedWorlds(ctx.sender())) {
                        ctx.sendMessage(
                            new TextTranslation("commands.time.added")
                                .yellow()
                                .argTranslate("commands.time.ticks", b -> b.arg(time).gold())
                                .argText(world.getName(), Text::gold)
                        );
                        world.addTotalTime(time);
                    }
                    break;
                }
                case "query": {
                    World world = ctx.sender().getWorld();
                    switch (ctx.action()) {
                        case "day": {
                            ctx.sendMessage(
                                new TextTranslation("commands.time.query.days")
                                    .gray()
                                    .argText(String.valueOf(world.getTotalDays()), Text::darkGray)
                            );
                            break;
                        }
                        case "daytime": {
                            ctx.sendMessage(
                                new TextTranslation("commands.time.query")
                                    .gray()
                                    .argText(String.valueOf(world.getCurrentDayTime()), Text::darkGray)
                            );
                            break;
                        }
                        case "gametime": {
                            ctx.sendMessage(
                                new TextTranslation("commands.time.query.total")
                                    .gray()
                                    .argText(String.valueOf(world.getTotalTime()), Text::darkGray)
                            );
                            break;
                        }
                    }
                    break;
                }
            }
        } else {
            String phrase = ctx.action();
            long time = parseTimePhrase(phrase);
            for (World world : getAffectedWorlds(ctx.sender())) {
                ctx.sendMessage(
                    new TextTranslation("commands.time.set")
                        .yellow()
                        .argTranslate("commands.time.set." + phrase, Text::gold)
                        .argText(world.getName(), Text::gold)
                );
                world.setTotalTime(time);
            }
        }
    }

    private static Set<World> getAffectedWorlds(CommandSender sender) {
        if (sender instanceof Server) {
            return ((Server) sender).getLoadedWorlds();
        } else {
            return Collections.singleton(sender.getWorld());
        }
    }

    private static long parseTimePhrase(String phrase) {
        switch (phrase.toLowerCase()) {
            case "day":
                return 1000;
            case "night":
                return 13000;
            default:
                throw new IllegalArgumentException("Unknown time phrase: " + phrase);
        }
    }

    private static long parseTimeUnit(int value, String unit) throws CommandException {
        switch (unit.toLowerCase()) {
            case "d":
            case "day":
            case "days":
                return value * 24000;
            case "h":
            case "hour":
            case "hours":
                return value * 400;
            case "m":
            case "minute":
            case "minutes":
                return (int)((float)value * 20F / 3F);
            case "t":
            case "tick":
            case "ticks":
                return value;
        }
        throw new CommandException("commands.time.unknownUnit", unit);
    }

    @ArgumentCompleter(type = "TimeUnit")
    public static List<String> completerTimeUnit(ArgumentCompletionContext ctx) {
        List<String> result = new ArrayList<>();
        for (String mode : new String[] {"d", "day", "days", "h", "hour", "hours", "m", "minute", "minutes"}) {
            if (mode.startsWith(ctx.partialName())) {
                result.add(mode);
            }
        }
        return result;
    }

    @ArgumentCompleter(type = "GameMode")
    public static List<String> completerGameMode(ArgumentCompletionContext ctx) {
        List<String> result = new ArrayList<>();
        for (GameType mode : GameType.values()) {
            if (mode.getName().startsWith(ctx.partialName())) {
                result.add(mode.getName());
            }
        }
        return result;
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
            if (StringUtils.startsWithIgnoreCase(playerName, ctx.partialName())) {
                variants.add(playerName);
            }
        }
        return variants;
    }

    @ArgumentCompleter(type = {"XCoord", "YCoord", "ZCoord"})
    public static List<String> completerXCoord(ArgumentCompletionContext ctx) {
        if (ctx.partialName().isEmpty()) {
            Location location = ctx.targetBlockOrSelfLocation();
            int coord = -1;
            switch (ctx.type().substring(0, 1)) {
                case "X":
                    coord = location.getBlockX();
                    break;
                case "Y":
                    coord = location.getBlockY();
                    break;
                case "Z":
                    coord = location.getBlockZ();
                    break;
            }
            return singletonList(String.valueOf(coord));
        }
        return emptyList();
    }
}
