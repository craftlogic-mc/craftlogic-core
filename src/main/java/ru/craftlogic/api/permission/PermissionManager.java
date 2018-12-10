package ru.craftlogic.api.permission;

import com.mojang.authlib.GameProfile;

import java.util.Collection;
import java.util.Collections;

public interface PermissionManager {
    boolean isEnabled();
    default boolean hasPermission(GameProfile profile, String permission) {
        return this.hasPermissions(profile, Collections.singleton(permission));
    }
    boolean hasPermissions(GameProfile profile, Collection<String> permissions);
    String getPermissionMetadata(GameProfile profile, String meta);
}
