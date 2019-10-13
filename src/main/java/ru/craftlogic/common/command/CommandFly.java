package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.Player;

public final class CommandFly extends CommandBase {
    CommandFly() {
        super("fly", 1,
            "",
            "off|on",
            "<player:Player>",
            "<player:Player> off|on"
        );
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        Player target = ctx.senderAsPlayerOrArg("player");
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
}
