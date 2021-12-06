package ru.craftlogic.mixin.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.world.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collection;

@Mixin(GuiPlayerTabOverlay.class)
public class MixinGuiPlayerTabOverlay {
    @Shadow @Final private Minecraft mc;

    /*@Redirect(
        method = "renderPlayerlist",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/NetworkPlayerInfo;getGameType()Lnet/minecraft/world/GameType;")
    )
    public GameType getGameType(NetworkPlayerInfo info) {
        GameType type = info.getGameType();
        if (type == GameType.SPECTATOR) { //DO NOT COMPROMISE SPECTATOR PLAYER
            return GameType.SURVIVAL;
        } else {
            return type;
        }
    }*/

    @Redirect(
        method = "renderPlayerlist",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/NetHandlerPlayClient;getPlayerInfoMap()Ljava/util/Collection;")
    )
    public Collection<NetworkPlayerInfo> getPlayerInfo(NetHandlerPlayClient handler) {
        ArrayList<NetworkPlayerInfo> result = new ArrayList<>(handler.getPlayerInfoMap());
        result.removeIf(info -> info.getGameType() == GameType.SPECTATOR && mc.playerController.getCurrentGameType() != GameType.SPECTATOR);
        return result;
    }
}
