package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import net.minecraft.world.GameType;
import net.minecraftforge.common.util.FakePlayer;
import ru.craftlogic.api.command.*;
import ru.craftlogic.api.util.WrappedPlayerInventory;
import ru.craftlogic.api.world.OnlinePlayer;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.api.world.World;

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
                    OnlinePlayer target = ctx.get("player").asOnlinePlayer();
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

    @Command(name = "fly", syntax = {
        "",
        "<player:Player>",
        "<player:Player> [off|on]"
    })
    public void commandFly(CommandContext ctx) throws CommandException {
        OnlinePlayer target;
        if (ctx.has("player")) {
            target = ctx.get("player").asOnlinePlayer();
        } else {
            target = ctx.senderAsPlayer();
        }
        boolean fly;
        if (ctx.has("action_0")) {
            fly = ctx.action().equals("on");
        } else {
            fly = !target.isFlyingAllowed();
        }
        target.setFlyingAllowed(fly);
    }

    @Command(name = "inventory", aliases = "inv", syntax = {
        "<player:Player>"
    })
    public static void commandInventory(CommandContext ctx) throws CommandException {
        OnlinePlayer viewer = ctx.senderAsPlayer();
        Player target = ctx.get("player").asPlayer();
        World requesterWorld = viewer.getWorld();
        if (target.loadData(requesterWorld, true)) {
            FakePlayer fakePlayer = target.asFake(requesterWorld);
            viewer.openChestInventory(new WrappedPlayerInventory(fakePlayer.inventory, viewer, target));
        } else {
            throw new CommandException("commands.inventory.noDataFound", target.getProfile().getName());
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
