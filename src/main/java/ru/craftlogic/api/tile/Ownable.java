package ru.craftlogic.api.tile;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import ru.craftlogic.api.world.OfflinePlayer;

import java.util.Objects;
import java.util.UUID;

public interface Ownable {
    default boolean isOwner(EntityPlayer player) {
        return this.isOwner(player.getGameProfile());
    }

    default boolean isOwner(OfflinePlayer player) {
        return this.isOwner(player.getId());
    }

    default boolean isOwner(GameProfile profile) {
        return this.isOwner(profile.getId());
    }

    default boolean isOwner(UUID id) {
        return Objects.equals(id, getOwner());
    }

    UUID getOwner();

    default void setOwner(EntityPlayer player) {
        this.setOwner(player.getGameProfile());
    }

    default void setOwner(OfflinePlayer player) {
        this.setOwner(player.getId());
    }

    default void setOwner(GameProfile profile) {
        this.setOwner(profile.getId());
    }

    void setOwner(UUID owner);
}
