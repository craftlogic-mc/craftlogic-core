package ru.craftlogic.mixin.server.management;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import net.minecraft.server.management.PlayerProfileCache;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(PlayerProfileCache.class)
public abstract class MixinPlayerProfileCache {
    @Shadow @Final @Mutable private File usercacheFile;

    @Inject(method = "load", at = @At("HEAD"))
    public void onLoad(CallbackInfo info) {
        this.usercacheFile = new File("./settings/", this.usercacheFile.getName());
    }

    /**
     * @author Radviger
     * @reason Moved server configuration to /settings/
     */
    @Overwrite
    private static GameProfile lookupProfile(GameProfileRepository repo, String username) {
        return null;
    }
}
