package ru.craftlogic.api.block;

import net.minecraft.util.EnumFacing;

public interface SmokeAcceptor {
    int acceptSmoke(EnumFacing side, int amount);
    int getThroughput();
    SmokeAcceptor getOutput();
}
