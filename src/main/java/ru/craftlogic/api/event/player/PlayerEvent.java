package ru.craftlogic.api.event.player;

import net.minecraft.util.text.ITextComponent;
import ru.craftlogic.api.event.Event;
import ru.craftlogic.api.world.OnlinePlayer;
import ru.craftlogic.api.world.Player;

public abstract class PlayerEvent extends Event {
    private final Player player;

    public PlayerEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public static class Joined extends PlayerEvent {
        private ITextComponent message;

        public Joined(OnlinePlayer player, ITextComponent message) {
            super(player);
            this.message = message;
        }

        public ITextComponent getMessage() {
            return message;
        }

        public void setMessage(ITextComponent message) {
            this.message = message;
        }

        @Override
        public OnlinePlayer getPlayer() {
            return (OnlinePlayer)super.getPlayer();
        }
    }

    public static class Left extends PlayerEvent {
        private ITextComponent message;

        public Left(OnlinePlayer player, ITextComponent message) {
            super(player);
            this.message = message;
        }

        public ITextComponent getMessage() {
            return message;
        }

        public void setMessage(ITextComponent message) {
            this.message = message;
        }

        @Override
        public OnlinePlayer getPlayer() {
            return (OnlinePlayer)super.getPlayer();
        }
    }
}
