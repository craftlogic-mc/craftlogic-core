package ru.craftlogic.api.server;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.storage.IPlayerFileData;

public interface AdvancedPlayerFileData extends IPlayerFileData {
    boolean hasPlayerData(GameProfile profile);
}
