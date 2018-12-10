package ru.craftlogic.api.event.block;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nullable;
import java.util.UUID;

@Cancelable
public class DispenserShootEvent extends Event {
    private final World world;
    private final BlockPos pos;
    private final EnumFacing facing;
    @Nullable
    private final UUID owner;

    public DispenserShootEvent(World world, BlockPos pos, EnumFacing facing, @Nullable UUID owner) {
        this.world = world;
        this.pos = pos;
        this.facing = facing;
        this.owner = owner;
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

    @Nullable
    public EntityPlayer getPlayer(WorldServer world) {
        return this.owner == null ? FakePlayerFactory.getMinecraft(world) : FakePlayerFactory.get(world, new GameProfile(this.owner, null));
    }
}
