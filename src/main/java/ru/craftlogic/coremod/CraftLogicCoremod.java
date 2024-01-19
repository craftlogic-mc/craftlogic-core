package ru.craftlogic.coremod;

import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.Tags;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name(Tags.MODID + "-coremod")
@IFMLLoadingPlugin.SortingIndex(1002)
public class CraftLogicCoremod implements IFMLLoadingPlugin, IEarlyMixinLoader {
    private static final Logger LOGGER = LogManager.getLogger("CLC");

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

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins.craftlogic.json");
    }
}
