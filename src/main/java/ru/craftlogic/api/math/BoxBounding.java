package ru.craftlogic.api.math;

public class BoxBounding implements Bounding {
    private final int startX, startY, startZ;
    private final int endX, endY, endZ;

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
