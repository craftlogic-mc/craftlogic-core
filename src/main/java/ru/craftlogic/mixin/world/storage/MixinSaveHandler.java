package ru.craftlogic.mixin.world.storage;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.storage.SaveHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.server.AdvancedPlayerFileData;

import java.io.File;

@Mixin(SaveHandler.class)
public abstract class MixinSaveHandler implements AdvancedPlayerFileData {
    @Shadow @Final private File playersDirectory;

    @Override
    public boolean hasPlayerData(GameProfile profile) {
        File playerDataFile = new File(this.playersDirectory, profile.getId() + ".dat");
        return playerDataFile.exists();
    }
}
