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
        return isOwning(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    default boolean isOwning(double x, double y, double z) {
        return x >= getStartX() && x <= getEndX() && y >= getStartY() && y <= getEndY() && z >= getStartZ() && z <= getEndZ();
    }

    default boolean isIntersects(Bounding other) {
        return isIntersects(other.getStartX(), other.getStartY(), other.getStartZ(), other.getEndX(), other.getEndY(), other.getEndZ());
    }

    default boolean isIntersects(double startX, double startY, double startZ, double endX, double endY, double endZ) {
        double sx = Math.min(startX, endX);
        double sy = Math.min(startY, endY);
        double sz = Math.min(startZ, endZ);
        double ex = Math.max(startX, endX);
        double ey = Math.max(startY, endY);
        double ez = Math.max(startZ, endZ);
        return getStartX() <= ex && getEndX() >= sx && getStartY() <= ey && getEndY() >= sy && getStartZ() <= ez && getEndZ() >= sz;
    }
}
