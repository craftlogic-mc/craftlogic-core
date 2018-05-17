package ru.craftlogic.api.model;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ModelAutoReg {
    @SideOnly(Side.CLIENT)
    void registerModel(ModelManager modelManager);
}
