package ru.craftlogic.mixin.entity;

import net.minecraft.command.CommandException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.world.CommandSender;

import javax.annotation.Nullable;

@Mixin(EntitySelector.class)
public abstract class MixinEntitySelector {
    @Shadow
    @Nullable
    public static <T extends Entity> T matchOneEntity(ICommandSender sender, String token, Class<? extends T> targetClass) throws CommandException {
        return null;
    }

    /**
     * @author Radviger
     * @reason Do not allow targeting spectator players in commands
     */
    @Overwrite
    @Nullable
    public static EntityPlayerMP matchOnePlayer(ICommandSender sender, String token) throws CommandException {
        EntityPlayerMP player = matchOneEntity(sender, token, EntityPlayerMP.class);
        if (player != null && player.interactionManager.getGameType() == GameType.SPECTATOR) {
            CommandSender s = CommandSender.from(Server.from(player.server), player);
            if (!s.hasPermission("command.completion.spectators")) {
                return null;
            }
        }
        return player;
    }
}
