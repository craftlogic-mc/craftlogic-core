package ru.craftlogic.api.event;

public interface Cancellable {
    boolean isCancelled();
    void setCancelled(boolean value);
}
