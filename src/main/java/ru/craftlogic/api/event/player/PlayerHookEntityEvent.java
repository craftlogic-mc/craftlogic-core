package ru.craftlogic.api.event.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PlayerHookEntityEvent extends EntityEvent {
    private final EntityPlayer angler;

    public PlayerHookEntityEvent(Entity target, EntityPlayer angler) {
        super(target);
        this.angler = angler;
    }

    public EntityPlayer getAngler() {
        return angler;
    }
}
