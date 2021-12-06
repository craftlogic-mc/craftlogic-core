package ru.craftlogic.mixin.server;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.server.AdvancedServer;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.world.CommandSender;
import ru.craftlogic.network.message.MessageServerCrash;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements AdvancedServer {
    @Shadow private PlayerList playerList;
    @Shadow @Final public ICommandManager commandManager;
    private final Server wrapper = new Server((MinecraftServer)(Object)this);

    @Override
    public Server wrapped() {
        return wrapper;
    }

    @Inject(method = "stopServer", at = @At(value = "HEAD"))
    public void onStopped(CallbackInfo info) {
        wrapper.stop(Server.StopReason.CORE);
    }

    @Inject(method = "finalTick", at = @At(value = "HEAD"))
    public void onCrashed(CallbackInfo info) {
        int delay = CraftConfig.tweaks.crashReconnectDelay;
        for (EntityPlayerMP player : this.playerList.getPlayers()) {
            CraftAPI.NETWORK.sendTo(player, new MessageServerCrash(delay));
        }
    }

    /**
     * @author Radviger
     * @reason Do not tab-complete spectator players without permission
     */
    @Overwrite
    public List<String> getTabCompletions(ICommandSender sender, String input, @Nullable BlockPos pos, boolean hasTargetBlock) {
        List<String> result = new ArrayList<>();
        boolean isCommand = input.startsWith("/");
        if (isCommand) {
            input = input.substring(1);
        }

        if (!isCommand && !hasTargetBlock) {
            String[] args = input.split(" ", -1);
            String firstArg = args[args.length - 1];
            List<EntityPlayerMP> players = this.playerList.getPlayers();
            CommandSender s = CommandSender.from(wrapper, sender);
            for (EntityPlayerMP player : players) {
                if (player.interactionManager.getGameType() != GameType.SPECTATOR || s.hasPermission("command.completion.spectators")) {
                    result.add(player.getName());
                }
            }
        } else {
            boolean hasNoSpaces = !input.contains(" ");
            List<String> variants = this.commandManager.getTabCompletions(sender, input, pos);
            if (!variants.isEmpty()) {
                for (String variant : variants) {
                    if (hasNoSpaces && !hasTargetBlock) {
                        result.add("/" + variant);
                    } else {
                        result.add(variant);
                    }
                }
            }
        }
        return result;
    }
}
