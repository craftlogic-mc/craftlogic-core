package ru.craftlogic.api.event.block;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.Event.HasResult;

import java.util.List;

@HasResult
public class PistonCheckCanMoveEvent extends Event {
    private final World world;
    private final BlockPos pistonPos;
    private final BlockPos blockToMove;
    private final EnumFacing moveDirection;
    private final List<BlockPos> toMove;
    private final List<BlockPos> toDestroy;

    public PistonCheckCanMoveEvent(World world, BlockPos pistonPos, BlockPos blockToMove, EnumFacing moveDirection, List<BlockPos> toMove, List<BlockPos> toDestroy) {
        this.world = world;
        this.pistonPos = pistonPos;
        this.blockToMove = blockToMove;
        this.moveDirection = moveDirection;
        this.toMove = toMove;
        this.toDestroy = toDestroy;
    }

    public World getWorld() {
        return world;
    }

    public BlockPos getBlockToMove() {
        return blockToMove;
    }

    public EnumFacing getMoveDirection() {
        return moveDirection;
    }

    public List<BlockPos> getToMove() {
        return toMove;
    }

    public List<BlockPos> getToDestroy() {
        return toDestroy;
    }

    public BlockPos getPistonPos() {
        return pistonPos;
    }
}
