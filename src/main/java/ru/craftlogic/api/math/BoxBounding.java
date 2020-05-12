package ru.craftlogic.api.math;

import ru.craftlogic.api.world.Location;

public class BoxBounding implements Bounding {
    private final int startX, startY, startZ;
    private final int endX, endY, endZ;

    public BoxBounding(Location start, Location end) {
        int sx = start.getBlockX();
        int sy = start.getBlockY();
        int sz = start.getBlockZ();
        int ex = end.getBlockX();
        int ey = end.getBlockY();
        int ez = end.getBlockZ();
        this.startX = Math.min(sx, ex);
        this.startY = Math.min(sy, ey);
        this.startZ = Math.min(sz, ez);
        this.endX = Math.max(sx, ex);
        this.endY = Math.max(sy, ey);
        this.endZ = Math.max(sz, ez);
    }

    public BoxBounding(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
    }

    @Override
    public double getStartX() {
        return startX;
    }

    @Override
    public double getStartY() {
        return startY;
    }

    @Override
    public double getStartZ() {
        return startZ;
    }

    @Override
    public double getEndX() {
        return endX;
    }

    @Override
    public double getEndY() {
        return endY;
    }

    @Override
    public double getEndZ() {
        return endZ;
    }
}
