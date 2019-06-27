package ru.craftlogic.api.model;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ModelRegistrar {
    @SideOnly(Side.CLIENT)
    void registerModel(ModelManager modelManager);
}
