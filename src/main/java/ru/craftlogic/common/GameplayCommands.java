package ru.craftlogic.common;

import net.minecraft.command.CommandException;
import net.minecraft.world.GameType;
import ru.craftlogic.api.command.*;
import ru.craftlogic.api.world.OnlinePlayer;

import java.util.ArrayList;
import java.util.List;

public class GameplayCommands implements CommandContainer {
    @Command(name = "gamemode", aliases = "gm", syntax = {
        "<mode:GameMode>",
        "<mode:GameMode> <player:Player>",
        ""
    })
    public static void commandGameMode(CommandContext ctx) throws CommandException {
        if (ctx.has("mode")) {
            GameType mode = GameType.parseGameTypeWithDefault(ctx.get("mode").asString(), GameType.SURVIVAL);
            if (ctx.has("player")) {
                if (ctx.checkPermissions("commands.gamemode.others")) {
                    OnlinePlayer target = ctx.get("player").asPlayer();
                    target.setGameMode(mode);
                }
            } else {
                OnlinePlayer sender = ctx.senderAsPlayer();
                sender.setGameMode(mode);
            }
        } else {
            OnlinePlayer sender = ctx.senderAsPlayer();
            sender.setGameMode(GameType.getByID((sender.getGameMode().ordinal() + 1) % GameType.values().length));
        }
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
}
