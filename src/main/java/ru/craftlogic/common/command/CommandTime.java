package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.CommandSender;
import ru.craftlogic.api.world.LocatableCommandSender;
import ru.craftlogic.api.world.World;

import java.util.Collections;
import java.util.Set;

public final class CommandTime extends CommandBase {
    CommandTime() {
        super("time", 2,
            "day|night",
            "day|night <world:World>",
            "set <value>",
            "set <value> <world:World>",
            "add <value>",
            "add <value> <world:World>",
            "add <value> <world:World> <unit:TimeUnit>",
            "query day|daytime|gametime",
            "query day|daytime|gametime <world:World>"
        );
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
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
                        Text.translation("commands.time.set")
                            .yellow()
                            .arg(world.getName(), Text::gold)
                            .argTranslate("commands.time.set." + phrase, Text::gold)
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

    private long parseTimePhrase(String phrase) {
        switch (phrase.toLowerCase()) {
            case "day":
                return 1000;
            case "night":
                return 13000;
            default:
                throw new IllegalArgumentException("Unknown time phrase: " + phrase);
        }
    }

    private long parseTimeUnit(int value, String unit) throws CommandException {
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
}
