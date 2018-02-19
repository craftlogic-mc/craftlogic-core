package ru.craftlogic.api.block;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;

public interface Ownable {
    default boolean isOwner(EntityPlayer player) {
        return this.isOwner(player.getGameProfile());
    }

    boolean isOwner(GameProfile profile);
}
