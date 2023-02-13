package ru.craftlogic.api.event.player;

import com.mojang.authlib.GameProfile;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.net.SocketAddress;

@Cancelable
public class PlayerConnectionAttemptEvent extends Event {

    public final SocketAddress address;
    public final GameProfile profile;
    private String disconnectReason = "";

    public PlayerConnectionAttemptEvent(SocketAddress address, GameProfile profile) {
        this.address = address;
        this.profile = profile;
    }

    public void disconnect(String reason) {
        disconnectReason = reason;
        setCanceled(true);
    }

    public String getDisconnectReason() {
        return disconnectReason;
    }
}
