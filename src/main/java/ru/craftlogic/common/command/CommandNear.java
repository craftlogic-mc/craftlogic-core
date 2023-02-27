package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.Player;

import java.util.*;

public final class CommandNear extends CommandBase {
    CommandNear() {
        super("near", 2, Arrays.asList(
            new Syntax("", "commands.near"),
            new Syntax("<range>", "commands.near.admin")));
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        Player sender = ctx.senderAsPlayer();
        EntityPlayerMP e = sender.getEntity();
        int range = ctx.getIfPresent("range", CommandContext.Argument::asInt).orElse(150);
        Map<Float, String> near = new TreeMap<>();
        for (EntityPlayer player : e.world.playerEntities) {
            float d = player.getDistance(e);
            if (d <= range && player.getClass() == EntityPlayerMP.class && player != e) {
                near.put(d, player.getName());
            }
        }
        if (near.isEmpty()) {
            ctx.sendMessage("commands.near.nobody");
            return;
        }
        Text<?, ?> message = Text.translation("commands.near.players");
        boolean first = true;
        for (Map.Entry<Float, String> entry : near.entrySet()) {
            if (!first) {
                message.appendText(", ");
            } else {
                message.appendText(" ");
            }
            message.appendText(entry.getValue() + " (");
            int d = (int) entry.getKey().floatValue();
            message.appendText( d + "m", d <= 25 ? Text::red : (d <= 75 ? Text::yellow : Text::green));
            message.appendText(")");
            first = false;
        }
        ctx.sendMessage(message);
    }
}

