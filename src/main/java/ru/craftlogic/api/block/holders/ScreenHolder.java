package ru.craftlogic.api.block.holders;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ScreenHolder {
    Container createContainer(EntityPlayer player, int subId);

    @SideOnly(Side.CLIENT)
    GuiScreen createScreen(EntityPlayer player, int subId);
}
