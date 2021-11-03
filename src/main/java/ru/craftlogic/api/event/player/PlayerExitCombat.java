package ru.craftlogic.api.event.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerExitCombat extends PlayerEvent {
    public PlayerExitCombat(EntityPlayer player) {
        super(player);
    }
}
