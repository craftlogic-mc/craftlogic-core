package ru.craftlogic.coremod;

import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("craftlogic-coremod")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions({
    "groovy.",
    "groovyjarjarantlr.",
    "groovyjarjarasm.",
    "groovyjarjarcommonscli.",
    "org.codehaus.groovy."
})
public class CraftLogicCoremod implements IFMLLoadingPlugin {

    public CraftLogicCoremod() {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.craftlogic.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {};
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
    public void injectData(Map<String, Object> map) {
        CoreModManager.getIgnoredMods().add("groovy-2.4.14");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
