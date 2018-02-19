package ru.craftlogic.api.block.holders;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.world.Location;

public interface ScreenHolder {
    Location getLocation();

    Container createContainer(EntityPlayer player, int subId);

    @SideOnly(Side.CLIENT)
    GuiScreen createScreen(EntityPlayer player, int subId);
}
