package ru.craftlogic.api.event.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PlayerCheckCanEditEvent extends PlayerEvent {
    public final BlockPos pos;
    public final EnumFacing side;
    public final ItemStack item;

    public PlayerCheckCanEditEvent(EntityPlayer player, BlockPos pos, EnumFacing side, ItemStack item) {
        super(player);
        this.pos = pos;
        this.side = side;
        this.item = item;
    }
}
