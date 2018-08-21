package ru.craftlogic.common.command;

import net.minecraft.block.BlockWorkbench;
import net.minecraft.command.CommandException;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.GameType;
import net.minecraftforge.common.util.FakePlayer;
import org.apache.commons.lang3.StringUtils;
import ru.craftlogic.api.command.*;
import ru.craftlogic.api.command.CommandContext.Argument;
import ru.craftlogic.api.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.util.WrappedPlayerEnderchest;
import ru.craftlogic.api.util.WrappedPlayerInventory;
import ru.craftlogic.api.world.*;
import ru.craftlogic.api.CraftSounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class GameplayCommands implements CommandRegistrar {
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
                    ctx.sendNotification(
                        Text.translation("commands.gamemode.set.other").gray()
                            .arg(target.getDisplayName())
                            .argTranslate("commands.gamemode." + modeName, Text::darkGray)
                    );
                }
            } else {
                Player sender = ctx.senderAsPlayer();
                sender.setGameMode(mode);
                ctx.sendNotification(
                    Text.translation("commands.gamemode.set.self").gray()
                        .argTranslate("commands.gamemode." + modeName, Text::darkGray)
                );
            }
        } else {
            Player sender = ctx.senderAsPlayer();
            GameType oldMode = sender.getGameMode();
            GameType newMode = GameType.getByID((sender.getGameMode().getID() + 1) % (GameType.values().length - 1));
            sender.setGameMode(newMode);
            ctx.sendMessage(
                Text.translation("commands.gamemode.toggle").gray()
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
            ctx.sendNotification(
                Text.translation("commands.fly.self").gray()
                    .argTranslate(mode, Text::darkGray)
            );
        } else {
            ctx.sendNotification(
                Text.translation("commands.fly.self").gray()
                    .argTranslate(mode, Text::darkGray)
                    .arg(target.getDisplayName())
            );
        }
    }

    @Command(name = "heal", syntax = {
        "<target:Player>",
        ""
    })
    public static void commandHeal(CommandContext ctx) throws CommandException {
        Player target = ctx.has("target") ? ctx.get("target").asPlayer() : ctx.senderAsPlayer();
        if (target.heal()) {
            target.playSound(CraftSounds.HEAL, 1F, ctx.randomFloat(0.7F));
            if (ctx.sender() == target) {
                ctx.sendNotification(
                    Text.translation("commands.heal.self").gray()
                );
            } else {
                ctx.sendNotification(
                    Text.translation("commands.heal.other").gray()
                        .arg(target.getName(), Text::darkGray)
                );
            }
        } else {
            if (ctx.sender() == target) {
                throw new CommandException("commands.heal.self.not_hurt");
            } else {
                throw new CommandException("commands.heal.other.not_hurt", target.getName());
            }
        }
    }

    @Command(name = "feed", syntax = {
        "<target:Player>",
        ""
    })
    public static void commandFeed(CommandContext ctx) throws CommandException {
        Player target = ctx.has("target") ? ctx.get("target").asPlayer() : ctx.senderAsPlayer();
        if (target.feed()) {
            target.playSound(SoundEvents.ENTITY_PLAYER_BURP, 1F, ctx.randomFloat(0.7F));
            if (ctx.sender() == target) {
                ctx.sendNotification(
                    Text.translation("commands.feed.self").gray()
                );
            } else {
                ctx.sendNotification(
                    Text.translation("commands.feed.other").gray()
                        .arg(target.getName(), Text::darkGray)
                );
            }
        } else {
            if (ctx.sender() == target) {
                throw new CommandException("commands.feed.self.not_hungry");
            } else {
                throw new CommandException("commands.feed.other.not_hungry", target.getName());
            }
        }
    }

    @Command(name = "craft", syntax = {
        "",
        "<target:Player>"
    })
    public static void commandCraft(CommandContext ctx) throws CommandException {
        Player target = ctx.has("target") ? ctx.get("target").asPlayer() : ctx.senderAsPlayer();
        Location location = target.getLocation();
        target.openInteraction(new BlockWorkbench.InterfaceCraftingTable(location.getWorld(), location.getPos()));
    }

    @Command(name = "inventory", aliases = "inv", syntax = {
        "<target:Player>"
    })
    public static void commandInventory(CommandContext ctx) throws CommandException {
        Player viewer = ctx.senderAsPlayer();
        OfflinePlayer target = ctx.get("target").asOfflinePlayer();
        World requesterWorld = viewer.getWorld();
        if (target.loadData(requesterWorld, true)) {
            FakePlayer fakePlayer = target.asFake(requesterWorld);
            viewer.openChestInventory(new WrappedPlayerInventory(fakePlayer.inventory, viewer, target));
        } else {
            throw new CommandException("commands.inventory.no_data", target.getName());
        }
    }

    @Command(name = "enderchest", aliases = "ec", syntax = {
        "<target:Player>",
        ""
    })
    public static void commandEnderchest(CommandContext ctx) throws CommandException {
        Player viewer = ctx.senderAsPlayer();
        OfflinePlayer target = ctx.has("target") ? ctx.get("target").asOfflinePlayer() : viewer;
        World requesterWorld = viewer.getWorld();
        if (target.loadData(requesterWorld, true)) {
            FakePlayer fakePlayer = target.asFake(requesterWorld);
            viewer.openChestInventory(new WrappedPlayerEnderchest(fakePlayer.getInventoryEnderChest(), viewer, target));
        } else {
            throw new CommandException("commands.inventory.no_data", target.getName());
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
                        ctx.sendNotification(
                            Text.translation("commands.time.set")
                                .yellow()
                                .argTranslate("%s", a -> {
                                    if (phrase) {
                                        a.argTranslate("commands.time.set." + value, Text::gold);
                                    } else {
                                        a.argTranslate("commands.time.ticks", b -> b.arg(time).gold());
                                    }
                                })
                                .arg(world.getName(), Text::gold)
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
                        ctx.sendNotification(
                            Text.translation("commands.time.added")
                                .yellow()
                                .argTranslate("commands.time.ticks", b -> b.arg(time).gold())
                                .arg(world.getName(), Text::gold)
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
                                Text.translation("commands.time.query.days")
                                    .gray()
                                    .arg(String.valueOf(world.getTotalDays()), Text::darkGray)
                            );
                            break;
                        }
                        case "daytime": {
                            ctx.sendMessage(
                                Text.translation("commands.time.query")
                                    .gray()
                                    .arg(String.valueOf(world.getCurrentDayTime()), Text::darkGray)
                            );
                            break;
                        }
                        case "gametime": {
                            ctx.sendMessage(
                                Text.translation("commands.time.query.total")
                                    .gray()
                                    .arg(String.valueOf(world.getTotalTime()), Text::darkGray)
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
                ctx.sendNotification(
                    Text.translation("commands.time.set").gray()
                        .arg(world.getName(), Text::darkGray)
                        .argTranslate("commands.time.set." + phrase, Text::darkGray)
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
    public static List<String> completerCoord(ArgumentCompletionContext ctx) {
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
