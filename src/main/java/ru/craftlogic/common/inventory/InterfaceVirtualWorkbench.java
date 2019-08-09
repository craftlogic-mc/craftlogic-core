package ru.craftlogic.common.inventory;

import net.minecraft.block.BlockWorkbench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InterfaceVirtualWorkbench extends BlockWorkbench.InterfaceCraftingTable {
    private final World world;

    public InterfaceVirtualWorkbench(World world) {
        super(world, BlockPos.ORIGIN);
        this.world = world;
    }

    @Override
    public Container createContainer(InventoryPlayer playerInv, EntityPlayer player) {
        return new ContainerVirtualWorkbench(playerInv, this.world);
    }
}
