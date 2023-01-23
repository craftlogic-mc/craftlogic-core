package ru.craftlogic.api.event.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PlayerInitialSpawnEvent extends Event {

    public BlockPos spawnPos;
    public World world;
    public final GameProfile profile;

    public PlayerInitialSpawnEvent(BlockPos spawnPos, World world, GameProfile profile) {
        this.spawnPos = spawnPos;
        this.world = world;
        this.profile = profile;
    }
}
