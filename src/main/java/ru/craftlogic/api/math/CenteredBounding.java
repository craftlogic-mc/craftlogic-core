package ru.craftlogic.api.math;

public class CenteredBounding implements Bounding {
    private final double x, y, z;
    private final double width, depth, height;

    public CenteredBounding(double x, double y, double z, double width, double height, double depth) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    @Override
    public double getStartX() {
        return this.x - this.width;
    }

    @Override
    public double getStartY() {
        return this.y - this.height;
    }

    @Override
    public double getStartZ() {
        return this.z - this.depth;
    }

    @Override
    public double getEndX() {
        return this.x + this.width;
    }

    @Override
    public double getEndY() {
        return this.y + this.height;
    }

    @Override
    public double getEndZ() {
        return this.z + this.depth;
    }
}
