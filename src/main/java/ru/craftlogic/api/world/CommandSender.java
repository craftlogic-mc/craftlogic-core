package ru.craftlogic.api.world;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.entity.Permissible;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.util.WrappedCommandSender;

public interface CommandSender extends Permissible, Locatable {
    default Server getServer() {
        ICommandSender handle = getHandle();
        if (handle.getServer() == CraftLogic.getServer().getHandle()) {
            return CraftLogic.getServer();
        } else {
            throw new IllegalStateException("Unknown server instance: " + handle.getServer());
        }
    }

    @Override
    default ITextComponent getDisplayName() {
        return getHandle().getDisplayName();
    }

    @Override
    default Location getLocation() {
        ICommandSender handle = getHandle();
        return new Location(handle.getEntityWorld(), handle.getPositionVector());
    }

    default void sendMessage(ITextComponent message) {
        getHandle().sendMessage(message);
    }

    default void sendMessage(Text<?, ?> text) {
        getHandle().sendMessage(text.build());
    }

    default void sendMessage(String format, Object... args) {
        getHandle().sendMessage(new TextComponentTranslation(format, args));
    }

    ICommandSender getHandle();

    static CommandSender from(Server server, ICommandSender sender) {
        if (sender instanceof EntityPlayerMP) {
            return server.getPlayer(((EntityPlayerMP) sender).getGameProfile());
        } else if (sender instanceof MinecraftServer) {
            if (CraftLogic.getServer().getHandle() == sender) {
                return CraftLogic.getServer();
            }
        } else if (sender instanceof CommandBlockBaseLogic) {
            return new WrappedCommandSender(sender) {
                @Override
                public boolean hasPermissions(String... permissions) {
                    return true;
                }
            };
        }
        return new WrappedCommandSender(sender);
    }

    default World getWorld() {
        net.minecraft.world.World nmw = getHandle().getEntityWorld();
        return getServer().getWorld(Dimension.fromVanilla(nmw.provider.getDimensionType()));
    }
}
