package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import net.minecraft.world.GameType;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.Player;

public final class CommandGameMode extends CommandBase {
    CommandGameMode() {
        super("gamemode", 2,
            "<mode:GameMode>",
            "<mode:GameMode> <player:Player>",
            ""
        );
        this.aliases.add("gm");
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        if (ctx.has("mode")) {
            GameType mode = GameType.parseGameTypeWithDefault(ctx.get("mode").asString(), GameType.SURVIVAL);
            String modeName = mode.getName();
            Player target = ctx.senderAsPlayerOrArg("player");
            if (!ctx.has("player") || ctx.senderAsPlayer().equals(target)) {
                if (ctx.checkPermission(true, "commands.gamemode." + mode.getName(), 2)) {
                    target.setGameMode(mode);
                    ctx.sendNotification(
                        Text.translation("commands.gamemode.set.self").gray()
                            .argTranslate("commands.gamemode." + modeName, Text::darkGray)
                    );
                }
            } else {
                if (ctx.checkPermission(true, "commands.gamemode.other", 3)) {
                    target.setGameMode(mode);
                    ctx.sendNotification(
                        Text.translation("commands.gamemode.set.other").gray()
                            .arg(target.getDisplayName())
                            .argTranslate("commands.gamemode." + modeName, Text::darkGray)
                    );
                }
            }
        } else {
            Player sender = ctx.senderAsPlayer();
            GameType oldMode = sender.getGameMode();
            GameType newMode = GameType.getByID((sender.getGameMode().getID() + 1) % (GameType.values().length - 1));
            if (ctx.checkPermission(true, "commands.gamemode." + newMode.getName(), 2)) {
                sender.setGameMode(newMode);
                ctx.sendMessage(
                    Text.translation("commands.gamemode.toggle").gray()
                        .argTranslate("commands.gamemode." + oldMode.getName(), Text::darkGray)
                        .argTranslate("commands.gamemode." + newMode.getName(), Text::darkGray)
                );
            }
        }
    }
}
