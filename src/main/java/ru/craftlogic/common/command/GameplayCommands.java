package ru.craftlogic.common.command;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import ru.craftlogic.api.CraftSounds;
import ru.craftlogic.api.command.*;
import ru.craftlogic.api.command.CommandContext.Argument;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.util.WrappedPlayerEnderchest;
import ru.craftlogic.api.util.WrappedPlayerInventory;
import ru.craftlogic.api.world.*;
import ru.craftlogic.common.inventory.InterfaceVirtualWorkbench;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class GameplayCommands implements CommandRegistrar {
    @Nullable
    private static Location adjustBedLocation(Location l) {
        if (l != null) {
            BlockPos p = l.getPos();
            net.minecraft.world.World world = l.getWorld();
            IBlockState state = l.getBlockState();
            Block block = state.getBlock();
            if (block.isBed(state, world, p, null)) {
                p = block.getBedSpawnPosition(state, world, p, null);
                if (p != null) {
                    return new Location(world, p);
                }
            }
        }
        return null;
    }

    private static void teleportHome(CommandContext ctx, Player sender, GameProfile target, Location bedLocation) throws CommandException {
        if (bedLocation != null) {
            Consumer<Server> task = server -> {
                if (sender.isOnline()) {
                    sender.teleport(bedLocation);
                    if (sender.getId().equals(target.getId())) {
                        ctx.sendMessage(Text.translation("commands.home.teleport.you").green());
                    } else {
                        ctx.sendMessage(Text.translation("commands.home.teleport.other").green().arg(target.getName(), Text::darkGreen));
                    }
                }
            };
            double distance = bedLocation.distance(sender.getLocation());
            if (distance <= 200 || sender.hasPermission("commands.home.instant")) {
                task.accept(ctx.server());
            } else {
                int timeout = 5;
                Text<?, ?> message = sender.getId().equals(target.getId()) ?
                        Text.translation("tooltip.home_teleport") :
                        Text.translation("tooltip.home_teleport.other");
                sender.sendCountdown("home", message, timeout);
                UUID id = ctx.server().addDelayedTask(task, timeout * 1000 + 250);
                sender.addPendingTeleport(id);
            }
        } else {
            if (sender.getId().equals(target.getId())) {
                throw new CommandException("commands.home.missing.you");
            } else {
                throw new CommandException("commands.home.missing.other", target.getName());
            }
        }
    }

    @Command(name = "home", syntax = {
        "",
        "<target:Player>"
    }, opLevel = 1)
    public static void commandHome(CommandContext ctx) throws CommandException {
        Player sender = ctx.senderAsPlayer();
        OfflinePlayer target = ctx.has("target") ? ctx.get("target").asOfflinePlayer() : sender;
        if (target.isOnline()) {
            Location bedLocation = adjustBedLocation(target.asOnline().getBedLocation());
            teleportHome(ctx, sender, target.getProfile(), bedLocation);
        } else {
            PhantomPlayer fake = target.asPhantom(sender.getWorld());
            Location bedLocation = adjustBedLocation(fake.getBedLocation());
            teleportHome(ctx, sender, fake.getProfile(), bedLocation);
        }
    }

    @Command(name = "gamemode", aliases = "gm", syntax = {
        "<mode:GameMode>",
        "<mode:GameMode> <player:Player>",
        ""
    }, opLevel = 2)
    public static void commandGameMode(CommandContext ctx) throws CommandException {
        if (ctx.has("mode")) {
            GameType mode = GameType.parseGameTypeWithDefault(ctx.get("mode").asString(), GameType.SURVIVAL);
            String modeName = mode.getName();
            if (ctx.has("player")) {
                if (ctx.checkPermission(true, "commands.gamemode.others", 3)) {
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
        "off|on",
        "<player:Player>",
        "<player:Player> off|on"
    }, opLevel = 1)
    public static void commandFly(CommandContext ctx) throws CommandException {
        Player target = ctx.getIfPresent("player", Argument::asPlayer).orElse(ctx.senderAsPlayer());
        boolean fly = ctx.hasAction(0) ? ctx.action(0).equals("on") : !target.isFlyingAllowed();
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
    }, opLevel = 1)
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
    }, opLevel = 1)
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
    }, opLevel = 1)
    public static void commandCraft(CommandContext ctx) throws CommandException {
        Player target = ctx.has("target") ? ctx.get("target").asPlayer() : ctx.senderAsPlayer();
        target.openInteraction(new InterfaceVirtualWorkbench(target.getWorld().unwrap()));
    }

    @Command(name = "inventory", aliases = "inv", syntax = {
        "<target:Player>"
    }, opLevel = 2)
    public static void commandInventory(CommandContext ctx) throws CommandException {
        Player viewer = ctx.senderAsPlayer();
        OfflinePlayer target = ctx.get("target").asOfflinePlayer();
        World requesterWorld = viewer.getWorld();
        if (target.hasData(requesterWorld)) {
            viewer.openChestInventory(new WrappedPlayerInventory(viewer, target.asPhantom(requesterWorld)));
        } else {
            throw new CommandException("commands.inventory.no_data", target.getName());
        }
    }

    @Command(name = "enderchest", aliases = "ec", syntax = {
        "<target:Player>",
        ""
    }, opLevel = 2)
    public static void commandEnderchest(CommandContext ctx) throws CommandException {
        Player viewer = ctx.senderAsPlayer();
        OfflinePlayer target = ctx.has("target") ? ctx.get("target").asOfflinePlayer() : viewer;
        World requesterWorld = viewer.getWorld();
        if (target.hasData(requesterWorld)) {
            viewer.openChestInventory(new WrappedPlayerEnderchest(viewer, target.asPhantom(requesterWorld)));
        } else {
            throw new CommandException("commands.inventory.no_data", target.getName());
        }
    }

    @Command(name = "time", syntax = {
        "day|night",
        "day|night <world:World>",
        "set <value>",
        "set <value> <world:World>",
        "add <value>",
        "add <value> <world:World>",
        "add <value> <world:World> <unit:TimeUnit>",
        "query day|daytime|gametime",
        "query day|daytime|gametime <world:World>"
    }, opLevel = 2)
    public static void commandTime(CommandContext ctx) throws CommandException {
        switch (ctx.action(0)) {
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
                for (World world : getAffectedWorlds(ctx)) {
                    ctx.sendNotification(
                        Text.translation("commands.time.set")
                            .yellow()
                            .arg(world.getName(), Text::gold)
                            .argTranslate("%s", a -> {
                                if (phrase) {
                                    a.argTranslate("commands.time.set." + value, Text::gold);
                                } else {
                                    a.argTranslate("commands.time.ticks", b -> b.arg(time).gold());
                                }
                            })
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
                for (World world : getAffectedWorlds(ctx)) {
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
                World world = ctx.senderAsLocatable().getWorld();
                switch (ctx.action(1)) {
                    case "day": {
                        ctx.sendMessage(
                            Text.translation("commands.time.query.days")
                                .gray()
                                .arg(world.getTotalDays(), Text::darkGray)
                        );
                        break;
                    }
                    case "daytime": {
                        ctx.sendMessage(
                            Text.translation("commands.time.query")
                                .gray()
                                .arg(world.getCurrentDayTime(), Text::darkGray)
                        );
                        break;
                    }
                    case "gametime": {
                        ctx.sendMessage(
                            Text.translation("commands.time.query.total")
                                .gray()
                                .arg(world.getTotalTime(), Text::darkGray)
                        );
                        break;
                    }
                }
                break;
            }
            default: {
                String phrase = ctx.action(0);
                long time = parseTimePhrase(phrase);
                for (World world : getAffectedWorlds(ctx)) {
                    ctx.sendNotification(
                        Text.translation("commands.time.set").gray()
                            .arg(world.getName(), Text::darkGray)
                            .argTranslate("commands.time.set." + phrase, Text::darkGray)
                    );
                    world.setTotalTime(time);
                }
            }
        }
    }

    private static Set<World> getAffectedWorlds(CommandContext ctx) throws CommandException {
        CommandSender sender = ctx.sender();
        if (sender instanceof Server) {
            return ((Server) sender).getWorldManager().getAllLoaded();
        } else if (sender instanceof LocatableCommandSender) {
            return Collections.singleton(((LocatableCommandSender) sender).getWorld());
        } else if (ctx.has("world")) {
            return Collections.singleton(ctx.get("world").asWorld());
        } else {
            throw new CommandException("commands.generic.specifyWorld");
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
        return Arrays.asList("d", "day", "days", "h", "hour", "hours", "m", "minute", "minutes");
    }

    @ArgumentCompleter(type = "GameMode")
    public static List<String> completerGameMode(ArgumentCompletionContext ctx) {
        return Stream.of(GameType.values())
                .map(GameType::getName)
                .collect(Collectors.toList());
    }

    @ArgumentCompleter(type = "World")
    public static Set<String> completerWorld(ArgumentCompletionContext ctx) {
        return ctx.server().getWorldManager().getAllLoadedNames();
    }

    @ArgumentCompleter(type = "Player", isEntityName = true)
    public static Set<String> completerPlayer(ArgumentCompletionContext ctx) {
        return ctx.server().getPlayerManager().getAllOnlineNames();
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
