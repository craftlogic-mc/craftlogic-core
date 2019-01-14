package ru.craftlogic.mixin.server;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.server.AdvancedServer;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.network.message.MessageServerCrash;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements AdvancedServer {
    @Shadow private PlayerList playerList;
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
}
