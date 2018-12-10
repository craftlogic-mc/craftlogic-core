package ru.craftlogic.mixin.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerLoginServer;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.CraftMessages;

@Mixin(NetHandlerLoginServer.class)
public abstract class MixinNetHandlerLoginServer {
    @Shadow @Final
    private MinecraftServer server;
    @Shadow @Final
    public NetworkManager networkManager;
    @Shadow
    private GameProfile loginGameProfile;

    @Redirect(method = "update()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/NetHandlerLoginServer;tryAcceptPlayer()V"))
    public void onUpdate(NetHandlerLoginServer self) {
        if (!this.loginGameProfile.isComplete()) {
            this.loginGameProfile = this.getOfflineProfile(this.loginGameProfile);
        }
        Text<?, ?> disconnectReason = CraftMessages.getDisconnectReason(this.server, this.networkManager, this.loginGameProfile);
        if (disconnectReason != null) {
            this.disconnect(disconnectReason.build());
        } else {
            this.tryAcceptPlayer();
        }
    }

    @Shadow
    protected abstract GameProfile getOfflineProfile(GameProfile profile);

    @Shadow
    public abstract void disconnect(final ITextComponent reason);

    @Shadow
    public abstract void tryAcceptPlayer();
}
