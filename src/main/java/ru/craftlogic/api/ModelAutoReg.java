package ru.craftlogic.api;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.client.ModelManager;

public interface ModelAutoReg {
    @SideOnly(Side.CLIENT)
    void registerModel(ModelManager modelManager);
}
