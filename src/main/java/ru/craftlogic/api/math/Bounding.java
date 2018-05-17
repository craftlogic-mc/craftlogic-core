package ru.craftlogic.api.math;

import ru.craftlogic.api.world.Location;

public interface Bounding {
    double getStartX();
    double getStartY();
    double getStartZ();
    double getEndX();
    double getEndY();
    double getEndZ();

    default boolean isOwning(Location location) {
        return this.isOwning(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    default boolean isOwning(double x, double y, double z) {
        return x >= getStartX() && x <= getEndX() && y >= getStartY() && y <= getEndY() && z >= getStartZ() && z <= getEndZ();
    }

    default boolean isIntersects(Bounding other) {
        return this.isIntersects(other.getStartX(), other.getStartY(), other.getStartZ(), other.getEndX(), other.getEndY(), other.getEndZ());
    }

    default boolean isIntersects(double startX, double startY, double startZ, double endX, double endY, double endZ) {
        return this.getStartX() < endX && this.getEndX() > startX && this.getStartY() < endY && this.getEndY() > startY && this.getStartZ() < endZ && this.getEndZ() > startZ;
    }
}
