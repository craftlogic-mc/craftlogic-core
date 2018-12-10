package ru.craftlogic.mixin.server;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.server.AdvancedServer;
import ru.craftlogic.api.server.Server;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements AdvancedServer {
    private final Server wrapper = new Server((MinecraftServer)(Object)this);

    @Override
    public Server wrapped() {
        return wrapper;
    }

    @Inject(method = "stopServer", at = @At(value = "HEAD"))
    public void onStopped(CallbackInfo info) {
        wrapper.stop(Server.StopReason.CORE);
    }
}
