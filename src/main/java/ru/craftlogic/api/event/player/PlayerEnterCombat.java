package ru.craftlogic.api.event.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerEnterCombat extends PlayerEvent {
    public PlayerEnterCombat(EntityPlayer player) {
        super(player);
    }
}
