package ru.craftlogic.mixin.forge;

import com.google.common.io.Files;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

@Mixin(value = UsernameCache.class, remap = false)
public class MixinUsernameCache {
    @Shadow(remap = false) @Final
    private static Charset charset;
    @Shadow(remap = false) @Final @Mutable
    private static final File saveFile = new File((File) FMLInjectionData.data()[6], "./settings/usernamecache.json");
    @Shadow(remap = false) @Final
    private static Logger log;

    @Mixin(targets = "net/minecraftforge/common/UsernameCache$SaveThread", remap = false)
    private static class MixinSaveThread extends Thread {
        @Shadow(remap = false) @Final private String data;

        /**
         * @author Radviger
         * @reason Moved server configuration to /settings/
         */
        @Overwrite(remap = false)
        public void run() {
            try {
                synchronized(saveFile) {
                    Files.createParentDirs(saveFile);
                    Files.write(this.data, saveFile, charset);
                }
            } catch (IOException exc) {
                log.error("Failed to save username cache to file!", exc);
            }
        }
    }
}
