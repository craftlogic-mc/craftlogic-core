package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.CraftSounds;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.Player;

public final class CommandHeal extends CommandBase {
    CommandHeal() {
        super("heal", 1,
            "<target:Player>",
            ""
        );
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        Player target = ctx.senderAsPlayerOrArg("target");
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
}
