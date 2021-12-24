package ru.craftlogic.api.event.entity;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class EntityTriggerWalkingEvent extends EntityEvent {
    public final Block block;
    public final BlockPos pos;

    public EntityTriggerWalkingEvent(Entity entity, Block block, BlockPos pos) {
        super(entity);
        this.block = block;
        this.pos = pos;
    }
}
