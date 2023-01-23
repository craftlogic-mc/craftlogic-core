package ru.craftlogic.api.event.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PlayerInitialSpawnEvent extends Event {

    public BlockPos spawnPos;
    public final GameProfile profile;

    public PlayerInitialSpawnEvent(BlockPos spawnPos, GameProfile profile) {
        this.spawnPos = spawnPos;
        this.profile = profile;
    }
}
