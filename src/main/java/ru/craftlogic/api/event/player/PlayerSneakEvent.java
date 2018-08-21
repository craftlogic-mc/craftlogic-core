package ru.craftlogic.api.event.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerSneakEvent extends PlayerEvent {
    public final boolean sneaking;

    public PlayerSneakEvent(EntityPlayer player, boolean sneaking) {
        super(player);
        this.sneaking = sneaking;
    }
}
