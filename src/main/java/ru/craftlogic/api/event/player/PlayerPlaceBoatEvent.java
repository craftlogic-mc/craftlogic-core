package ru.craftlogic.api.event.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PlayerPlaceBoatEvent extends PlayerEvent {
    public final RayTraceResult target;
    public final boolean atWater;

    public PlayerPlaceBoatEvent(EntityPlayer player, RayTraceResult target, boolean atWater) {
        super(player);
        this.target = target;
        this.atWater = atWater;
    }
}
