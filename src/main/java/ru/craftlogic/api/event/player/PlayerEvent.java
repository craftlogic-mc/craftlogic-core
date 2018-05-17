package ru.craftlogic.api.event.player;

import ru.craftlogic.api.event.Event;
import ru.craftlogic.api.world.OfflinePlayer;
import ru.craftlogic.api.world.Player;

public abstract class PlayerEvent extends Event {
    private final OfflinePlayer player;

    public PlayerEvent(OfflinePlayer player) {
        this.player = player;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public static class Joined extends PlayerEvent {
        public Joined(Player player) {
            super(player);
        }

        @Override
        public Player getPlayer() {
            return (Player)super.getPlayer();
        }
    }

    public static class Left extends PlayerEvent {
        public Left(Player player) {
            super(player);
        }

        @Override
        public Player getPlayer() {
            return (Player)super.getPlayer();
        }
    }
}
