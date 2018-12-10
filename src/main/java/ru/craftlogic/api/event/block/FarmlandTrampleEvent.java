package ru.craftlogic.api.event.block;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class FarmlandTrampleEvent extends Event {
    private final World world;
    private final BlockPos pos;
    private final Entity entity;

    public FarmlandTrampleEvent(World world, BlockPos pos, Entity entity) {
        this.world = world;
        this.pos = pos;
        this.entity = entity;
    }

    public World getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Entity getEntity() {
        return entity;
    }
}
