package ru.craftlogic.api.heat.tile;

import net.minecraft.util.EnumFacing;
import ru.craftlogic.api.world.Locatable;

public interface HeatConnectable extends Locatable {
    HeatConductor getHeatConductor(EnumFacing side);

    default boolean canConnectHeatTo(HeatConnectable target, EnumFacing side) {
        return true;
    }
}
