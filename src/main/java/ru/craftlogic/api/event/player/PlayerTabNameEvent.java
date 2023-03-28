package ru.craftlogic.api.event.player;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerTabNameEvent extends PlayerEvent {
    private ITextComponent name;

    public PlayerTabNameEvent(EntityPlayer player, ITextComponent name) {
        super(player);
        this.name = name;
    }

    public ITextComponent getName() {
        return name;
    }

    public void setName(ITextComponent name) {
        this.name = name;
    }
}
