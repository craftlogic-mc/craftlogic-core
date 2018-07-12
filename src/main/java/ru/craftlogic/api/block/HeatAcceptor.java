package ru.craftlogic.api.block;

import net.minecraft.util.EnumFacing;

public interface HeatAcceptor extends HeatConductor {
    int acceptHeat(EnumFacing side, int amount);
}
