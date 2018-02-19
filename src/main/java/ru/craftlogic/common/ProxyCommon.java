package ru.craftlogic.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.world.TileEntities;

import javax.annotation.Nullable;

public class ProxyCommon implements IGuiHandler {
    public void preInit() {}
    public void init() {}
    public void postInit() {}

    @Nullable
    @Override
    public Container getServerGuiElement(int subId, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        ScreenHolder screenHolder = TileEntities.getTileEntity(world, pos, ScreenHolder.class);
        return screenHolder != null ? screenHolder.createContainer(player, subId) : null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }
}
