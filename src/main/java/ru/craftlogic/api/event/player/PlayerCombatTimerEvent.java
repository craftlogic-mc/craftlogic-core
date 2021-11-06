package ru.craftlogic.api.event.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerCombatTimerEvent extends PlayerEvent {
    public PlayerCombatTimerEvent(EntityPlayer player, EntityPlayer attacker, DamageSource source) {
        super(player);
    }
}
