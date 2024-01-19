package ru.craftlogic.api.event.player;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerBlockDamageEvent extends PlayerEvent {
    private final float source;
    private float blocked;

    public PlayerBlockDamageEvent(EntityPlayer player, float source, float blocked) {
        super(player);
        this.source = source;
        this.blocked = blocked;
    }

    public float getSource() {
        return source;
    }

    public float getBlocked() {
        return blocked;
    }

    public void setBlocked(int blocked) {
        this.blocked = blocked;
    }


}
