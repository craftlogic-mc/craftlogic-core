package ru.craftlogic.api.math;

public class RadiusBounding implements Bounding {
    private final double x, y, z;
    private final double radius;

    public RadiusBounding(double x, double y, double z, double radius) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
    }

    @Override
    public double getStartX() {
        return this.x - this.radius;
    }

    @Override
    public double getStartY() {
        return this.y - this.radius;
    }

    @Override
    public double getStartZ() {
        return this.z - this.radius;
    }

    @Override
    public double getEndX() {
        return this.x + this.radius;
    }

    @Override
    public double getEndY() {
        return this.y + this.radius;
    }

    @Override
    public double getEndZ() {
        return this.z + this.radius;
    }
}
