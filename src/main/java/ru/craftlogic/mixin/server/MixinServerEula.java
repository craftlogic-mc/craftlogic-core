package ru.craftlogic.mixin.server;

import net.minecraft.server.ServerEula;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.File;

@Mixin(ServerEula.class)
public class MixinServerEula {
    /**
     * @author Radviger
     * @reason Evil eula
     */
    @Overwrite
    private boolean loadEULAFile(File file) {
        return true;
    }

    /**
     * @author Radviger
     * @reason Evil eula
     */
    @Overwrite
    public void createEULAFile() {}
}
