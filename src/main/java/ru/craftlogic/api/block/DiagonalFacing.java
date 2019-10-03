package ru.craftlogic.api.block;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.util.math.Vec3i;

import java.util.*;

public enum DiagonalFacing {
    NORTH(false, new Vec3i(0, 0, -1), new double[]{0.0D}),
    SOUTH(NORTH, false, new Vec3i(0, 0, 1), new double[]{0.5D}),
    EAST(false, new Vec3i(1, 0, 0), new double[]{0.25D}),
    WEST(EAST, false, new Vec3i(-1, 0, 0), new double[]{0.75D}),
    NORTH_EAST(true, new Vec3i(1, 0, -1), new double[]{0.125D}, NORTH, EAST),
    NORTH_WEST(true, new Vec3i(-1, 0, -1), new double[]{0.875D}, NORTH, WEST),
    SOUTH_EAST(NORTH_WEST, true, new Vec3i(1, 0, 1), new double[]{0.375D}, SOUTH, EAST),
    SOUTH_WEST(NORTH_EAST, true, new Vec3i(-1, 0, 1), new double[]{0.625D}, SOUTH, WEST);

    private DiagonalFacing opposite;
    private final Vec3i offset;
    private final boolean diagonal;
    private final List<DiagonalFacing> incompatibleFacings;
    private final double[] rotationsFromStandardNorth;

    DiagonalFacing(DiagonalFacing opposite, boolean diagonal, Vec3i offset, double[] rotationsFromStandardNorth, DiagonalFacing... incompatiblePrevious) {
        this.opposite = opposite;
        opposite.opposite = this;
        this.incompatibleFacings = Collections.unmodifiableList(Arrays.asList(incompatiblePrevious));
        this.diagonal = diagonal;
        this.offset = offset;
        this.rotationsFromStandardNorth = rotationsFromStandardNorth;
    }

    DiagonalFacing(boolean diagonal, Vec3i offset, double[] rotationsFromStandardNorth, DiagonalFacing... incompatiblePrevious) {
        this.incompatibleFacings = Collections.unmodifiableList(Arrays.asList(incompatiblePrevious));
        this.diagonal = diagonal;
        this.offset = offset;
        this.rotationsFromStandardNorth = rotationsFromStandardNorth;
    }

    public boolean isIncompatible(List<DiagonalFacing> currentFacings) {
        if (!currentFacings.isEmpty() && !this.incompatibleFacings.isEmpty()) {
            return !Collections.disjoint(currentFacings, this.incompatibleFacings);
        } else {
            return false;
        }
    }

    public boolean isDiagonal() {
        return this.diagonal;
    }

    public DiagonalFacing getOpposite() {
        return this.opposite;
    }

    public List<DiagonalFacing> getIncompatibles() {
        return this.incompatibleFacings;
    }

    public Vec3i getOffset() {
        return this.offset;
    }

    public static DiagonalFacing getFromProperty(PropertyBool prop) {
        return valueOf(prop.getName().toUpperCase());
    }

    public double[] getRotationsFromStandardNorth() {
        return this.rotationsFromStandardNorth.clone();
    }
}
