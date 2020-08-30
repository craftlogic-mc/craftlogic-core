package ru.craftlogic.mixin.forge;

import com.google.common.io.Files;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.io.IOException;

@Mixin(value = UsernameCache.class, remap = false)
public class MixinUsernameCache {
    @Shadow(remap = false) @Final @Mutable
    private static final File saveFile = new File((File) FMLInjectionData.data()[6], "./settings/usernamecache.json");

    static {
        try {
            Files.createParentDirs(saveFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
