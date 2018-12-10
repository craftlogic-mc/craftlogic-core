package ru.craftlogic.api.event.block;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class FluidFlowEvent extends Event {
    private final Fluid fluid;
    private final World world;
    private final BlockPos pos;
    private final EnumFacing facing;

    public FluidFlowEvent(Fluid fluid, World world, BlockPos pos, EnumFacing facing) {
        this.fluid = fluid;
        this.world = world;
        this.pos = pos;
        this.facing = facing;
    }

    public Fluid getFluid() {
        return fluid;
    }

    public World getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public EnumFacing getFacing() {
        return facing;
    }
}
