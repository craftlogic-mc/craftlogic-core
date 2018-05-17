package ru.craftlogic.api.event.player;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class PlayerJoinedMessageEvent extends Event {
    private final EntityPlayerMP player;
    private ITextComponent message;

    public PlayerJoinedMessageEvent(EntityPlayerMP player, ITextComponent message) {
        this.player = player;
        this.message = message;
    }

    public EntityPlayerMP getPlayer() {
        return player;
    }

    public ITextComponent getMessage() {
        return message;
    }

    public void setMessage(ITextComponent message) {
        this.message = message;
    }
}
