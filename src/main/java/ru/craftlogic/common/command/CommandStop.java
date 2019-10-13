package ru.craftlogic.common.command;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.CraftMessages;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.network.message.MessageServerStop;

public final class CommandStop extends CommandBase {
    CommandStop() {
        super("stop", 4,
            "",
            "<delay>",
            "<delay> <reconnect>"
        );
    }

    @Override
    protected void execute(CommandContext ctx) throws CommandException {
        int delay = ctx.has("delay") ? ctx.get("delay").asInt(0, 30) : 0;
        Server server = ctx.server();
        if (delay > 0) {
            int reconnect = ctx.has("reconnect") ? ctx.get("reconnect").asInt(0, 15 * 60) : 60;
            ctx.sendNotification("commands.stop.delayed", CraftMessages.parseDuration(delay * 1000L).build());
            server.broadcastPacket(new MessageServerStop(delay, reconnect));
            server.addDelayedTask(this::stopServer, delay * 1000L);
        } else {
            ctx.sendNotification("commands.stop.start");
            stopServer(server);
        }
    }

    private void stopServer(Server server) {
        server.unwrap().initiateShutdown();
    }
}
