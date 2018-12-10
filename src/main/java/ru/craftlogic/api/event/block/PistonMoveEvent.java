package ru.craftlogic.api.event.block;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class PistonMoveEvent extends Event {
    private final World world;
    private final BlockPos pos;
    private final EnumFacing facing;
    private final boolean push;

    public PistonMoveEvent(World world, BlockPos pos, EnumFacing facing, boolean push) {
        this.world = world;
        this.pos = pos;
        this.facing = facing;
        this.push = push;
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

    public boolean isPush() {
        return push;
    }
}
