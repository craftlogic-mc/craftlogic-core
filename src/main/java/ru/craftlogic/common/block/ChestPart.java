package ru.craftlogic.common.block;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import ru.craftlogic.api.util.Nameable;

public enum ChestPart implements Nameable {
    SINGLE(Rotation.NONE),
    LEFT(Rotation.COUNTERCLOCKWISE_90),
    RIGHT(Rotation.CLOCKWISE_90);

    private final Rotation rotation;

    ChestPart(Rotation rotation) {
        this.rotation = rotation;
    }

    public EnumFacing rotate(EnumFacing facing) {
        return this.rotation.rotate(facing);
    }

    public ChestPart opposite() {
        switch (this) {
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
        }
        return SINGLE;
    }
}