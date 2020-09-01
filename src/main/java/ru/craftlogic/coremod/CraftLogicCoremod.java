package ru.craftlogic.coremod;

import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("{@mc:version}")
@IFMLLoadingPlugin.Name("{@mod:id}-coremod")
@IFMLLoadingPlugin.SortingIndex(1002)
public class CraftLogicCoremod implements IFMLLoadingPlugin {
    private static final Logger LOGGER = LogManager.getLogger("CLC");

    private static final String MIXIN_VERSION = "0.7.8-SNAPSHOT";

    public CraftLogicCoremod() {
        if (!MIXIN_VERSION.startsWith(MixinBootstrap.VERSION)) {
            LOGGER.warn("Your classpath contains different version of sponge-mixin. Things may go wrong!");
        }

        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.craftlogic.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
            "ru.craftlogic.coremod.transformers.RemoteDependencyTransformer"
        };
    }

    @Override
    public String getModContainerClass() {
        return "ru.craftlogic.coremod.CraftModContainer";
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        LaunchClassLoader cl = (LaunchClassLoader) this.getClass().getClassLoader();
        cl.addTransformerExclusion("ru.craftlogic.coremod.transformers.");
        cl.addTransformerExclusion("groovy.");
        cl.addTransformerExclusion("org.codehaus.groovy.");
        cl.addTransformerExclusion("groovyjarjarantlr.");
        cl.addTransformerExclusion("groovyjarjarasm.");
        cl.addTransformerExclusion("groovyjarjarcommonscli.");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}
