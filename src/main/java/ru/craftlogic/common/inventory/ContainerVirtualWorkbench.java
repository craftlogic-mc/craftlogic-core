package ru.craftlogic.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerVirtualWorkbench extends ContainerWorkbench {
    public ContainerVirtualWorkbench(InventoryPlayer playerInv, World world) {
        super(playerInv, world, BlockPos.ORIGIN);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
