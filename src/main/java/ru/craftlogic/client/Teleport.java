package ru.craftlogic.client;

import ru.craftlogic.api.world.Location;

public final class Teleport {
    private final Location target;
    private final int timeout;
    private final boolean freeze;
    private final long startTime = System.currentTimeMillis();

    public Teleport(Location target, int timeout, boolean freeze) {
        this.target = target;
        this.timeout = timeout;
        this.freeze = freeze;
    }

    public double getProgress() {
        return Math.min(1, (double) (System.currentTimeMillis() - startTime) / (double) (timeout * 1000));
    }

    public boolean isFreeze() {
        return freeze;
    }
}
